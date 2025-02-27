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

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

public class FeatureTeak extends FeatureTree {

	public FeatureTeak(ITreeGenData tree) {
		super(tree, 6, 3);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor world, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(world, rand, wood, startPos, height, girth, 0, 0, null, 0);

		int branchWidth = height / 3;

		return FeatureHelper.generateBranches(world, rand, wood, startPos.offset(0, height - 3, 0), girth, 0.2f, 0.5f, branchWidth, 1, 1.0f);
	}

	@Override
	protected void generateLeaves(LevelAccessor world, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		float leafMultiplier = height / 6.0f;
		if (leafMultiplier > 2) {
			leafMultiplier = 2;
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCircle(world, rand, branchEnd, 2, Math.round(3 * leafMultiplier), 2, leaf, 1.0f, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 0.5f * leafMultiplier + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 1.9f * leafMultiplier + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 1.9f * leafMultiplier + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		if (rand.nextBoolean()) {
			FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 1.9f * leafMultiplier + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn, 0), girth, 0.5f * leafMultiplier + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
