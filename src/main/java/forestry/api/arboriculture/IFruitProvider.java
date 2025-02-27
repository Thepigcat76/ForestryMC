/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;

import forestry.api.arboriculture.genetics.ITreeRoot;
import forestry.api.arboriculture.genetics.TreeChromosomes;
import forestry.api.core.ISetupListener;
import forestry.api.genetics.IFruitFamily;
import forestry.api.genetics.products.IProductList;

import genetics.api.individual.IGenome;

/**
 * Provides all information that is needed to spawn a fruit leaves / pod block in the world.
 */
public interface IFruitProvider extends ISetupListener {
	/**
	 * @return The fruit family of this fruit.
	 */
	IFruitFamily getFamily();

	/**
	 * Returns the color of the fruit spite based on the ripening time of the fruit.
	 *
	 * @param genome       The genome of the tree of the pod / leaves block.
	 * @param ripeningTime The ripening time of the leaves / pod block. From 0 to {@link #getRipeningPeriod()}.
	 */
	int getColour(IGenome genome, BlockGetter world, BlockPos pos, int ripeningTime);

	/**
	 * return the color to use for decorative leaves. Usually the ripe color.
	 */
	int getDecorativeColor();

	/**
	 * Determines if fruit block of this provider is considered a leaf block.
	 *
	 * @param genome The genome of the tree of the pod / leaves block.
	 * @param world  The world in that the pod / leaves block is located.
	 * @param pos    The position of the pod / leaves block.
	 * @return True if this provider provides a fruit leaf for the given genome at the given position.
	 */
	boolean isFruitLeaf(IGenome genome, LevelAccessor world, BlockPos pos);

	/**
	 * The chance that this leaves contains fruits or the chance that a pod block spawns.
	 *
	 * @param genome The genome of the tree of the pod / leaves block.
	 * @param world
	 * @return The chance that this leaves contains fruits or the chance that a pod block spawns.
	 */
	default float getFruitChance(IGenome genome, LevelAccessor world, BlockPos pos) {
		ITreeRoot treeRoot = TreeManager.treeRoot;
		if (treeRoot == null) {
			return 0.0F;
		}
		float yieldModifier = treeRoot.getTreekeepingMode(world).getYieldModifier(genome, 1.0F);
		return genome.getActiveValue(TreeChromosomes.YIELD) * yieldModifier * 2.5F;
	}

	/**
	 * @return How many successful ripening block ticks a fruit needs to be ripe.
	 */
	int getRipeningPeriod();

	/**
	 * A unmodifiable list that contains all products and their associated drop chances.
	 *
	 * @return A unmodifiable list that contains all products and their associated drop chances.
	 */
	IProductList getProducts();

	/**
	 * A unmodifiable list that contains all specialties and their associated drop chances.
	 *
	 * @return A unmodifiable list that contains all products and their associated drop chances.
	 */
	IProductList getSpecialty();

	/**
	 * Returns all drops of this block if you harvest it.
	 *
	 * @param genome       The genome of the tree of the leaves / pod.
	 * @param ripeningTime The repining time of the block. From 0 to {@link #getRipeningPeriod()}.
	 */
	NonNullList<ItemStack> getFruits(IGenome genome, Level world, BlockPos pos, int ripeningTime);

	/**
	 * @return Short, human-readable identifier used in the treealyzer.
	 */
	MutableComponent getDescription();

	/**
	 * @return The location of the pod model in the "modid:pods/" folder.
	 */
	@Nullable
	String getModelName();

	/**
	 * @return The mod id of that adds this fruit provider. Needed for the allele of this fruit.
	 */
	String getModID();

	/* TEXTURE OVERLAY */

	/**
	 * @param ripeningTime Elapsed ripening time for the fruit.
	 * @return ResourceLocation of the texture to overlay on the leaf block.
	 */
	@Nullable
	ResourceLocation getSprite(IGenome genome, BlockGetter world, BlockPos pos, int ripeningTime);

	/**
	 * return the ResourceLocation to display on decorative leaves
	 */
	@Nullable
	ResourceLocation getDecorativeSprite();

	/**
	 * @return true if this fruit provider requires fruit blocks to spawn, false otherwise.
	 */
	boolean requiresFruitBlocks();

	/**
	 * Tries to spawn a fruit block at the potential position when the tree generates.
	 * Spawning a fruit has a random chance of success based on {@link TreeChromosomes#SAPPINESS}
	 *
	 * @return true if a fruit block was spawned, false otherwise.
	 */
	boolean trySpawnFruitBlock(IGenome genome, LevelAccessor world, RandomSource rand, BlockPos pos);

	/**
	 * Can be used to register the sprite/s that can be returned with
	 * {@link #getSprite(IGenome, IBlockReader, BlockPos, int)}.
	 *
	 * @param event
	 */
	@OnlyIn(Dist.CLIENT)
	void registerSprites(TextureStitchEvent.Pre event);

	/**
	 * Tag for the log that the is placed on
	 */
	default TagKey<Block> getLogTag() {
		return BlockTags.JUNGLE_LOGS;
	}
}
