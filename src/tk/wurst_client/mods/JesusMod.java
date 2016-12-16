/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import tk.wurst_client.WurstClient;
import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(
	description = "Allows you to walk on water.\n"
		+ "The real Jesus used this hack ~2000 years ago.\n"
		+ "Bypasses NoCheat+ if YesCheat+ is enabled.",
	name = "Jesus",
	help = "Mods/Jesus")
@Mod.Bypasses
public class JesusMod extends Mod implements UpdateListener
{
	private int ticksOutOfWater = 10;
	public int time = 0;
	public final int delay = 4;
	
	@Override
	public void onEnable()
	{
		WurstClient.INSTANCE.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!mc.gameSettings.keyBindSneak.pressed)
			if(mc.player.isInWater())
			{
				mc.player.motionY = 0.11;
				ticksOutOfWater = 0;
			}else
			{
				if(ticksOutOfWater == 0)
					mc.player.motionY = 0.30;
				else if(ticksOutOfWater == 1)
					mc.player.motionY = 0;
				
				ticksOutOfWater++;
			}
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.events.remove(UpdateListener.class, this);
	}
	
	public boolean isOverWater()
	{
		final EntityPlayerSP thePlayer = mc.player;
		
		boolean isOnWater = false;
		boolean isOnSolid = false;
		
		for(final Object o : mc.world.getCollidingBoundingBoxes(thePlayer,
			thePlayer.getEntityBoundingBox().offset(0, -1.0D, 0)
				.contract(0.001D, 0D, 0.001D)))
		{
			final AxisAlignedBB bbox = (AxisAlignedBB)o;
			final BlockPos blockPos =
				new BlockPos(bbox.maxX - (bbox.maxX - bbox.minX) / 2.0,
					bbox.maxY - (bbox.maxY - bbox.minY) / 2.0,
					bbox.maxZ - (bbox.maxZ - bbox.minZ) / 2.0);
			final Block block = mc.world.getBlockState(blockPos).getBlock();
			if(block.getMaterial() == Material.water
				|| block.getMaterial() == Material.lava)
				isOnWater = true;
			else if(block.getMaterial() != Material.air)
				isOnSolid = true;
		}
		
		return isOnWater && !isOnSolid;
	}
	
	public boolean shouldBeSolid()
	{
		return isActive() && !(mc.player == null)
			&& !(mc.player.fallDistance > 3)
			&& !mc.gameSettings.keyBindSneak.pressed && !mc.player.isInWater();
	}
}
