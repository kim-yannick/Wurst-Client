/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.compatibility.WBlock;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Feature;
import net.wurstclient.utils.BlockUtils;
import net.wurstclient.utils.InventoryUtils;

@Mod.Info(description = "Automatically places blocks below your feet.",
	name = "ScaffoldWalk",
	tags = "scaffold walk, tower")
@Mod.Bypasses(ghostMode = false)
public final class ScaffoldWalkMod extends Mod implements UpdateListener
{
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.safeWalkMod, wurst.mods.buildRandomMod};
	}
	
	@Override
	public void onEnable()
	{
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
		BlockPos belowPlayer = new BlockPos(WMinecraft.getPlayer()).down();
		
		// check if block is already placed
		if(!WBlock.getMaterial(belowPlayer).isReplaceable())
			return;
		
		// search blocks in hotbar
		int newSlot = -1;
		for(int i = 0; i < 9; i++)
		{
			// filter out non-block items
			ItemStack stack =
				WMinecraft.getPlayer().inventory.getStackInSlot(i);
			if(InventoryUtils.isEmptySlot(stack)
				|| !(stack.getItem() instanceof ItemBlock))
				continue;
			
			// filter out non-solid blocks
			if(!Block.getBlockFromItem(stack.getItem()).getDefaultState()
				.isFullBlock())
				continue;
			
			newSlot = i;
			break;
		}
		
		// check if any blocks were found
		if(newSlot == -1)
			return;
		
		// set slot
		int oldSlot = WMinecraft.getPlayer().inventory.currentItem;
		WMinecraft.getPlayer().inventory.currentItem = newSlot;
		
		// place block
		BlockUtils.placeBlockLegit(belowPlayer);
		
		// reset slot
		WMinecraft.getPlayer().inventory.currentItem = oldSlot;
	}
}
