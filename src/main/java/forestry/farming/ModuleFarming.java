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
package forestry.farming;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

import forestry.api.circuits.ChipsetManager;
import forestry.api.circuits.CircuitSocketType;
import forestry.api.circuits.ICircuitLayout;
import forestry.api.core.ForestryAPI;
import forestry.api.farming.IFarmRegistry;
import forestry.api.modules.ForestryModule;
import forestry.core.ClientsideCode;
import forestry.core.circuits.CircuitLayout;
import forestry.core.config.Constants;
import forestry.core.features.CoreItems;
import forestry.farming.features.FarmingMenuTypes;
import forestry.farming.gui.GuiFarm;
import forestry.farming.logic.ForestryFarmIdentifier;
import forestry.farming.logic.farmables.FarmableAgingCrop;
import forestry.farming.logic.farmables.FarmableChorus;
import forestry.farming.logic.farmables.FarmableGE;
import forestry.farming.logic.farmables.FarmableGourd;
import forestry.farming.logic.farmables.FarmableSapling;
import forestry.farming.logic.farmables.FarmableStacked;
import forestry.farming.proxy.ProxyFarming;
import forestry.modules.BlankForestryModule;
import forestry.modules.ForestryModuleUids;
import forestry.modules.ISidedModuleHandler;

@ForestryModule(modId = Constants.MOD_ID, moduleID = ForestryModuleUids.FARMING, name = "Farming", author = "SirSengir", url = Constants.URL, unlocalizedDescription = "for.module.farming.description")
public class ModuleFarming extends BlankForestryModule {
	private static final ProxyFarming PROXY = FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.newProxyProxyFarming() : new ProxyFarming();

	@Override
	public void setupAPI() {
		ForestryAPI.farmRegistry = FarmRegistry.INSTANCE;
	}

	@Override
	public void disabledSetupAPI() {
		ForestryAPI.farmRegistry = new DummyFarmRegistry();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerGuiFactories() {
		MenuScreens.register(FarmingMenuTypes.FARM.menuType(), GuiFarm::new);
	}

	@Override
	public void preInit() {
		IFarmRegistry registry = ForestryAPI.farmRegistry;
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
				new ItemStack(Blocks.OAK_SAPLING),
				new ItemStack[]{new ItemStack(Items.APPLE), new ItemStack(Items.STICK)}
		));
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
				new ItemStack(Blocks.BIRCH_SAPLING),
				new ItemStack[]{new ItemStack(Items.STICK)}
		));
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
				new ItemStack(Blocks.SPRUCE_SAPLING),
				new ItemStack[]{new ItemStack(Items.STICK)}
		));
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
				new ItemStack(Blocks.JUNGLE_SAPLING),
				new ItemStack[]{new ItemStack(Items.STICK), new ItemStack(Items.COCOA_BEANS)}
		));
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
				new ItemStack(Blocks.DARK_OAK_SAPLING),
				new ItemStack[]{new ItemStack(Items.APPLE), new ItemStack(Items.STICK)}
		));
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
				new ItemStack(Blocks.ACACIA_SAPLING),
				new ItemStack[]{new ItemStack(Items.STICK)}
		));
		// todo 1.20.1
		//registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableSapling(
		//		new ItemStack(Blocks.CHERRY_SAPLING),
		//		new ItemStack[]{new ItemStack(Items.STICK)}
		//));
		registry.registerFarmables(ForestryFarmIdentifier.ARBOREAL, new FarmableGE());

		registry.registerFarmables(ForestryFarmIdentifier.CROPS,
				new FarmableAgingCrop(new ItemStack(Items.WHEAT_SEEDS), Blocks.WHEAT, new ItemStack(Items.WHEAT), CropBlock.AGE, 7, 0),
				new FarmableAgingCrop(new ItemStack(Items.POTATO), Blocks.POTATOES, new ItemStack(Items.POTATO), CropBlock.AGE, 7, 0),
				new FarmableAgingCrop(new ItemStack(Items.CARROT), Blocks.CARROTS, new ItemStack(Items.CARROT), CropBlock.AGE, 7, 0),
				new FarmableAgingCrop(new ItemStack(Items.BEETROOT_SEEDS), Blocks.BEETROOTS, new ItemStack(Items.BEETROOT), BeetrootBlock.AGE, 3, 0));

		/*BlockState plantedBrownMushroom = FarmingBlocks.MUSHROOM.with(BlockMushroom.VARIANT, BlockMushroom.MushroomType.BROWN);
		registry.registerFarmables(ForestryFarmIdentifier.SHROOM, new FarmableVanillaMushroom(new ItemStack(Blocks.BROWN_MUSHROOM), plantedBrownMushroom, Blocks.BROWN_MUSHROOM_BLOCK));

		BlockState plantedRedMushroom = FarmingBlocks.MUSHROOM.with(BlockMushroom.VARIANT, BlockMushroom.MushroomType.RED);
		registry.registerFarmables(ForestryFarmIdentifier.SHROOM, new FarmableVanillaMushroom(new ItemStack(Blocks.RED_MUSHROOM), plantedRedMushroom, Blocks.RED_MUSHROOM_BLOCK));*/

		registry.registerFarmables(ForestryFarmIdentifier.GOURD, new FarmableGourd(new ItemStack(Items.PUMPKIN_SEEDS), Blocks.PUMPKIN_STEM, Blocks.PUMPKIN));
		registry.registerFarmables(ForestryFarmIdentifier.GOURD, new FarmableGourd(new ItemStack(Items.MELON_SEEDS), Blocks.MELON_STEM, Blocks.MELON));

		registry.registerFarmables(ForestryFarmIdentifier.INFERNAL, new FarmableAgingCrop(new ItemStack(Items.NETHER_WART), Blocks.NETHER_WART, NetherWartBlock.AGE, 3));

		registry.registerFarmables(ForestryFarmIdentifier.POALES, new FarmableStacked(new ItemStack(Items.SUGAR_CANE), Blocks.SUGAR_CANE, 3));

		registry.registerFarmables(ForestryFarmIdentifier.SUCCULENTES, new FarmableStacked(new ItemStack(Blocks.CACTUS), Blocks.CACTUS, 3));

		registry.registerFarmables(ForestryFarmIdentifier.ENDER, FarmableChorus.INSTANCE);

		//Forestry fertilizer
		registry.registerFertilizer(Ingredient.of(CoreItems.FERTILIZER_COMPOUND), 500);

		// Layouts
		ICircuitLayout layoutManaged = new CircuitLayout("farms.managed", CircuitSocketType.FARM);
		ChipsetManager.circuitRegistry.registerLayout(layoutManaged);
		ICircuitLayout layoutManual = new CircuitLayout("farms.manual", CircuitSocketType.FARM);
		ChipsetManager.circuitRegistry.registerLayout(layoutManual);
	}

	@Override
	public void doInit() {
		FarmDefinition.init();
	}

	@Override
	public void registerRecipes() {
		FarmDefinition.registerCircuits();
	}

	@Override
	public ISidedModuleHandler getModuleHandler() {
		return PROXY;
	}
}
