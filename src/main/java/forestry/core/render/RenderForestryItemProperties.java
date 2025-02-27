package forestry.core.render;

import forestry.core.items.ItemBlockBase;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;

public class RenderForestryItemProperties implements IClientItemExtensions {
	private final NonNullLazy<RenderForestryItem> renderItem;

	// initializeClient is called during ItemBlockBase super
	// field itemBlock.blockTypeTesr is not initialized until after constructor done
	// lazy has two purposes:
	// - itemBlock.blockTypeTesr filled in after 
	// - getEntityModels not available until later in loading
	@SuppressWarnings("Convert2MethodRef")
	public RenderForestryItemProperties(ItemBlockBase<?> itemBlock) {
		// This must be a lambda, because method reference causes dereference of blockTypeTesr
		this.renderItem = NonNullLazy.of(() -> itemBlock.blockTypeTesr.initRenderItem());
	}

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return renderItem.get();
	}
}
