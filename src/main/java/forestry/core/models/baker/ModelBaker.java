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
package forestry.core.models.baker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.core.config.Constants;
import forestry.core.models.ClientManager;
import forestry.core.utils.ResourceUtil;


/**
 * A model baker to make custom block models with more than one texture layer.
 */
//Todo: Test if baker can be replaced with model loaders
@OnlyIn(Dist.CLIENT)
public final class ModelBaker {

	private static final ResourceLocation FACE_LOCATION = new ResourceLocation(Constants.MOD_ID, "baker_face");
	private static final float[] UVS = new float[]{0.0F, 0.0F, 16.0F, 16.0F, 0.0F, 0.0F, 16.0F, 16.0F};
	private static final FaceBakery FACE_BAKERY = new FaceBakery();
	private static final Vector3f POS_FROM = new Vector3f(0.0F, 0.0F, 0.0F);
	private static final Vector3f POS_TO = new Vector3f(16.0F, 16.0F, 16.0F);

	private final List<ModelBakerFace> faces = new ArrayList<>();

	private final ModelBakerModel currentModel = new ModelBakerModel(ClientManager.INSTANCE.getDefaultBlockState());

	private int colorIndex = -1;

	public ModelBaker addBlockModel(TextureAtlasSprite[] textures, int colorIndex) {
		this.colorIndex = colorIndex;

		for (Direction facing : Direction.VALUES) {
			addFace(facing, textures[facing.ordinal()]);
		}
		return this;
	}

	public ModelBaker addBlockModel(TextureAtlasSprite texture, int colorIndex) {
		return addBlockModel(new TextureAtlasSprite[]{texture, texture, texture, texture, texture, texture}, colorIndex);
	}

	public ModelBaker addFace(Direction facing, TextureAtlasSprite sprite) {
		if (sprite != ResourceUtil.getMissingTexture()) {
			faces.add(new ModelBakerFace(facing, colorIndex, sprite));
		}
		return this;
	}

	public ModelBakerModel bake(boolean flip) {
		BlockModelRotation modelRotation = BlockModelRotation.X0_Y0;

		if (flip) {
			modelRotation = BlockModelRotation.X0_Y180;
		}

		for (ModelBakerFace face : faces) {
			Direction facing = face.face;
			BlockFaceUV uvFace = new BlockFaceUV(UVS, 0);
			BlockElementFace partFace = new BlockElementFace(facing, face.colorIndex, "", uvFace);
			BakedQuad quad = FACE_BAKERY.bakeQuad(POS_FROM, POS_TO, partFace, face.spite, facing, modelRotation, null, true, FACE_LOCATION);

			currentModel.addQuad(facing, quad);
		}

		return currentModel;
	}

	public void setParticleSprite(TextureAtlasSprite particleSprite) {
		currentModel.setParticleSprite(particleSprite);
	}
}
