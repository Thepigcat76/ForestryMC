/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.farming;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface IFarmRegistry {

	/**
	 * Registers farming logic in registry
	 *
	 * @since Forestry 5.8
	 */
	IFarmProperties registerLogic(String identifier, IFarmProperties farmInstance);

	/**
	 * Registers farming logic in registry under given identifier
	 *
	 * @param identifier           Valid identifiers: farmArboreal farmCrops farmGourd farmInfernal farmPoales farmSucculentes farmShroom
	 * @param logicFactory         factory that creates the corresponding instance of logic
	 * @param farmablesIdentifiers Identifiers: farmArboreal farmCrops farmGourd farmInfernal farmPoales farmSucculentes farmShroom
	 */
	IFarmPropertiesBuilder getPropertiesBuilder(String identifier);

	/**
	 * Can be used to add IFarmables to some of the vanilla farm logics.
	 * <p>
	 * Identifiers: farmArboreal farmCrops farmGourd farmInfernal farmPoales farmSucculentes farmShroom
	 */
	void registerFarmables(String identifier, IFarmable... farmable);

	Collection<IFarmable> getFarmables(String identifier);

	IFarmableInfo getFarmableInfo(String identifier);

	/**
	 * @param fertilizer the fertilizer
	 * @param value      The value of the fertilizer. The value of the forestry fertilizer is 500.
	 */
	void registerFertilizer(Ingredient fertilizer, int value);

	/**
	 * @return The value of the fertilizer
	 */
	int getFertilizeValue(ItemStack stack);


	/**
	 * @since Forestry 5.8
	 */
	@Nullable
	IFarmProperties getProperties(String identifier);

}
