/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import org.darkstorm.minecraft.gui.component.BoundedRangeComponent.ValueDisplay;

import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.settings.SliderSetting;

@Info(category = Category.COMBAT,
	description = "Automatically throws splash healing potions if your health is below the set value.",
	name = "AutoSplashPot",
	tags = "AutoPotion,auto potion,auto splash potion",
	tutorial = "Mods/AutoSplashPot")
public class AutoSplashPotMod extends Mod implements UpdateListener
{
	public float health = 18F;
	
	@Override
	public void initSettings()
	{
		settings.add(new SliderSetting("Health", health, 2, 20, 1,
			ValueDisplay.INTEGER)
		{
			@Override
			public void update()
			{
				health = (float)getValue();
			}
		});
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		// check if no container is open
		if(mc.currentScreen instanceof GuiContainer
			&& !(mc.currentScreen instanceof GuiInventory))
			return;
		
		// check if health is low
		if(mc.thePlayer.getHealth() >= health)
			return;
		
		// find health potions
		int potionInInventory = findPotion(9, 36);
		int potionInHotbar = findPotion(36, 45);
		
		// check if any potion was found
		if(potionInInventory == -1 && potionInHotbar == -1)
			return;
		
		if(hasTimePassedM(500))
			if(potionInHotbar != -1)
			{
				// throw potion in hotbar
				int oldSlot = mc.thePlayer.inventory.currentItem;
				NetHandlerPlayClient sendQueue = mc.thePlayer.sendQueue;
				sendQueue
					.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(
						mc.thePlayer.rotationYaw, 90.0F, mc.thePlayer.onGround));
				sendQueue.addToSendQueue(new C09PacketHeldItemChange(
					potionInHotbar - 36));
				mc.playerController.updateController();
				sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(
					mc.thePlayer.inventoryContainer.getSlot(potionInHotbar)
						.getStack()));
				sendQueue.addToSendQueue(new C09PacketHeldItemChange(oldSlot));
				sendQueue
					.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(
						mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
						mc.thePlayer.onGround));
				
				// reset timer
				updateLastMS();
			}else
				// move potion in inventory to hotbar
				mc.playerController.windowClick(0, potionInInventory, 0, 1,
					mc.thePlayer);
		
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	private int findPotion(int startSlot, int endSlot)
	{
		for(int i = startSlot; i < endSlot; i++)
		{
			ItemStack stack =
				mc.thePlayer.inventoryContainer.getSlot(i).getStack();
			if(stack != null && stack.getItem() == Items.potionitem
				&& ItemPotion.isSplash(stack.getItemDamage()))
			{
				for(Object o : ((ItemPotion)stack.getItem()).getEffects(stack))
				{
					if(((PotionEffect)o).getPotionID() == Potion.heal.id)
						return i;
				}
			}
		}
		return -1;
	}
}
