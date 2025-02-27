package forestry.modules;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.minecraftforge.registries.RegisterEvent;

import net.minecraftforge.fml.InterModComms;

import forestry.Forestry;
import forestry.api.modules.IForestryModule;
import forestry.core.IPickupHandler;
import forestry.core.IResupplyHandler;
import forestry.core.ISaveEventHandler;
import forestry.core.ItemGroupForestry;
import forestry.core.config.Constants;
import forestry.core.network.IPacketRegistry;
import forestry.modules.features.IModFeature;
import forestry.modules.features.ModFeatureRegistry;

//TODO - most of this needs tearing up and replacing
public class CommonModuleHandler {

	// todo remove this, it's useless
	//TODO use toposort for sorting dependancies?
	public enum Stage {
		SETUP, // setup API to make it functional. GameMode Configs are not yet accessible
		SETUP_DISABLED, // setup fallback API to avoid crashes
		REGISTER, // register basic blocks and items
		PRE_INIT, // register handlers, definitions, and anything that depends on basic items
		BACKPACKS_CRATES, // backpacks, crates
		INIT, // anything that depends on PreInit stages, recipe registration
		POST_INIT, // stubborn mod integration, dungeon loot, and finalization of things that take input from mods
		FINISHED
	}

	protected final ModFeatureRegistry registry;
	protected final Set<BlankForestryModule> modules = new LinkedHashSet<>();
	protected final Set<IForestryModule> disabledModules = new LinkedHashSet<>();
	protected Stage stage = Stage.SETUP;

	public CommonModuleHandler() {
		this.registry = ModFeatureRegistry.get(Constants.MOD_ID);
	}

	public void addModules(Collection<IForestryModule> modules, Collection<IForestryModule> disabledModules) {
		if (stage != Stage.SETUP) {
			throw new RuntimeException("Tried to register Modules outside of SETUP");
		}
		for (IForestryModule module : modules) {
			if (!(module instanceof BlankForestryModule)) {
				continue;
			}
			this.modules.add((BlankForestryModule) module);
		}
		this.disabledModules.addAll(disabledModules);
	}

	public Stage getStage() {
		return stage;
	}

	public void runSetup() {
		stage = Stage.SETUP;
		for (IForestryModule module : modules) {
			Forestry.LOGGER.debug("Setup API Start: {}", module);
			module.setupAPI();
			Forestry.LOGGER.debug("Setup API Complete: {}", module);
		}
		stage = Stage.SETUP_DISABLED;
		for (IForestryModule module : disabledModules) {
			Forestry.LOGGER.debug("Disabled-Setup Start: {}", module);
			module.disabledSetupAPI();
			Forestry.LOGGER.debug("Disabled-Setup Complete: {}", module);
		}
		stage = Stage.REGISTER;

	}

	public void createFeatures() {
		ItemGroupForestry.create();
		ForestryPluginUtil.loadFeatureProviders();
	}

	public Collection<IModFeature> getFeatures(ResourceKey<? extends Registry<?>> filter) {
		return registry.getFeatures(filter);
	}

	public void registerObjects(RegisterEvent event) {
		// used for wood kinds and block/item colors
		registry.onRegister(event);
		// does misc object registration, not features
		registerObjects();
	}

	private void registerObjects() {
		for (IForestryModule module : modules) {
			// these are not registry objects, just other data
			module.registerObjects();
		}
	}

	public void registerGuiFactories() {
		for (IForestryModule module : modules) {
			module.registerGuiFactories();
		}
	}

	public void runPreInit() {
		stage = Stage.PRE_INIT;
		for (BlankForestryModule module : modules) {
			Forestry.LOGGER.debug("Pre-Init Start: {}", module);
			registerHandlers(module);
			module.preInit();
			Forestry.LOGGER.debug("Pre-Init Complete: {}", module);
		}
	}

	public void registerCapabilities(Consumer<Class<?>> consumer) {
		for (BlankForestryModule module : modules) {
			module.registerCapabilities(consumer);
		}
	}

	private void registerHandlers(BlankForestryModule module) {
		Forestry.LOGGER.debug("Registering Handlers for Module: {}", module);

		IPickupHandler pickupHandler = module.getPickupHandler();
		if (pickupHandler != null) {
			ModuleManager.pickupHandlers.add(pickupHandler);
		}

		ISaveEventHandler saveHandler = module.getSaveEventHandler();
		if (saveHandler != null) {
			ModuleManager.saveEventHandlers.add(saveHandler);
		}

		IResupplyHandler resupplyHandler = module.getResupplyHandler();
		if (resupplyHandler != null) {
			ModuleManager.resupplyHandlers.add(resupplyHandler);
		}
	}

	public void runInit() {
		stage = Stage.INIT;
		for (IForestryModule module : modules) {
			Forestry.LOGGER.debug("Init Start: {}", module);
			module.doInit();
			module.registerRecipes();
			Forestry.LOGGER.debug("Init Complete: {}", module);
		}
	}

	public void runClientInit() {

	}

	public void runPostInit() {
		stage = Stage.POST_INIT;
		for (IForestryModule module : modules) {
			Forestry.LOGGER.debug("Post-Init Start: {}", module);
			module.postInit();
			Forestry.LOGGER.debug("Post-Init Complete: {}", module);
		}
		stage = Stage.FINISHED;
	}

	public void processIMCMessages(Stream<InterModComms.IMCMessage> messages) {
		messages.forEach(m -> {
			for (BlankForestryModule module : modules) {
				if (module.processIMCMessage(m)) {
					break;
				}
			}
		});
	}
}
