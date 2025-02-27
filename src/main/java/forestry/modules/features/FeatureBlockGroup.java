package forestry.modules.features;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.core.IBlockSubtype;

public class FeatureBlockGroup<B extends Block, S extends IBlockSubtype> extends FeatureGroup<FeatureBlockGroup.Builder<B, S>, FeatureBlock<B, BlockItem>, S> {

	private FeatureBlockGroup(Builder<B, S> builder) {
		super(builder);
	}

	@Override
	protected FeatureBlock<B, BlockItem> createFeature(Builder<B, S> builder, S type) {
		return builder.registry.block(() -> builder.constructor.apply(type), builder.itemConstructor != null ? (block) -> builder.itemConstructor.apply(block, type) : null, builder.getIdentifier(type));
	}

	public Collection<B> getBlocks() {
		ArrayList<B> blocks = new ArrayList<>(featureByType.size());
		for (FeatureBlock<B, BlockItem> value : featureByType.values()) {
			blocks.add(value.block());
		}
		return blocks;
	}

	public Collection<BlockItem> getItems() {
		ArrayList<BlockItem> items = new ArrayList<>(featureByType.size());
		for (FeatureBlock<B, BlockItem> value : featureByType.values()) {
			items.add(value.item());
		}
		return items;
	}

	@Nullable
	public BlockState findState(String typeName) {
		Optional<FeatureBlock> block = featureByType.entrySet().stream()
				.filter(e -> e.getKey().getSerializedName().equals(typeName))
				.findFirst()
				.flatMap(e -> Optional.of(e.getValue()));
		return block.map(FeatureBlock::defaultState).orElse(null);
	}

	public boolean blockEqual(BlockState state) {
		return getFeatures().stream().anyMatch(f -> f.blockEqual(state));
	}

	public boolean blockEqual(Block block) {
		return getFeatures().stream().anyMatch(f -> f.blockEqual(block));
	}

	public Block[] blockArray() {
		return getBlocks().toArray(new Block[0]);
	}

	public static class Builder<B extends Block, S extends IBlockSubtype> extends FeatureGroup.Builder<S, FeatureBlockGroup<B, S>> {
		private final IFeatureRegistry registry;
		private final Function<S, B> constructor;
		@Nullable
		private BiFunction<B, S, BlockItem> itemConstructor;

		public Builder(IFeatureRegistry registry, Function<S, B> constructor) {
			super(registry);
			this.registry = registry;
			this.constructor = constructor;
		}

		public Builder<B, S> itemWithType(BiFunction<B, S, BlockItem> itemConstructor) {
			this.itemConstructor = itemConstructor;
			return this;
		}

		public Builder<B, S> item(Function<B, BlockItem> itemConstructor) {
			this.itemConstructor = (block, type) -> itemConstructor.apply(block);
			return this;
		}

		@Override
		public FeatureBlockGroup<B, S> create() {
			return new FeatureBlockGroup<>(this);
		}
	}
}
