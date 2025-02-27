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
package forestry.mail.blocks;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.ForestryTicker;
import forestry.core.tiles.TileForestry;
import forestry.mail.features.MailTiles;
import forestry.mail.tiles.TileStampCollector;
import forestry.mail.tiles.TileTrader;
import forestry.modules.features.FeatureTileType;

public enum BlockTypeMail implements IBlockType {
	MAILBOX(() -> MailTiles.MAILBOX, "mailbox", null),
	TRADE_STATION(() -> MailTiles.TRADER, "trade_station", TileTrader::serverTick),
	PHILATELIST(() -> MailTiles.STAMP_COLLECTOR, "stamp_collector", TileStampCollector::serverTick);

	public static final BlockTypeMail[] VALUES = values();

	private final IMachineProperties machineProperties;

	<T extends TileForestry> BlockTypeMail(Supplier<FeatureTileType<? extends T>> teClass, String name, @Nullable ForestryTicker<T> serverTicker) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name).setServerTicker(serverTicker).create();
	}

	@Override
	public IMachineProperties getMachineProperties() {
		return machineProperties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}
