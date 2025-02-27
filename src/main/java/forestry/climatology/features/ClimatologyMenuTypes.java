package forestry.climatology.features;


import forestry.climatology.ModuleClimatology;
import forestry.climatology.gui.ContainerHabitatFormer;
import forestry.modules.features.FeatureMenuType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class ClimatologyMenuTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ModuleClimatology.class);

	public static final FeatureMenuType<ContainerHabitatFormer> HABITAT_FORMER = REGISTRY.menuType(ContainerHabitatFormer::fromNetwork, "habitat_former");

}
