/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.lepidopterology;

import javax.annotation.Nullable;

import forestry.lepidopterology.genetics.ButterflyRoot;
import genetics.api.GeneticsAPI;
import genetics.api.root.IRootDefinition;

import forestry.api.lepidopterology.genetics.IButterflyFactory;
import forestry.api.lepidopterology.genetics.IButterflyMutationFactory;
import forestry.api.lepidopterology.genetics.IButterflyRoot;

public class ButterflyManager {

	/**
	 * Convenient access to AlleleManager.alleleRegistry.getSpeciesRoot("rootButterflies")
	 *
	 * @implNote Only null if the "lepidopterology" module is not enabled.
	 */
	public static IButterflyRoot butterflyRoot;

	/**
	 * Used to create new butterflies.
	 *
	 * @implNote Only null if the "lepidopterology" module is not enabled.
	 */
	@Nullable
	public static IButterflyFactory butterflyFactory;

	/**
	 * Used to create new butterfly mutations.
	 *
	 * @implNote Only null if the "lepidopterology" module is not enabled.
	 */
	@Nullable
	public static IButterflyMutationFactory butterflyMutationFactory;
}
