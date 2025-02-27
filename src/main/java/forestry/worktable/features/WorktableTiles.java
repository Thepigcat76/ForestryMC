package forestry.worktable.features;

import java.util.List;

import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureTileType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.worktable.ModuleWorktable;
import forestry.worktable.tiles.WorktableTile;

@FeatureProvider
public class WorktableTiles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ModuleWorktable.class);

	public static final FeatureTileType<WorktableTile> WORKTABLE = REGISTRY.tile(WorktableTile::new, "worktable", () -> List.of(WorktableBlocks.WORKTABLE.block()));
}
