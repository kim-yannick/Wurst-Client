/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.utils;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.compatibility.WBlock;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WItem;
import net.wurstclient.compatibility.WMinecraft;

public class InventoryUtils
{
	public static boolean placeStackInHotbar(ItemStack stack)
	{
		for(int i = 0; i < 9; i++)
			if(isSlotEmpty(i))
			{
				WConnection.sendPacket(
					new CPacketCreativeInventoryAction(36 + i, stack));
				return true;
			}
		
		return false;
	}
	
	public static void placeStackInArmor(int armorSlot, ItemStack stack)
	{
		WMinecraft.getPlayer().inventory.armorInventory[armorSlot] = stack;
	}
	
	public static boolean isSlotEmpty(int slot)
	{
		return WMinecraft.getPlayer().inventory.getStackInSlot(slot) == null;
	}
	
	public static boolean isSplashPotion(ItemStack stack)
	{
		return stack != null && stack.getItem() == Items.potionitem
			&& ItemPotion.isSplash(stack.getItemDamage());
	}
	
	public static ItemStack createSplashPotion()
	{
		ItemStack stack = new ItemStack(Items.potionitem);
		stack.setItemDamage(16384);
		return stack;
	}
	
	public static float getStrVsBlock(ItemStack stack, BlockPos pos)
	{
		return stack.getStrVsBlock(WBlock.getBlock(pos));
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
		ItemStack stack = WMinecraft.getPlayer().inventory.getCurrentItem();
		
		if(WItem.isNull(stack))
			return false;
		
		return validator.isValid(stack.getItem());
	}
	
	public static interface ItemValidator
	{
		public boolean isValid(Item item);
	}
}
