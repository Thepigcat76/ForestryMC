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
package forestry.core.multiblock;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import forestry.Forestry;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.multiblock.IMultiblockLogic;

public abstract class MultiblockLogic<T extends IMultiblockControllerInternal> implements IMultiblockLogic {
	private final Class<T> controllerClass;
	private boolean visited;
	private boolean saveMultiblockData;
	@Nullable
	private CompoundTag cachedMultiblockData;
	@Nullable
	protected T controller;

	protected MultiblockLogic(Class<T> controllerClass) {
		this.controllerClass = controllerClass;
		this.controller = null;
		this.visited = false;
		this.saveMultiblockData = false;
		this.cachedMultiblockData = null;
	}

	public void setController(@Nullable IMultiblockControllerInternal controller) {
		if (controller == null) {
			this.controller = null;
		} else if (controllerClass.isAssignableFrom(controller.getClass())) {
			this.controller = controllerClass.cast(controller);
		}
	}

	public Class<T> getControllerClass() {
		return controllerClass;
	}

	@Override
	public abstract T getController();

	public abstract T createNewController(Level world);

	/**
	 * This is called when a block is being marked as valid by the chunk, but has not yet fully
	 * been placed into the world's TileEntity cache. this.world, xCoord, yCoord and zCoord have
	 * been initialized, but any attempts to read data about the world can cause infinite loops -
	 * if you call getTileEntity on this TileEntity's coordinate from within validate(), you will
	 * blow your call stack.
	 * <p>
	 * TL;DR: Here there be dragons.
	 *
	 * @see net.minecraft.tileentity.TileEntity#validate()
	 */
	@Override
	public void validate(Level world, IMultiblockComponent part) {
		MultiblockRegistry.onPartAdded(world, part);
	}

	/**
	 * Called when a block is removed by game actions, such as a player breaking the block
	 * or the block being changed into another block.
	 *
	 * @see net.minecraft.tileentity.TileEntity#invalidate()
	 */
	@Override
	public final void invalidate(Level world, IMultiblockComponent part) {
		detachSelf(world, part, false);
	}

	/**
	 * Called from Minecraft's tile entity loop, after all tile entities have been ticked,
	 * as the chunk in which this tile entity is contained is unloading.
	 * Happens before the Forge TickEnd event.
	 *
	 * @see net.minecraft.tileentity.TileEntity#onChunkUnload()
	 */
	@Override
	public final void onChunkUnload(Level world, IMultiblockComponent part) {
		detachSelf(world, part, true);
	}

	/*
	 * Detaches this block from its controller. Calls detachBlock() and clears the controller member.
	 */
	protected void detachSelf(Level world, IMultiblockComponent part, boolean chunkUnloading) {
		if (this.controller != null) {
			// Clean part out of controller
			this.controller.detachBlock(part, chunkUnloading);

			// The above should call onDetached, but, just in case...
			this.controller = null;
		}

		// Clean part out of lists in the registry
		MultiblockRegistry.onPartRemovedFromWorld(world, part);
	}

	@Override
	public void readFromNBT(CompoundTag data) {
		// We can't directly initialize a multiblock controller yet, so we cache the data here until
		// we receive a validate() call, which creates the controller and hands off the cached data.
		if (data.contains("multiblockData")) {
			this.cachedMultiblockData = data.getCompound("multiblockData");
		}
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		if (isMultiblockSaveDelegate() && this.controller != null) {
			CompoundTag multiblockData = new CompoundTag();
			this.controller.write(multiblockData);
			data.put("multiblockData", multiblockData);
		}
		return data;
	}

	public final void assertDetached(IMultiblockComponent part) {
		if (this.controller != null) {
			BlockPos coords = part.getCoordinates();
			Forestry.LOGGER.info("[assert] Part @ ({}, {}, {}) should be detached already, but detected that it was not. This is not a fatal error, and will be repaired, but is unusual.", coords.getX(), coords.getY(), coords.getZ());
			this.controller = null;
		}
	}

	@Override
	public final boolean isConnected() {
		return controller != null;
	}

	public void becomeMultiblockSaveDelegate() {
		this.saveMultiblockData = true;
	}

	public void forfeitMultiblockSaveDelegate() {
		this.saveMultiblockData = false;
	}

	public final boolean isMultiblockSaveDelegate() {
		return this.saveMultiblockData;
	}

	public final void setUnvisited() {
		this.visited = false;
	}

	public final void setVisited() {
		this.visited = true;
	}

	public final boolean isVisited() {
		return this.visited;
	}

	public final boolean hasMultiblockSaveData() {
		return this.cachedMultiblockData != null;
	}

	@Nullable
	public final CompoundTag getMultiblockSaveData() {
		return this.cachedMultiblockData;
	}

	public final void onMultiblockDataAssimilated() {
		this.cachedMultiblockData = null;
	}

	/**
	 * Override this to easily modify the description packet's data without having
	 * to worry about sending the packet itself.
	 * Decode this data in decodeDescriptionPacket.
	 *
	 * @param packetData An NBT compound tag into which you should write your custom description data.
	 */
	@Override
	public void encodeDescriptionPacket(CompoundTag packetData) {
		if (this.isMultiblockSaveDelegate() && controller != null) {
			CompoundTag tag = new CompoundTag();
			controller.formatDescriptionPacket(tag);
			packetData.put("multiblockData", tag);
		}
	}

	/**
	 * Override this to easily read in data from a TileEntity's description packet.
	 * Encoded in encodeDescriptionPacket.
	 *
	 * @param packetData The NBT data from the tile entity's description packet.
	 */
	@Override
	public void decodeDescriptionPacket(CompoundTag packetData) {
		if (packetData.contains("multiblockData")) {
			CompoundTag tag = packetData.getCompound("multiblockData");
			if (controller != null) {
				controller.decodeDescriptionPacket(tag);
			} else {
				// This part hasn't been added to a machine yet, so cache the data.
				this.cachedMultiblockData = tag;
			}
		}
	}
}
