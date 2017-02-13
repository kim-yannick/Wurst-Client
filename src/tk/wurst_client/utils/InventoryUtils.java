/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

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
	
	public static boolean isSplashPotion(ItemStack stack)
	{
		return stack != null && stack.getItem() == Items.potionitem
			&& ItemPotion.isSplash(stack.getItemDamage());
	}
	
	public static int getArmorType(ItemArmor armor)
	{
		return 3 - armor.armorType;
	}
	
	public static boolean hasEffect(ItemStack stack, Potion potion)
	{
		for(PotionEffect effect : ((ItemPotion)stack.getItem())
			.getEffects(stack))
			if(effect.getPotionID() == potion.id)
				return true;
			
		return false;
	}
	
	public static boolean checkHeldItem(ItemValidator validator)
	{
		ItemStack stack = mc.player.inventory.getCurrentItem();
		
		if(isEmptySlot(stack))
			return false;
		
		return validator.isValid(stack.getItem());
	}
	
	public static interface ItemValidator
	{
		public boolean isValid(Item item);
	}
}
