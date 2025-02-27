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
package forestry.apiculture.multiblock;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.climate.ClimateCapabilities;
import forestry.api.climate.IClimateListener;
import forestry.api.climate.IClimatised;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.IErrorLogic;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.gui.ContainerAlveary;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.MultiblockTileEntityForestry;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;
import forestry.core.owner.IOwnerHandler;
import forestry.core.tiles.ITitled;
import forestry.core.utils.RenderUtil;

public class TileAlveary extends MultiblockTileEntityForestry<MultiblockLogicAlveary> implements IBeeHousing, IAlvearyComponent, IOwnedTile, IStreamableGui, ITitled, IClimatised {
	private final String unlocalizedTitle;

	public TileAlveary(BlockAlvearyType type, BlockPos pos, BlockState state) {
		super(type.getTileType().tileType(), pos, state, new MultiblockLogicAlveary());
		this.unlocalizedTitle = ApicultureBlocks.ALVEARY.get(type).getTranslationKey();
	}

	@Override
	public void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord) {
		Block block = getBlockState().getBlock();
		if (block instanceof BlockAlveary) {
			level.setBlockAndUpdate(getBlockPos(), ((BlockAlveary) block).getNewState(this));
		}
		level.updateNeighborsAt(getBlockPos(), block);
	}

	@Override
	public void onMachineBroken() {
		Block block = getBlockState().getBlock();
		if (block instanceof BlockAlveary) {
			level.setBlockAndUpdate(getBlockPos(), ((BlockAlveary) block).getNewState(this));
		}
		level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
		// Re-render this block on the client
		if (level.isClientSide) {
			//TODO
			BlockPos pos = getBlockPos();
			RenderUtil.markForUpdate(pos);
		}
		setChanged();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		LazyOptional<T> superCap = super.getCapability(capability, facing);
		if (superCap.isPresent()) {
			return superCap;
		}

		if (capability == ForgeCapabilities.ITEM_HANDLER) {
			if (facing != null) {
				SidedInvWrapper sidedInvWrapper = new SidedInvWrapper(getInternalInventory(), facing);
				return LazyOptional.of(() -> sidedInvWrapper).cast();    //TODO - still not sure if I am doing this right
			} else {
				InvWrapper invWrapper = new InvWrapper(getInternalInventory());
				return LazyOptional.of(() -> invWrapper).cast();
			}
		}
		if (capability == ClimateCapabilities.CLIMATE_LISTENER) {
			IClimateListener listener = getMultiblockLogic().getController().getClimateListener();
			return LazyOptional.of(() -> listener).cast();
		}
		return LazyOptional.empty();
	}

	/* IHousing */
	@Override
	public Biome getBiome() {
		return getMultiblockLogic().getController().getBiome();
	}

	/* IBeeHousing */
	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return getMultiblockLogic().getController().getBeeModifiers();
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return getMultiblockLogic().getController().getBeeListeners();
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return getMultiblockLogic().getController().getBeeInventory();
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return getMultiblockLogic().getController().getBeekeepingLogic();
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return getMultiblockLogic().getController().getBeeFXCoordinates();
	}

	/* IClimatised */
	@Override
	public EnumTemperature getTemperature() {
		return getMultiblockLogic().getController().getTemperature();
	}

	@Override
	public EnumHumidity getHumidity() {
		return getMultiblockLogic().getController().getHumidity();
	}

	@Override
	public int getBlockLightValue() {
		return getMultiblockLogic().getController().getBlockLightValue();
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return getMultiblockLogic().getController().canBlockSeeTheSky();
	}

	@Override
	public boolean isRaining() {
		return getMultiblockLogic().getController().isRaining();
	}

	@Override
	public IErrorLogic getErrorLogic() {
		return getMultiblockLogic().getController().getErrorLogic();
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return getMultiblockLogic().getController().getOwnerHandler();
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return getMultiblockLogic().getController().getInternalInventory();
	}

	@Override
	public String getUnlocalizedTitle() {
		return unlocalizedTitle;
	}

	/* IClimatised */
	@Override
	public float getExactTemperature() {
		return getMultiblockLogic().getController().getExactTemperature();
	}

	@Override
	public float getExactHumidity() {
		return getMultiblockLogic().getController().getExactHumidity();
	}

	/* IStreamableGui */
	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		getMultiblockLogic().getController().writeGuiData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		getMultiblockLogic().getController().readGuiData(data);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAlveary(windowId, player.getInventory(), this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(this.getUnlocalizedTitle());
	}
}
