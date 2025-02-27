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

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import forestry.api.climate.IClimateControlled;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.core.network.packets.PacketActiveUpdate;
import forestry.core.tiles.IActivatable;
import forestry.core.utils.NetworkUtil;
import forestry.energy.EnergyHelper;
import forestry.energy.ForestryEnergyStorage;
import forestry.energy.EnergyTransferMode;

public abstract class TileAlvearyClimatiser extends TileAlveary implements IActivatable, IAlvearyComponent.Climatiser {

	private static final int WORK_CYCLES = 1;
	private static final int ENERGY_PER_OPERATION = 50;

	protected interface IClimitiserDefinition {
		float getChangePerTransfer();

		float getBoundaryUp();

		float getBoundaryDown();
	}

	private final ForestryEnergyStorage energyStorage;
	private final LazyOptional<ForestryEnergyStorage> energyCap;

	private final IClimitiserDefinition definition;

	private int workingTime = 0;

	// CLIENT
	private boolean active;

	protected TileAlvearyClimatiser(BlockAlvearyType alvearyType, BlockPos pos, BlockState state, IClimitiserDefinition definition) {
		super(alvearyType, pos, state);
		this.definition = definition;

		this.energyStorage = new ForestryEnergyStorage(1000, 2000, EnergyTransferMode.RECEIVE);
		this.energyCap = LazyOptional.of(() -> energyStorage);
	}

	/* UPDATING */
	@Override
	public void changeClimate(int tick, IClimateControlled climateControlled) {
		if (workingTime < 20 && EnergyHelper.consumeEnergyToDoWork(energyStorage, WORK_CYCLES, ENERGY_PER_OPERATION)) {
			// one tick of work for every 10 RF
			workingTime += ENERGY_PER_OPERATION / 10;
		}

		if (workingTime > 0) {
			workingTime--;
			climateControlled.addTemperatureChange(definition.getChangePerTransfer(), definition.getBoundaryDown(), definition.getBoundaryUp());
		}

		setActive(workingTime > 0);
	}

	/* LOADING & SAVING */
	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
		energyStorage.read(compoundNBT);
		workingTime = compoundNBT.getInt("Heating");
		setActive(workingTime > 0);
	}

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
		energyStorage.write(compoundNBT);
		compoundNBT.putInt("Heating", workingTime);
	}

	/* Network */
	@Override
	protected void encodeDescriptionPacket(CompoundTag packetData) {
		super.encodeDescriptionPacket(packetData);
		packetData.putBoolean("Active", active);
	}

	@Override
	protected void decodeDescriptionPacket(CompoundTag packetData) {
		super.decodeDescriptionPacket(packetData);
		setActive(packetData.getBoolean("Active"));
	}

	/* IActivatable */
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		if (this.active == active) {
			return;
		}

		this.active = active;

		if (level != null) {
			if (level.isClientSide) {
				//TODO
				BlockPos pos = getBlockPos();
				Minecraft.getInstance().levelRenderer.setSectionDirty(pos.getX(), pos.getY(), pos.getZ());
				//				world.markForRerender(getPos());
			} else {
				NetworkUtil.sendNetworkPacket(new PacketActiveUpdate(this), worldPosition, level);
			}
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!remove && capability == ForgeCapabilities.ENERGY) {
			return energyCap.cast();
		}
		return super.getCapability(capability, facing);
	}
}
