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
package forestry.mail;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.mail.EnumAddressee;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostOffice;
import forestry.api.mail.IPostalCarrier;
import forestry.api.mail.IPostalState;
import forestry.api.mail.ITradeStation;
import forestry.api.mail.PostManager;
import forestry.core.render.TextureManagerForestry;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.PlayerUtil;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;

public class PostalCarrier implements IPostalCarrier {

	private final String iconID;
	private final EnumAddressee type;

	public PostalCarrier(EnumAddressee type) {
		iconID = "mail/carrier." + type;
		this.type = type;
	}

	@Override
	public EnumAddressee getType() {
		return type;
	}

	// todo this is unused, delete?
	@Override
	public String getName() {
		return Component.translatable("for.gui.addressee." + type).getString();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getSprite() {
		return TextureManagerForestry.INSTANCE.getDefault(iconID);
	}

	@Override
	public IPostalState deliverLetter(ServerLevel world, IPostOffice office, IMailAddress recipient, ItemStack letterStack, boolean doDeliver) {
		if (type == EnumAddressee.TRADER) {
			return handleTradeLetter(world, recipient, letterStack, doDeliver);
		} else {
			return storeInPOBox(world, recipient, letterStack);
		}
	}

	private static IPostalState handleTradeLetter(ServerLevel world, IMailAddress recipient, ItemStack letterStack, boolean doLodge) {
		ITradeStation trade = PostManager.postRegistry.getTradeStation(world, recipient);
		if (trade == null) {
			return EnumDeliveryState.NO_MAILBOX;
		}

		return trade.handleLetter(world, recipient, letterStack, doLodge);
	}

	private static EnumDeliveryState storeInPOBox(ServerLevel world, IMailAddress recipient, ItemStack letterStack) {

		POBox pobox = PostRegistry.getPOBox(world, recipient);
		if (pobox == null) {
			return EnumDeliveryState.NO_MAILBOX;
		}

		if (!pobox.storeLetter(letterStack.copy())) {
			return EnumDeliveryState.MAILBOX_FULL;
		} else {
			Player player = PlayerUtil.getPlayer(world, recipient.getPlayerProfile());
			if (player instanceof ServerPlayer) {
				NetworkUtil.sendToPlayer(new PacketPOBoxInfoResponse(pobox.getPOBoxInfo()), (ServerPlayer) player);
			}
		}

		return EnumDeliveryState.OK;
	}

}
