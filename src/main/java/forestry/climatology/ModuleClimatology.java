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
package forestry.climatology;

import java.util.function.Consumer;

import net.minecraft.client.gui.screens.MenuScreens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.fml.loading.FMLEnvironment;

import forestry.api.climate.IClimateListener;
import forestry.api.climate.IClimateTransformer;
import forestry.api.modules.ForestryModule;
import forestry.climatology.features.ClimatologyMenuTypes;
import forestry.climatology.gui.GuiHabitatFormer;
import forestry.climatology.network.packets.PacketSelectClimateTargeted;
import forestry.climatology.proxy.ProxyClimatology;
import forestry.core.ClientsideCode;
import forestry.core.config.Constants;
import forestry.core.network.IPacketRegistry;
import forestry.core.network.PacketIdServer;
import forestry.modules.BlankForestryModule;
import forestry.modules.ForestryModuleUids;
import forestry.modules.ISidedModuleHandler;

@ForestryModule(modId = Constants.MOD_ID, moduleID = ForestryModuleUids.CLIMATOLOGY, name = "Climatology", author = "Nedelosk", url = Constants.URL, unlocalizedDescription = "for.module.greenhouse.description")
public class ModuleClimatology extends BlankForestryModule {
	private static final ProxyClimatology PROXY = FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.newProxyClimatology() : new ProxyClimatology();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerGuiFactories() {
		MenuScreens.register(ClimatologyMenuTypes.HABITAT_FORMER.menuType(), GuiHabitatFormer::new);
	}

	@Override
	public void preInit() {
		PROXY.preInit();
	}

	@Override
	public void registerCapabilities(Consumer<Class<?>> consumer) {
		consumer.accept(IClimateListener.class);
		consumer.accept(IClimateTransformer.class);
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.SELECT_CLIMATE_TARGETED, PacketSelectClimateTargeted.class, PacketSelectClimateTargeted::decode, PacketSelectClimateTargeted::handle);
	}

	@Override
	public ISidedModuleHandler getModuleHandler() {
		return PROXY;
	}
}
