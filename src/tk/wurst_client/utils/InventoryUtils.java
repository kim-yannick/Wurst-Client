/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;

public class InventoryUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static boolean placeStackInHotbar(ItemStack stack)
	{
		for(int i = 0; i < 9; i++)
			if(isSlotEmpty(i))
			{
				mc.player.connection.sendPacket(
					new CPacketCreativeInventoryAction(36 + i, stack));
				return true;
			}
		
		return false;
	}
	
	public static boolean isSlotEmpty(int slot)
	{
		return mc.player.inventory.getStackInSlot(slot) == null;
	}
	
	public static boolean isEmptySlot(ItemStack slot)
	{
		return slot == null;
	}
}
