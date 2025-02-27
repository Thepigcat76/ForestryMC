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
package forestry.core;

import java.util.Optional;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IForestrySpeciesRoot;

import genetics.api.individual.IIndividual;
import genetics.api.root.IRootDefinition;
import genetics.utils.RootUtils;

public class PickupHandlerCore implements IPickupHandler {

	@Override
	public boolean onItemPickup(Player PlayerEntity, ItemEntity entityitem) {
		ItemStack itemstack = entityitem.getItem();
		if (itemstack.isEmpty()) {
			return false;
		}

		IRootDefinition<IForestrySpeciesRoot<IIndividual>> definition = RootUtils.getRoot(itemstack);
		if (definition.isPresent()) {
			IForestrySpeciesRoot<IIndividual> root = definition.get();
			IIndividual individual = root.create(itemstack);

			if (individual != null) {
				IBreedingTracker tracker = root.getBreedingTracker(entityitem.level, PlayerEntity.getGameProfile());
				tracker.registerPickup(individual);
			}
		}

		return false;
	}

}
