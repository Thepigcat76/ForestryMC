package forestry.core.blocks;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

import forestry.core.tiles.ForestryTicker;
import forestry.core.tiles.TileForestry;
import forestry.modules.features.FeatureTileType;

public class MachineProperties<T extends TileForestry> implements IMachineProperties<T> {
	private static final ISimpleShapeProvider FULL_CUBE = Shapes::block;

	private final String name;
	private final Supplier<FeatureTileType<? extends T>> teType;
	private final IShapeProvider shape;
	@Nullable
	private final ForestryTicker<? extends T> clientTicker;
	@Nullable
	private final ForestryTicker<? extends T> serverTicker;
	@Nullable
	private Block block;

	// todo make this not a supplier because Feature... is already a registry object
	public MachineProperties(Supplier<FeatureTileType<? extends T>> teType, String name, IShapeProvider shape, @Nullable ForestryTicker<? extends T> clientTicker, @Nullable ForestryTicker<? extends T> serverTicker) {
		this.teType = teType;
		this.name = name;
		this.shape = shape;
		this.clientTicker = clientTicker;
		this.serverTicker = serverTicker;
	}

	@Override
	public void setBlock(Block block) {
		this.block = block;
	}

	@Nullable
	@Override
	public Block getBlock() {
		return block;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return shape.getShape(state, reader, pos, context);
	}

	@Override
	public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
		return teType.get().tileType().create(pos, state);
	}

	@Nullable
	@Override
	public ForestryTicker<? extends T> getClientTicker() {
		return clientTicker;
	}

	@Nullable
	@Override
	public ForestryTicker<? extends T> getServerTicker() {
		return serverTicker;
	}

	@Override
	public BlockEntityType<? extends T> getTeType() {
		return teType.get().tileType();
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public static class Builder<T extends TileForestry, B extends Builder<T, ?>> {
		protected final Supplier<FeatureTileType<? extends T>> type;
		protected final String name;
		protected IShapeProvider shape = FULL_CUBE;
		@Nullable
		protected ForestryTicker<? extends T> clientTicker = null;
		@Nullable
		protected ForestryTicker<? extends T> serverTicker = null;

		public Builder(Supplier<FeatureTileType<? extends T>> type, String name) {
			this.type = Preconditions.checkNotNull(type);
			this.name = Preconditions.checkNotNull(name);
		}

		public B setShape(VoxelShape shape) {
			return setShape(() -> shape);
		}

		public B setShape(ISimpleShapeProvider shape) {
			this.shape = shape;
			//noinspection unchecked
			return (B) this;
		}

		public B setShape(IShapeProvider shape) {
			this.shape = shape;
			//noinspection unchecked
			return (B) this;
		}

		public B setClientTicker(@Nullable ForestryTicker<? extends T> clientTicker) {
			this.clientTicker = clientTicker;
			//noinspection unchecked
			return (B) this;
		}

		public B setServerTicker(@Nullable ForestryTicker<? extends T> serverTicker) {
			this.serverTicker = serverTicker;
			//noinspection unchecked
			return (B) this;
		}

		public MachineProperties<T> create() {
			Preconditions.checkNotNull(shape);
			return new MachineProperties<>(type, name, shape, clientTicker, serverTicker);
		}
	}
}
