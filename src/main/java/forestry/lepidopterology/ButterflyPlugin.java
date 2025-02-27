package forestry.lepidopterology;

import forestry.api.lepidopterology.genetics.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import com.mojang.authlib.GameProfile;

import forestry.api.genetics.ForestryComponentKeys;
import forestry.api.genetics.IResearchHandler;
import forestry.api.lepidopterology.ButterflyManager;
import forestry.core.config.Constants;
import forestry.core.genetics.alleles.EnumAllele;
import forestry.core.genetics.root.IResearchPlugin;
import forestry.core.genetics.root.ResearchHandler;
import forestry.lepidopterology.features.LepidopterologyItems;
import forestry.lepidopterology.genetics.ButterflyBranchDefinition;
import forestry.lepidopterology.genetics.ButterflyDefinition;
import forestry.lepidopterology.genetics.ButterflyHelper;
import forestry.lepidopterology.genetics.ButterflyRoot;
import forestry.lepidopterology.genetics.MothDefinition;
import forestry.lepidopterology.genetics.alleles.ButterflyAlleles;

import genetics.api.GeneticPlugin;
import genetics.api.IGeneticApiInstance;
import genetics.api.IGeneticFactory;
import genetics.api.IGeneticPlugin;
import genetics.api.alleles.IAlleleRegistry;
import genetics.api.alleles.IAlleleSpecies;
import genetics.api.classification.IClassificationRegistry;
import genetics.api.individual.IIndividual;
import genetics.api.organism.IOrganismTypes;
import genetics.api.root.IGeneticListenerRegistry;
import genetics.api.root.IIndividualRoot;
import genetics.api.root.IIndividualRootBuilder;
import genetics.api.root.IRootManager;
import genetics.api.root.components.ComponentKeys;

@GeneticPlugin(modId = Constants.MOD_ID)
public class ButterflyPlugin implements IGeneticPlugin {
	@Override
	public void registerClassifications(IClassificationRegistry registry) {
		ButterflyBranchDefinition.createClassifications(registry);
	}

	@Override
	public void registerListeners(IGeneticListenerRegistry registry) {
		registry.add(ButterflyRoot.UID, ButterflyDefinition.values());
		registry.add(ButterflyRoot.UID, MothDefinition.values());
	}

	@Override
	public void registerAlleles(IAlleleRegistry registry) {
		registry.registerAlleles(EnumAllele.Size.values(), ButterflyChromosomes.SIZE);
		registry.registerAlleles(EnumAllele.Metabolism.values(), ButterflyChromosomes.METABOLISM);
		ButterflyAlleles.registerAlleles(registry);
	}

	@Override
	public void createRoot(IRootManager rootManager, IGeneticFactory geneticFactory) {
		IIndividualRootBuilder<IButterfly> rootBuilder = rootManager.createRoot(ButterflyRoot.UID);
		rootBuilder
			.setRootFactory(ButterflyRoot::new)
			.setSpeciesType(ButterflyChromosomes.SPECIES)
			.addListener(ComponentKeys.TYPES, (IOrganismTypes<IButterfly> builder) -> {
				builder.registerType(EnumFlutterType.SERUM, LepidopterologyItems.SERUM_GE::stack);
				builder.registerType(EnumFlutterType.CATERPILLAR, LepidopterologyItems.CATERPILLAR_GE::stack);
				builder.registerType(EnumFlutterType.COCOON, LepidopterologyItems.COCOON_GE::stack);
				builder.registerType(EnumFlutterType.BUTTERFLY, LepidopterologyItems.BUTTERFLY_GE::stack);
			})
			.addComponent(ComponentKeys.TRANSLATORS)
			.addComponent(ComponentKeys.MUTATIONS)
			.addComponent(ForestryComponentKeys.RESEARCH, ResearchHandler::new)
			.addListener(ForestryComponentKeys.RESEARCH, (IResearchHandler<IButterfly> component) -> {
				component.addPlugin(new IResearchPlugin() {
					@Override
					public float getResearchSuitability(IAlleleSpecies species, ItemStack itemstack) {
						if (itemstack.isEmpty() || !(species instanceof IAlleleButterflySpecies butterflySpecies)) {
							return -1;
						}

						if (itemstack.getItem() == Items.GLASS_BOTTLE) {
							return 0.9f;
						}

						for (ItemStack stack : butterflySpecies.getButterflyLoot().getPossibleStacks()) {
							if (stack.sameItem(itemstack)) {
								return 1.0f;
							}
						}
						for (ItemStack stack : butterflySpecies.getCaterpillarLoot().getPossibleStacks()) {
							if (stack.sameItem(itemstack)) {
								return 1.0f;
							}
						}
						return -1;
					}

					@Override
					public NonNullList<ItemStack> getResearchBounty(IAlleleSpecies species, Level world, GameProfile researcher, IIndividual individual, int bountyLevel) {
						ItemStack serum = ((IIndividualRoot<IIndividual>) species.getRoot()).getTypes().createStack(individual.copy(), EnumFlutterType.SERUM);
						NonNullList<ItemStack> bounty = NonNullList.create();
						bounty.add(serum);
						return bounty;
					}
				});
			})
			.setDefaultTemplate(ButterflyHelper::createDefaultTemplate);
	}

	@Override
	public void onFinishRegistration(IRootManager manager, IGeneticApiInstance instance) {
		ButterflyManager.butterflyRoot = instance.<IButterflyRoot>getRoot(ButterflyRoot.UID).get();
	}
}
