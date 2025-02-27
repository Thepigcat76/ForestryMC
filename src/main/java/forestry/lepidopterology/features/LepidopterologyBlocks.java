package forestry.lepidopterology.features;

import forestry.core.blocks.BlockBase;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import forestry.api.core.ItemGroups;
import forestry.core.items.ItemBlockBase;
import forestry.lepidopterology.ModuleLepidopterology;
import forestry.lepidopterology.blocks.BlockCocoon;
import forestry.lepidopterology.blocks.BlockSolidCocoon;
import forestry.lepidopterology.blocks.BlockTypeLepidopterologyTesr;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

@FeatureProvider
public class LepidopterologyBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ModuleLepidopterology.class);

	public static final FeatureBlock<BlockBase<BlockTypeLepidopterologyTesr>, ItemBlockBase<BlockBase<BlockTypeLepidopterologyTesr>>> BUTTERFLY_CHEST = REGISTRY.block(() -> new BlockBase<>(BlockTypeLepidopterologyTesr.LEPICHEST, Block.Properties.of(Material.WOOD).sound(SoundType.WOOD)), (block) -> new ItemBlockBase<>(block, new Item.Properties().tab(ItemGroups.tabLepidopterology), BlockTypeLepidopterologyTesr.LEPICHEST), "butterfly_chest");
	// used only in world generation
	public static final FeatureBlock<BlockCocoon, BlockItem> COCOON = REGISTRY.block(BlockCocoon::new, "cocoon");
	public static final FeatureBlock<BlockSolidCocoon, BlockItem> COCOON_SOLID = REGISTRY.block(BlockSolidCocoon::new, "cocoon_solid");
}
