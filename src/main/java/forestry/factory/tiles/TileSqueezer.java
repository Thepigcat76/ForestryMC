/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.factory.tiles;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import forestry.api.circuits.ChipsetManager;
import forestry.api.circuits.CircuitSocketType;
import forestry.api.circuits.ICircuitBoard;
import forestry.api.circuits.ICircuitSocketType;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.ISqueezerRecipe;
import forestry.api.recipes.RecipeManagers;
import forestry.core.circuits.ISocketable;
import forestry.core.circuits.ISpeedUpgradable;
import forestry.core.config.Constants;
import forestry.core.errors.EnumErrorCode;
import forestry.core.fluids.StandardTank;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.render.TankRenderInfo;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.InventoryUtil;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.ContainerSqueezer;
import forestry.factory.inventory.InventorySqueezer;

public class TileSqueezer extends TilePowered implements ISocketable, WorldlyContainer, ILiquidTankTile, ISpeedUpgradable {
	private static final int TICKS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 2000;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 10;

	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");

	private final TankManager tankManager;
	private final StandardTank productTank;
	private final InventorySqueezer inventory;
	@Nullable
	private ISqueezerRecipe currentRecipe;

	public TileSqueezer(BlockPos pos, BlockState state) {
		super(FactoryTiles.SQUEEZER.tileType(), pos, state, 1100, Constants.MACHINE_MAX_ENERGY);
		this.inventory = new InventorySqueezer(this);
		setInternalInventory(this.inventory);
		this.productTank = new StandardTank(Constants.PROCESSOR_TANK_CAPACITY, false, true);
		this.tankManager = new TankManager(this, productTank);
	}

	/* LOADING & SAVING */

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
		tankManager.write(compoundNBT);
		sockets.write(compoundNBT);
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
		tankManager.read(compoundNBT);
		sockets.read(compoundNBT);

		ItemStack chip = sockets.getItem(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = ChipsetManager.circuitRegistry.getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
		tankManager.readData(data);
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		super.writeGuiData(data);
		sockets.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		super.readGuiData(data);
		sockets.readData(data);
	}

	// / WORKING
	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			FluidStack fluid = productTank.getFluid();
			if (!fluid.isEmpty()) {
				inventory.fillContainers(fluid, tankManager);
			}
		}
	}

	@Override
	public boolean workCycle() {
		if (currentRecipe == null) {
			return false;
		}
		if (!inventory.removeResources(currentRecipe.getResources())) {
			return false;
		}

		FluidStack resultFluid = currentRecipe.getFluidOutput();
		productTank.fillInternal(resultFluid, IFluidHandler.FluidAction.EXECUTE);

		if (!currentRecipe.getRemnants().isEmpty() && level.random.nextFloat() < currentRecipe.getRemnantsChance()) {
			ItemStack remnant = currentRecipe.getRemnants().copy();
			inventory.addRemnant(remnant, true);
		}

		return true;
	}

	private boolean checkRecipe() {
		ISqueezerRecipe matchingRecipe = null;

		if (inventory.hasResources()) {
			NonNullList<ItemStack> resources = inventory.getResources();

			boolean containsSets = false;

			if (currentRecipe != null) {
				Container inventory = new InventoryMapper(this, InventorySqueezer.SLOT_RESOURCE_1, InventorySqueezer.SLOTS_RESOURCE_COUNT);
				containsSets = InventoryUtil.consumeIngredients(inventory, currentRecipe.getResources(), null, false, false, false);
			}

			if (currentRecipe != null && containsSets) {
				matchingRecipe = currentRecipe;
			} else {
				matchingRecipe = RecipeManagers.squeezerManager.findMatchingRecipe(getLevel().getRecipeManager(), resources)
						.orElse(null);
			}

			if (matchingRecipe == null) {
				for (ItemStack resource : resources) {
					if (matchingRecipe == null) {
						matchingRecipe = RecipeManagers.squeezerContainerManager.findMatchingContainerRecipe(getLevel().getRecipeManager(), resource)
								.orElse(null);
					}
				}
			}
		}

		if (currentRecipe != matchingRecipe) {
			currentRecipe = matchingRecipe;

			if (currentRecipe != null) {
				int recipeTime = currentRecipe.getProcessingTime();
				setTicksPerWorkCycle(recipeTime * TICKS_PER_RECIPE_TIME);
				setEnergyPerWorkCycle(recipeTime * ENERGY_PER_RECIPE_TIME);
			}
		}

		getErrorLogic().setCondition(currentRecipe == null, EnumErrorCode.NO_RECIPE);
		return currentRecipe != null;
	}

	@Override
	public boolean hasWork() {
		checkRecipe();

		boolean hasResources = inventory.hasResources();
		boolean hasRecipe = true;
		boolean canFill = true;
		boolean canAdd = true;

		if (hasResources) {
			hasRecipe = currentRecipe != null;
			if (hasRecipe) {
				FluidStack resultFluid = currentRecipe.getFluidOutput();
				canFill = productTank.fillInternal(resultFluid, IFluidHandler.FluidAction.SIMULATE) == resultFluid.getAmount();

				if (!currentRecipe.getRemnants().isEmpty()) {
					canAdd = inventory.addRemnant(currentRecipe.getRemnants(), false);
				}
			}
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasResources, EnumErrorCode.NO_RESOURCE);
		errorLogic.setCondition(!hasRecipe, EnumErrorCode.NO_RECIPE);
		errorLogic.setCondition(!canFill, EnumErrorCode.NO_SPACE_TANK);
		errorLogic.setCondition(!canAdd, EnumErrorCode.NO_SPACE_INVENTORY);

		return hasResources && hasRecipe && canFill && canAdd;
	}

	@Override
	public TankRenderInfo getProductTankInfo() {
		return new TankRenderInfo(productTank);
	}


	@Override
	public TankManager getTankManager() {
		return tankManager;
	}

	/* ISocketable */
	@Override
	public int getSocketCount() {
		return sockets.getContainerSize();
	}

	@Override
	public ItemStack getSocket(int slot) {
		return sockets.getItem(slot);
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {
		if (stack.isEmpty() || ChipsetManager.circuitRegistry.isChipset(stack)) {
			// Dispose correctly of old chipsets
			if (!sockets.getItem(slot).isEmpty()) {
				if (ChipsetManager.circuitRegistry.isChipset(sockets.getItem(slot))) {
					ICircuitBoard chipset = ChipsetManager.circuitRegistry.getCircuitBoard(sockets.getItem(slot));
					if (chipset != null) {
						chipset.onRemoval(this);
					}
				}
			}

			sockets.setItem(slot, stack);
			if (!stack.isEmpty()) {
				ICircuitBoard chipset = ChipsetManager.circuitRegistry.getCircuitBoard(stack);
				if (chipset != null) {
					chipset.onInsertion(this);
				}
			}
		}
	}

	@Override
	public ICircuitSocketType getSocketType() {
		return CircuitSocketType.MACHINE;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> tankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerSqueezer(windowId, inv, this);
	}
}
