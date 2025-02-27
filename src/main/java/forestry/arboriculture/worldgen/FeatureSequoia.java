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
package forestry.arboriculture.worldgen;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

public class FeatureSequoia extends FeatureTree {

	public FeatureSequoia(ITreeGenData tree) {
		this(tree, 20, 5);
	}

	protected FeatureSequoia(ITreeGenData tree, int baseHeight, int heightVariation) {
		super(tree, baseHeight, heightVariation);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor world, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(world, rand, wood, startPos, height, girth, 0, 0, null, 0);
		FeatureHelper.generateSupportStems(wood, world, rand, startPos, height, girth, 0.4f, 0.4f);

		int topHeight = height / 3 + rand.nextInt(height / 6);

		Set<BlockPos> branchCoords = new HashSet<>();
		for (int yBranch = topHeight; yBranch < height; yBranch++) {
			int branchLength = Math.round(height - yBranch) / 2;
			if (branchLength > 4) {
				branchLength = 4;
			}
			branchCoords.addAll(FeatureHelper.generateBranches(world, rand, wood, startPos.offset(0, yBranch, 0), girth, 0.05f, 0.25f, branchLength, 1, 0.5f));
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor world, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(world, leaf, branchEnd, 1.0f + girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height + 2;

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 1 + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 1 + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int topHeight = height / 3 + rand.nextInt(height / 6);
		while (leafSpawn > topHeight) {
			FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 1 + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn, 0), girth, girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
