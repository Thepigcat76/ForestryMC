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
package forestry.core.blocks;

import javax.annotation.Nullable;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

import forestry.core.render.IForestryRendererProvider;
import forestry.core.tiles.ForestryTicker;
import forestry.core.tiles.TileForestry;

public interface IMachineProperties<T extends TileForestry> extends StringRepresentable, IShapeProvider {
	BlockEntityType<? extends T> getTeType();

	default void clientSetupRenderers(EntityRenderersEvent.RegisterRenderers event) {
	}

	@Nullable
	BlockEntity createTileEntity(BlockPos pos, BlockState state);

	@Nullable
	ForestryTicker<? extends T> getClientTicker();

	@Nullable
	ForestryTicker<? extends T> getServerTicker();

	void setBlock(Block block);

	@Nullable
	Block getBlock();
}
