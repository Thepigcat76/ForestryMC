/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ObjectHolder;

public interface IFabricatorRecipe extends IForestryRecipe {

	RecipeType<IFabricatorRecipe> TYPE = RecipeManagers.create("forestry:fabricator");

	class Companion {
		@ObjectHolder(registryName = "recipe_serializer", value = "forestry:fabricator")
		public static final RecipeSerializer<IFabricatorRecipe> SERIALIZER = null;
	}

	/**
	 * @return the molten liquid (and amount) required for this recipe.
	 */
	FluidStack getLiquid();

	/**
	 * @return the plan for this recipe (the item in the top right slot)
	 */
	Ingredient getPlan();

	/**
	 * @return the crafting grid recipe. The crafting recipe's getRecipeOutput() is used as the IFabricatorRecipe's output.
	 */
	ShapedRecipe getCraftingGridRecipe();

	@Override
	default RecipeType<?> getType() {
		return TYPE;
	}

	@Override
	default RecipeSerializer<?> getSerializer() {
		return Companion.SERIALIZER;
	}
}
