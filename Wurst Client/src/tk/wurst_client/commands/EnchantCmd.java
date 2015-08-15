/*
 * Copyright © 2014 - 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import tk.wurst_client.WurstClient;
import tk.wurst_client.commands.Cmd.Info;

@Info(help = "Enchants items with everything or removes enchantments.",
	name = "enchant",
	syntax = {"[all]", "clear"})
public class EnchantCmd extends Cmd
{
	@Override
	public void execute(String[] args) throws Error
	{
		if(!Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode)
			error("Creative mode only.");
		
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		
		if(args.length == 0)
		{
			ItemStack currentItem = player.inventory.getCurrentItem();
			if(currentItem == null)
				error("There is no item in your hand.");
			for(Enchantment enchantment : Enchantment.enchantmentsList)
				try
				{
					if(enchantment == Enchantment.silkTouch)
						continue;
					currentItem.addEnchantment(enchantment, 127);
				}catch(Exception e)
				{	
					
				}
			
			Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(
					new C10PacketCreativeInventoryAction(
							36+player.inventory.currentItem, currentItem));
		}else if(args[0].equalsIgnoreCase("all"))
		{
			int items = 0;
			for(int i = 5; i < 45; i++)
			{
				ItemStack currentItem = player.inventoryContainer.getSlot(i).getStack();
				if(currentItem == null)
					continue;
				items++;
				for(Enchantment enchantment : Enchantment.enchantmentsList)
					try
					{
						if(enchantment == Enchantment.silkTouch)
							continue;
						currentItem.addEnchantment(enchantment, 127);
					}catch(Exception e)
					{	
						
					}
				
				Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(
						new C10PacketCreativeInventoryAction(
								i, currentItem));
			}
			if(items == 1)
				WurstClient.INSTANCE.chat.message("Enchanted 1 item.");
			else
				WurstClient.INSTANCE.chat.message("Enchanted " + items
					+ " items.");
		}else if (args[0].equalsIgnoreCase("clear")) {
			ItemStack currentItem = player.inventory.getCurrentItem();
			if(currentItem == null)
				error("There is no item in your hand.");
			
			NBTTagCompound tag = currentItem.getTagCompound();
			
			if (tag != null && tag.hasKey("ench")) {
				tag.removeTag("ench");
				
				Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(
						new C10PacketCreativeInventoryAction(
								36+player.inventory.currentItem, currentItem));
				
				WurstClient.INSTANCE.chat.message("Disenchanted 1 item.");
			}
		}else
			syntaxError();
	}
}
