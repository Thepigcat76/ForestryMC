package forestry.modules;

import java.util.function.Consumer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import forestry.core.utils.ForgeUtils;

@OnlyIn(Dist.CLIENT)
public class ClientModuleHandler extends CommonModuleHandler {
	public ClientModuleHandler() {
		ForgeUtils.modBus().register(this);
	}

	@SubscribeEvent
	public void registerSprites(TextureStitchEvent.Pre event) {
		modules.forEach((module -> actOnHandler(module, (handler) -> handler.registerSprites(event))));
	}

	@SubscribeEvent
	public void handleSprites(TextureStitchEvent.Post event) {
		modules.forEach((module -> actOnHandler(module, (handler) -> handler.handleSprites(event))));
	}

	@SubscribeEvent
	public void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		modules.forEach(module -> actOnHandler(module, handler -> handler.registerAdditionalModels(event)));
	}

	@SubscribeEvent
	public void bakeModels(ModelEvent.BakingCompleted event) {
		modules.forEach((module -> actOnHandler(module, (handler) -> handler.bakeModels(event))));
	}

	@SubscribeEvent
	public void registerReloadListeners(RegisterClientReloadListenersEvent event) {
		modules.forEach((module -> actOnHandler(module, (handler) -> handler.registerReloadListeners(event))));
	}

	@SubscribeEvent
	public void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		modules.forEach((module -> actOnHandler(module, (handler) -> handler.registerModelLoaders(event))));
	}

	@SubscribeEvent
	public void setupClient(FMLClientSetupEvent event) {
		modules.forEach((module -> actOnHandler(module, (handler) -> handler.setupClient(event))));
	}

	@SubscribeEvent
	public void setupLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		modules.forEach(module -> actOnHandler(module, handler -> handler.setupLayers(event)));
	}

	@SubscribeEvent
	public void setupRenderers(EntityRenderersEvent.RegisterRenderers event) {
		modules.forEach(module -> actOnHandler(module, handler -> handler.setupRenderers(event)));
	}

	@SubscribeEvent
	public void registerBlockColors(RegisterColorHandlersEvent.Block event) {
		modules.forEach(module -> actOnHandler(module, handler -> handler.registerBlockColors(event)));
	}

	@SubscribeEvent
	public void registerItemColors(RegisterColorHandlersEvent.Item event) {
		modules.forEach(module -> actOnHandler(module, handler -> handler.registerItemColors(event)));
	}

	private void actOnHandler(BlankForestryModule module, Consumer<IClientModuleHandler> actor) {
		if (module.getModuleHandler() instanceof IClientModuleHandler clientHandler) {
			actor.accept(clientHandler);
		}
	}
}
