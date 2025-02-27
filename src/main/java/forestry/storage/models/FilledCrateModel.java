package forestry.storage.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;

import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import forestry.core.config.Constants;

public class FilledCrateModel implements IUnbakedGeometry<FilledCrateModel> {
	@Nullable
	public static BakedModel cachedBaseModel = null;
	@Nullable
	public static ItemTransforms transforms = null;
	@Nullable
	public static TextureAtlasSprite particle = null;

	private final BlockModel contents;
	private final List<Material> materials;

	public FilledCrateModel(BlockModel contents, List<Material> materials) {
		this.contents = contents;
		this.materials = materials;
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		if (cachedBaseModel == null) {
			cachedBaseModel = bakery.getModel(Loader.FILLED_CRATE_LOCATION).bake(bakery, spriteGetter, modelState, modelLocation);
		}

		return new Baked(contents.bake(bakery, spriteGetter, modelState, modelLocation), cachedBaseModel);
	}

	@Override
	public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return materials;
	}

	public enum Loader implements IGeometryLoader<FilledCrateModel> {
		INSTANCE;

		private static final ResourceLocation FILLED_CRATE_LOCATION = new ResourceLocation(Constants.MOD_ID, "item/filled_crate");

		@Override
		public FilledCrateModel read(JsonObject json, JsonDeserializationContext ctx) {
			List<BlockElement> elements = new ArrayList<>();
			Map<String, Either<Material, String>> textureMap = new HashMap<>();
			ArrayList<Material> materials = new ArrayList<>();

			// vanilla models support layer0 to layer4; layer0 is implied to be the crate
			for (int layer = 1; layer <= 4; ++layer) {
				if (json.has("textures") && json.get("textures").getAsJsonObject().has("layer" + layer)) {
					elements.add(make2dElement(layer, 3f, 4f, 11f, 12f, 0.002f));
					// add material
					ResourceLocation contentsTexture = ResourceLocation.tryParse(GsonHelper.getAsString(GsonHelper.getAsJsonObject(json, "textures"), "layer" + layer));
					//noinspection DataFlowIssue
					Material contentsMaterial = new Material(TextureAtlas.LOCATION_BLOCKS, contentsTexture);
					textureMap.put("layer" + layer, Either.left(contentsMaterial));
					materials.add(contentsMaterial);
				}
			}
			BlockModel contents = new BlockModel(FILLED_CRATE_LOCATION, elements, textureMap, false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, List.of());
			materials.trimToSize();

			return new FilledCrateModel(contents, materials);
		}
	}

	public static BlockElement make2dElement(int layer, float startX, float startY, float endX, float endY, float zOffset) {
		Map<Direction, BlockElementFace> faces = Map.of(
				Direction.SOUTH, new BlockElementFace(null, layer, "layer" + layer, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
				Direction.NORTH, new BlockElementFace(null, layer, "layer" + layer, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0))
		);
		// front and back 2d faces
		// add the element, scaled down
		return new BlockElement(new Vector3f(startX, startY, 7.5f - zOffset), new Vector3f(endX, endY, 8.5f + zOffset), faces, null, false);
	}

	private static class Baked implements BakedModel {
		private final ItemTransforms transforms;
		private final TextureAtlasSprite particle;
		private final List<BakedQuad> quads;
		private final List<BakedModel> passes;

		private Baked(BakedModel bakedContents, BakedModel baseModel) {
			this.quads = List.copyOf(bakedContents.getQuads(null, null, RandomSource.create()));
			this.transforms = baseModel.getTransforms();
			this.particle = bakedContents.getParticleIcon();
			this.passes = List.of(baseModel, this);
		}

		@Override
		public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction p_235040_, RandomSource p_235041_) {
			return quads;
		}

		@Override
		public boolean useAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean usesBlockLight() {
			return false;
		}

		@Override
		public boolean isCustomRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return particle;
		}

		@Override
		public ItemOverrides getOverrides() {
			return ItemOverrides.EMPTY;
		}

		@Override
		public ItemTransforms getTransforms() {
			return transforms;
		}

		@Override
		public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
			return passes;
		}
	}
}
