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
package forestry.core.utils;

import com.google.common.collect.AbstractIterator;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class VectUtil {
	public static final Comparator<BlockPos> TOP_DOWN_COMPARATOR = (BlockPos a, BlockPos b) -> Integer.compare(b.getY(), a.getY());

	private VectUtil() {
	}

	public static BlockPos getRandomPositionInArea(Random random, Vec3i area) {
		int x = random.nextInt(area.getX());
		int y = random.nextInt(area.getY());
		int z = random.nextInt(area.getZ());
		return new BlockPos(x, y, z);
	}

	public static BlockPos getRandomPositionInArea(RandomSource random, Vec3i area) {
		int x = random.nextInt(area.getX());
		int y = random.nextInt(area.getY());
		int z = random.nextInt(area.getZ());
		return new BlockPos(x, y, z);
	}

	public static BlockPos add(Vec3i... vects) {
		int x = 0;
		int y = 0;
		int z = 0;
		for (Vec3i vect : vects) {
			x += vect.getX();
			y += vect.getY();
			z += vect.getZ();
		}
		return new BlockPos(x, y, z);
	}

	public static BlockPos scale(Vec3i vect, float factor) {
		return new BlockPos(vect.getX() * factor, vect.getY() * factor, vect.getZ() * factor);
	}

	public static Direction direction(Vec3i a, Vec3i b) {
		int x = Math.abs(a.getX() - b.getX());
		int y = Math.abs(a.getY() - b.getY());
		int z = Math.abs(a.getZ() - b.getZ());
		int max = Math.max(x, Math.max(y, z));
		if (max == x) {
			return Direction.EAST;
		} else if (max == z) {
			return Direction.SOUTH;
		} else {
			return Direction.UP;
		}
	}

	public static Iterator<BlockPos.MutableBlockPos> getAllInBoxFromCenterMutable(Level world, final BlockPos from, final BlockPos center, final BlockPos to) {
		final BlockPos minPos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
		final BlockPos maxPos = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

		return new MutableBlockPosSpiralIterator(world, center, maxPos, minPos);
	}

	private static class MutableBlockPosSpiralIterator extends AbstractIterator<BlockPos.MutableBlockPos> {
		private final Level world;
		private final BlockPos center;
		private final BlockPos maxPos;
		private final BlockPos minPos;
		private int spiralLayer;
		private final int maxSpiralLayers;
		private int direction;

		@Nullable
		private BlockPos.MutableBlockPos theBlockPos;

		public MutableBlockPosSpiralIterator(Level world, BlockPos center, BlockPos maxPos, BlockPos minPos) {
			this.world = world;
			this.center = center;
			this.maxPos = maxPos;
			this.minPos = minPos;

			int xDiameter = maxPos.getX() - minPos.getX();
			int zDiameter = maxPos.getZ() - minPos.getZ();
			this.maxSpiralLayers = Math.max(xDiameter, zDiameter) / 2;
			this.spiralLayer = 1;
		}

		@Override
		@Nullable
		protected BlockPos.MutableBlockPos computeNext() {
			BlockPos.MutableBlockPos pos;

			do {
				pos = nextPos();
			}
			while (pos != null && (pos.getX() > maxPos.getX() || pos.getY() > maxPos.getY() || pos.getZ() > maxPos.getZ() || pos.getX() < minPos.getX() || pos.getY() < minPos.getY() || pos.getZ() < minPos.getZ()));

			return pos;
		}

		@Nullable
		protected BlockPos.MutableBlockPos nextPos() {
			if (this.theBlockPos == null) {
				this.theBlockPos = new BlockPos.MutableBlockPos(center.getX(), maxPos.getY(), center.getZ());
				int y = Math.min(this.maxPos.getY(), this.world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, this.theBlockPos).getY());
				this.theBlockPos.setY(y);
				return this.theBlockPos;
			} else if (spiralLayer > maxSpiralLayers) {
				return this.endOfData();
			} else {
				int x = this.theBlockPos.getX();
				int y = this.theBlockPos.getY();
				int z = this.theBlockPos.getZ();

				if (y > minPos.getY() && y > 0) {
					y--;
				} else {
					switch (direction) {
						case 0 -> {
							++x;
							if (x == center.getX() + spiralLayer) {
								++direction;
							}
						}
						case 1 -> {
							++z;
							if (z == center.getZ() + spiralLayer) {
								++direction;
							}
						}
						case 2 -> {
							--x;
							if (x == center.getX() - spiralLayer) {
								++direction;
							}
						}
						case 3 -> {
							--z;
							if (z == center.getZ() - spiralLayer) {
								direction = 0;
								++spiralLayer;
							}
						}
					}

					this.theBlockPos.set(x, y, z);
					y = Math.min(this.maxPos.getY(), this.world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, this.theBlockPos).getY());
				}

				return this.theBlockPos.set(x, y, z);
			}
		}
	}
}
