package forestry.core.worldgen;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import forestry.api.arboriculture.ITreeGenData;
import forestry.arboriculture.worldgen.ITreeBlockType;
import forestry.arboriculture.worldgen.TreeBlockType;
import forestry.arboriculture.worldgen.TreeContour;
import forestry.core.utils.VectUtil;
//import forestry.arboriculture.worldgen.ITreeBlockType;
//import forestry.arboriculture.worldgen.TreeBlockType;


public class FeatureHelper {

	public static boolean addBlock(LevelAccessor world, BlockPos pos, ITreeBlockType type, EnumReplaceMode replaceMode) {
		return addBlock(world, pos, type, replaceMode, TreeContour.EMPTY);
	}

	public static boolean addBlock(LevelAccessor world, BlockPos pos, ITreeBlockType type, EnumReplaceMode replaceMode, TreeContour contour) {
		if (!world.hasChunkAt(pos)) {
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		if (replaceMode.canReplace(blockState, world, pos)) {
			type.setBlock(world, pos);
			contour.addLeaf(pos);
			return true;
		}
		return false;
	}

	/**
	 * Uses centerPos and girth of a tree to calculate the center
	 */
	public static void generateCylinderFromTreeStartPos(LevelAccessor world, ITreeBlockType block, BlockPos startPos, int girth, float radius, int height, EnumReplaceMode replace, TreeContour contour) {
		generateCylinderFromPos(world, block, startPos.offset(girth / 2, 0, girth / 2), radius, height, replace, contour);
	}

	/**
	 * Center is the bottom middle of the cylinder
	 */
	public static void generateCylinderFromPos(LevelAccessor world, ITreeBlockType block, BlockPos center, float radius, int height, EnumReplaceMode replace, TreeContour contour) {
		BlockPos start = new BlockPos(center.getX() - radius, center.getY(), center.getZ() - radius);
		for (int x = 0; x < radius * 2 + 1; x++) {
			for (int y = height - 1; y >= 0; y--) { // generating top-down is faster for lighting calculations
				for (int z = 0; z < radius * 2 + 1; z++) {
					BlockPos position = start.offset(x, y, z);
					Vec3i treeCenter = new Vec3i(center.getX(), position.getY(), center.getZ());
					if (position.distSqr(treeCenter) <= radius * radius + 0.01) {
						Direction direction = VectUtil.direction(position, treeCenter);
						block.setDirection(direction);
						if (addBlock(world, position, block, replace)) {
							contour.addLeaf(position);
						}
					}
				}
			}
		}
	}

	public static void generateCircleFromTreeStartPos(LevelAccessor world, RandomSource rand, BlockPos startPos, int girth, float radius, int width, int height, ITreeBlockType block, float chance, EnumReplaceMode replace, TreeContour contour) {
		generateCircle(world, rand, startPos.offset(girth / 2, 0, girth / 2), radius, width, height, block, chance, replace, contour);
	}

	public static void generateCircle(LevelAccessor world, RandomSource rand, BlockPos center, float radius, int width, int height, ITreeBlockType block, float chance, EnumReplaceMode replace, TreeContour contour) {
		Vec3i start = new Vec3i(center.getX() - radius, center.getY(), center.getZ() - radius);
		Vec3i area = new Vec3i(radius * 2 + 1, height, radius * 2 + 1);

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		for (int x = start.getX(); x < start.getX() + area.getX(); x++) {
			for (int y = start.getY() + area.getY() - 1; y >= start.getY(); y--) { // generating top-down is faster for lighting calculations
				for (int z = start.getZ(); z < start.getZ() + area.getZ(); z++) {

					if (rand.nextFloat() > chance) {
						continue;
					}

					double distance = mutablePos.set(x, y, z).distToLowCornerSqr(center.getX(), y, center.getZ());
					if ((radius - width - 0.01) * (radius - width - 0.01) < distance && distance <= (radius + 0.01) * (radius + 0.01)) {
						if (addBlock(world, mutablePos, block, replace)) {
							contour.addLeaf(mutablePos);
						}
					}
				}
			}
		}
	}

	public static void generateSphereFromTreeStartPos(LevelAccessor world, BlockPos startPos, int girth, int radius, ITreeBlockType block, EnumReplaceMode replace, TreeContour contour) {
		generateSphere(world, startPos.offset(girth / 2, 0, girth / 2), radius, block, replace, contour);
	}

	public static void generateSphere(LevelAccessor world, BlockPos center, int radius, ITreeBlockType block, EnumReplaceMode replace, TreeContour contour) {
		Vec3i start = new Vec3i(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
		Vec3i area = new Vec3i(radius * 2 + 1, radius * 2 + 1, radius * 2 + 1);
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		for (int x = start.getX(); x < start.getX() + area.getX(); x++) {
			for (int y = start.getY() + area.getY() - 1; y >= start.getY(); y--) { // generating top-down is faster for lighting calculations
				for (int z = start.getZ(); z < start.getZ() + area.getZ(); z++) {
					//center.getDistance(x, y, z) <= radius + 0.01
					if (center.closerThan(mutablePos.set(x, y, z), radius + 0.01)) {
						if (addBlock(world, mutablePos, block, replace)) {
							contour.addLeaf(mutablePos);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns a list of trunk top coordinates
	 */
	public static Set<BlockPos> generateTreeTrunk(
		LevelAccessor world,
		RandomSource rand,
		ITreeBlockType wood,
		BlockPos startPos,
		int height,
		int girth,
		int yStart,
		float vinesChance,
		@Nullable Direction leanDirection,
		float leanScale
	) {
		Set<BlockPos> treeTops = new HashSet<>();

		final int leanStartY = (int) Math.floor(height * 0.33f);
		int prevXOffset = 0;
		int prevZOffset = 0;

		int leanX = 0;
		int leanZ = 0;

		if (leanDirection != null) {
			leanX = leanDirection.getStepX();
			leanZ = leanDirection.getStepZ();
		}

		for (int x = 0; x < girth; x++) {
			for (int z = 0; z < girth; z++) {
				for (int y = height - 1; y >= yStart; y--) { // generating top-down is faster for lighting calculations
					float lean;
					if (y < leanStartY) {
						lean = 0;
					} else {
						lean = leanScale * (y - leanStartY) / (height - leanStartY);
					}
					int xOffset = (int) Math.floor(leanX * lean);
					int zOffset = (int) Math.floor(leanZ * lean);

					if (xOffset != prevXOffset || zOffset != prevZOffset) {
						prevXOffset = xOffset;
						prevZOffset = zOffset;
						if (y > 0) {
							if (leanDirection != null) {
								wood.setDirection(leanDirection);
							}
							addBlock(world, startPos.offset(x + xOffset, y - 1, z + zOffset), wood, EnumReplaceMode.ALL);
							wood.setDirection(Direction.UP);
						}
					}

					BlockPos pos = startPos.offset(x + xOffset, y, z + zOffset);
					addBlock(world, pos, wood, EnumReplaceMode.ALL);
					addVines(world, rand, pos, vinesChance);

					if (y + 1 == height) {
						treeTops.add(pos);
					}
				}
			}
		}

		return treeTops;
	}

	protected static void addVines(LevelAccessor world, RandomSource rand, BlockPos pos, float chance) {
		if (chance <= 0) {
			return;
		}

		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true);
			addBlock(world, pos.west(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true);
			addBlock(world, pos.east(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true);
			addBlock(world, pos.north(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
		if (rand.nextFloat() < chance) {
			BlockState blockState = Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true);
			addBlock(world, pos.south(), new TreeBlockType(blockState), EnumReplaceMode.AIR);
		}
	}

	public static void generatePods(ITreeGenData tree, LevelAccessor world, RandomSource rand, BlockPos startPos, int height, int minHeight, int girth, EnumReplaceMode replaceMode) {
		for (int y = height - 1; y >= minHeight; y--) { // generating top-down is faster for lighting calculations
			for (int x = 0; x < girth; x++) {
				for (int z = 0; z < girth; z++) {

					if (x > 0 && x < girth && z > 0 && z < girth) {
						continue;
					}

					trySpawnFruitBlock(tree, world, rand, startPos.offset(x + 1, y, z), replaceMode);
					trySpawnFruitBlock(tree, world, rand, startPos.offset(x - 1, y, z), replaceMode);
					trySpawnFruitBlock(tree, world, rand, startPos.offset(x, y, z + 1), replaceMode);
					trySpawnFruitBlock(tree, world, rand, startPos.offset(x, y, z - 1), replaceMode);
				}
			}
		}
	}

	private static void trySpawnFruitBlock(ITreeGenData tree, LevelAccessor world, RandomSource rand, BlockPos pos, EnumReplaceMode replaceMode) {
		BlockState blockState = world.getBlockState(pos);
		if (replaceMode.canReplace(blockState, world, pos)) {
			tree.trySpawnFruitBlock(world, rand, pos);
		}
	}

	public static void generateSupportStems(ITreeBlockType wood, LevelAccessor world, RandomSource rand, BlockPos startPos, int height, int girth, float chance, float maxHeight) {

		final int min = -1;

		for (int x = min; x <= girth; x++) {
			for (int z = min; z <= girth; z++) {

				// skip the corners, support stems should touch the body of the trunk
				if ((x == min && z == min) || (x == girth && z == girth) || (x == min && z == girth) || (x == girth && z == min)) {
					continue;
				}

				int stemHeight = rand.nextInt(Math.round(height * maxHeight));
				if (rand.nextFloat() < chance) {
					for (int y = 0; y < stemHeight; y++) {
						addBlock(world, startPos.offset(x, y, z), wood, EnumReplaceMode.SOFT);
					}
				}
			}
		}
	}

	public static Set<BlockPos> generateBranches(final LevelAccessor world, final RandomSource rand, final ITreeBlockType wood, final BlockPos startPos, final int girth, final float spreadY, final float spreadXZ, int radius, final int count, final float chance) {
		Set<BlockPos> branchEnds = new HashSet<>();
		if (radius < 1) {
			radius = 1;
		}

		for (final Direction branchDirection : Direction.Plane.HORIZONTAL) {
			wood.setDirection(branchDirection);

			BlockPos branchStart = startPos;

			int offsetX = branchDirection.getStepX();
			int offsetZ = branchDirection.getStepZ();
			if (offsetX > 0) {
				branchStart = branchStart.offset(girth - 1, 0, 0);
			}
			if (offsetZ > 0) {
				branchStart = branchStart.offset(0, 0, girth - 1);
			}

			for (int i = 0; i < count; i++) {
				if (rand.nextFloat() > chance) {
					continue;
				}
				int y = 0;
				int x = 0;
				int z = 0;

				BlockPos branchEnd = null;
				for (int r = 0; r < radius; r++) {
					if (rand.nextFloat() < spreadY) {
						// make branches only spread up, not down
						y++;
						wood.setDirection(Direction.UP);
					} else {
						if (rand.nextFloat() < spreadXZ) {
							if (branchDirection.getAxis() == Direction.Axis.Z) {
								if (rand.nextBoolean()) {
									x++;
								} else {
									x--;
								}
								wood.setDirection(Direction.EAST);
							} else if (branchDirection.getAxis() == Direction.Axis.X) {
								if (rand.nextBoolean()) {
									z++;
								} else {
									z--;
								}
								wood.setDirection(Direction.SOUTH);
							}
						} else {
							x += offsetX;
							z += offsetZ;
							wood.setDirection(branchDirection);
						}
					}

					BlockPos pos = branchStart.offset(x, y, z);
					if (addBlock(world, pos, wood, EnumReplaceMode.SOFT)) {
						branchEnd = pos;
					} else {
						break;
					}
				}

				if (branchEnd != null) {
					branchEnds.add(branchEnd);
				}
			}
		}

		return branchEnds;
	}

	public enum EnumReplaceMode {
		AIR {
			@Override
			public boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
				return world.isEmptyBlock(pos);
			}
		},
		ALL {
			@Override
			public boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
				return true;
			}
		},
		SOFT {
			@Override
			public boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos) {
				if (world instanceof Level) {
					BlockPlaceContext context = new DirectionalPlaceContext((Level) world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP);
					return blockState.canBeReplaced(context);
				}
				return blockState.getMaterial().isReplaceable();
			}
		};

		public abstract boolean canReplace(BlockState blockState, LevelAccessor world, BlockPos pos);
	}

	public static class DirectionHelper {

		public static final Direction[] VALUES = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

		public static Direction getRandom(RandomSource random) {
			return VALUES[random.nextInt(VALUES.length)];
		}

		public static Direction getRandomOther(RandomSource random, Direction direction) {
			List<Direction> directions = Arrays.asList(VALUES);
			directions.remove(direction);
			int size = directions.size();
			return directions.toArray(new Direction[size])[random.nextInt(size)];
		}
	}
}
