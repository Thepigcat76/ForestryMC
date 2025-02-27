package forestry.core.data.builder;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import deleteme.RegistryNameFinder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.recipes.IFermenterRecipe;
import forestry.factory.recipes.RecipeSerializers;

public class FermenterRecipeBuilder {

	private Ingredient resource;
	private int fermentationValue;
	private float modifier;
	private Fluid output;
	private FluidStack fluidResource;

	public FermenterRecipeBuilder setResource(Ingredient resource) {
		this.resource = resource;
		return this;
	}

	public FermenterRecipeBuilder setFermentationValue(int fermentationValue) {
		this.fermentationValue = fermentationValue;
		return this;
	}

	public FermenterRecipeBuilder setModifier(float modifier) {
		this.modifier = modifier;
		return this;
	}

	public FermenterRecipeBuilder setOutput(Fluid output) {
		this.output = output;
		return this;
	}

	public FermenterRecipeBuilder setFluidResource(FluidStack fluidResource) {
		this.fluidResource = fluidResource;
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		consumer.accept(new Result(id, resource, fermentationValue, modifier, output, fluidResource));
	}

	private static class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final Ingredient resource;
		private final int fermentationValue;
		private final float modifier;
		private final Fluid output;
		private final FluidStack fluidResource;

		public Result(ResourceLocation id, Ingredient resource, int fermentationValue, float modifier, Fluid output, FluidStack fluidResource) {
			this.id = id;
			this.resource = resource;
			this.fermentationValue = fermentationValue;
			this.modifier = modifier;
			this.output = output;
			this.fluidResource = fluidResource;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("resource", resource.toJson());
			json.addProperty("fermentationValue", fermentationValue);
			json.addProperty("modifier", modifier);
			json.addProperty("output", RegistryNameFinder.getRegistryName(output).toString());
			json.add("fluidResource", RecipeSerializers.serializeFluid(fluidResource));
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return IFermenterRecipe.Companion.SERIALIZER;
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}
}
