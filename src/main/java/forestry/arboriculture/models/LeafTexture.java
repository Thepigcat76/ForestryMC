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
package forestry.arboriculture.models;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;

import forestry.api.arboriculture.EnumLeafType;
import forestry.core.config.Constants;

public record LeafTexture(ResourceLocation fast, ResourceLocation fancy, ResourceLocation pollinatedFast, ResourceLocation pollinatedFancy) {
	private static final Map<EnumLeafType, LeafTexture> leafTextures = new EnumMap<>(EnumLeafType.class);

	static {
		for (EnumLeafType leafType : EnumLeafType.values()) {
			leafTextures.put(leafType, LeafTexture.create(leafType));
		}
	}

	public static LeafTexture get(EnumLeafType leafType) {
		return leafTextures.get(leafType);
	}

	public static void registerAllSprites(TextureStitchEvent.Pre event) {
		for (LeafTexture leafTexture : leafTextures.values()) {
			leafTexture.registerSprites(event);
		}
	}

	private static LeafTexture create(EnumLeafType enumLeafType) {
		String id = enumLeafType.toString().toLowerCase(Locale.ENGLISH);

		return new LeafTexture(
				new ResourceLocation(Constants.MOD_ID, "block/leaves/" + id + "_fast"),
				new ResourceLocation(Constants.MOD_ID, "block/leaves/" + id),
				new ResourceLocation(Constants.MOD_ID, "block/leaves/" + id + "_pollinated_fast"),
				new ResourceLocation(Constants.MOD_ID, "block/leaves/" + id + "_pollinated")
		);
	}

	private void registerSprites(TextureStitchEvent.Pre event) {
		event.addSprite(fast);
		event.addSprite(fancy);
		event.addSprite(pollinatedFast);
		event.addSprite(pollinatedFancy);
	}

	public ResourceLocation getSprite(boolean pollinated, boolean fancy) {
		if (pollinated) {
			return fancy ? this.pollinatedFancy : this.pollinatedFast;
		} else {
			return fancy ? this.fancy : this.fast;
		}
	}
}