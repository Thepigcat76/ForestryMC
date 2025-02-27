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
package forestry.energy.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import forestry.api.fuels.FuelManager;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.utils.SlotUtil;
import forestry.energy.tiles.PeatEngineBlockEntity;

public class InventoryEnginePeat extends InventoryAdapterTile<PeatEngineBlockEntity> {
	public static final short SLOT_FUEL = 0;
	public static final short SLOT_WASTE_1 = 1;
	public static final short SLOT_WASTE_COUNT = 4;

	public InventoryEnginePeat(PeatEngineBlockEntity engineCopper) {
		super(engineCopper, 5, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack itemStack) {
		return slotIndex == SLOT_FUEL && FuelManager.peatEngineFuel.containsKey(itemStack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_WASTE_1, SLOT_WASTE_COUNT);
	}
}
