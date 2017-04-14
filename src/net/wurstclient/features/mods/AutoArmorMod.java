/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.compatibility.WPlayerController;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.utils.InventoryUtils;

@Mod.Info(description = "Manages your armor automatically.",
	name = "AutoArmor",
	tags = "auto armor",
	help = "Mods/AutoArmor")
@Mod.Bypasses
public final class AutoArmorMod extends Mod implements UpdateListener
{
	private int timer;
	
	@Override
	public void onEnable()
	{
		// reset timer
		timer = 0;
		
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// wait for timer
		if(timer > 0)
		{
			timer--;
			return;
		}
		
		// check screen
		if(mc.currentScreen instanceof GuiContainer
			&& !(mc.currentScreen instanceof InventoryEffectRenderer))
			return;
		
		// store slots and values of best armor pieces
		int[] bestArmorSlots = new int[4];
		int[] bestArmorValues = new int[4];
		
		// initialize with currently equipped armor
		for(int armorType = 0; armorType < 4; armorType++)
		{
			ItemStack oldArmor =
				WMinecraft.getPlayer().inventory.armorItemInSlot(armorType);
			if(oldArmor != null && oldArmor.getItem() instanceof ItemArmor)
				bestArmorValues[armorType] =
					((ItemArmor)oldArmor.getItem()).damageReduceAmount;
			
			bestArmorSlots[armorType] = -1;
		}
		
		// search inventory for better armor
		for(int slot = 0; slot < 36; slot++)
		{
			ItemStack stack =
				WMinecraft.getPlayer().inventory.getStackInSlot(slot);
			if(stack == null || !(stack.getItem() instanceof ItemArmor))
				continue;
			
			ItemArmor armor = (ItemArmor)stack.getItem();
			int armorType = InventoryUtils.getArmorType(armor);
			int armorValue = armor.damageReduceAmount;
			
			if(armorValue > bestArmorValues[armorType])
			{
				bestArmorSlots[armorType] = slot;
				bestArmorValues[armorType] = armorValue;
			}
		}
		
		// equip better armor
		for(int armorType = 0; armorType < 4; armorType++)
		{
			// check if better armor was found
			int slot = bestArmorSlots[armorType];
			if(slot == -1)
				continue;
				
			// check if armor can be swapped
			// needs 1 free slot where it can put the old armor
			ItemStack oldArmor =
				WMinecraft.getPlayer().inventory.armorItemInSlot(armorType);
			if(oldArmor == null || !InventoryUtils.isEmptySlot(oldArmor)
				|| WMinecraft.getPlayer().inventory.getFirstEmptyStack() != -1)
			{
				// hotbar fix
				if(slot < 9)
					slot += 36;
				
				// swap armor
				WPlayerController.windowClick_QUICK_MOVE(8 - armorType);
				WPlayerController.windowClick_QUICK_MOVE(slot);
				
				break;
			}
		}
		
		// set timer
		timer = 2;
	}
}
