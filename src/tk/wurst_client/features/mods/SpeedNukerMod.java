/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import tk.wurst_client.events.LeftClickEvent;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.ChatUtils;

@Mod.Info(description = "Faster Nuker that cannot bypass NoCheat+.",
	name = "SpeedNuker",
	tags = "FastNuker, speed nuker, fast nuker",
	help = "Mods/SpeedNuker")
@Mod.Bypasses
public class SpeedNukerMod extends Mod
	implements LeftClickListener, UpdateListener
{
	private static Block currentBlock;
	private BlockPos pos;
	private int oldSlot = -1;
	
	@Override
	public String getRenderName()
	{
		NukerMod nuker = wurst.mods.nukerMod;
		switch(nuker.getMode())
		{
			case 0:
				return "SpeedNuker";
			case 1:
				return "IDSpeedNuker [" + NukerMod.id + "]";
			default:
				return nuker.getModes()[nuker.getMode()] + "SpeedNuker";
		}
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.nukerMod,
			wurst.mods.nukerLegitMod, wurst.mods.tunnellerMod,
			wurst.mods.fastBreakMod, wurst.mods.autoMineMod};
	}
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.nukerMod.isEnabled())
			wurst.mods.nukerMod.setEnabled(false);
		if(wurst.mods.nukerLegitMod.isEnabled())
			wurst.mods.nukerLegitMod.setEnabled(false);
		if(wurst.mods.tunnellerMod.isEnabled())
			wurst.mods.tunnellerMod.setEnabled(false);
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
		{
			setEnabled(false);
			ChatUtils
				.message("Switching to " + wurst.mods.nukerMod.getName() + ".");
			wurst.mods.nukerMod.setEnabled(true);
			return;
		}
		if(mc.player.capabilities.isCreativeMode)
		{
			ChatUtils.error(getName() + " doesn't work in creative mode.");
			setEnabled(false);
			ChatUtils
				.message("Switching to " + wurst.mods.nukerMod.getName() + ".");
			wurst.mods.nukerMod.setEnabled(true);
			return;
		}
		BlockPos newPos = find();
		if(newPos == null)
		{
			if(oldSlot != -1)
			{
				mc.player.inventory.currentItem = oldSlot;
				oldSlot = -1;
			}
			return;
		}
		pos = newPos;
		currentBlock = mc.world.getBlockState(pos).getBlock();
		if(wurst.mods.autoToolMod.isActive() && oldSlot == -1)
			oldSlot = mc.player.inventory.currentItem;
		if(!mc.player.capabilities.isCreativeMode
			&& wurst.mods.autoToolMod.isActive() && currentBlock
				.getPlayerRelativeBlockHardness(mc.player, mc.world, pos) < 1)
			AutoToolMod.setSlot(pos);
		nukeAll();
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(LeftClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		if(oldSlot != -1)
		{
			mc.player.inventory.currentItem = oldSlot;
			oldSlot = -1;
		}
		NukerMod.id = 0;
		wurst.files.saveOptions();
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.getBlockPos() == null)
			return;
		if(wurst.mods.nukerMod.getMode() == 1
			&& mc.world.getBlockState(mc.objectMouseOver.getBlockPos())
				.getBlock().getMaterial() != Material.air)
		{
			NukerMod.id = Block.getIdFromBlock(mc.world
				.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock());
			wurst.files.saveOptions();
		}
	}
	
	private BlockPos find()
	{
		BlockPos closest = null;
		float closestDistance = wurst.mods.nukerMod.yesCheatRange + 1;
		int nukerMode = wurst.mods.nukerMod.getMode();
		for(int y = (int)wurst.mods.nukerMod.yesCheatRange; y >= (nukerMode == 2
			? 0 : -wurst.mods.nukerMod.yesCheatRange); y--)
			for(int x =
				(int)wurst.mods.nukerMod.yesCheatRange; x >= -wurst.mods.nukerMod.yesCheatRange
					- 1; x--)
				for(int z =
					(int)wurst.mods.nukerMod.yesCheatRange; z >= -wurst.mods.nukerMod.yesCheatRange; z--)
				{
					if(mc.player == null)
						continue;
					if(x == 0 && y == -1 && z == 0)
						continue;
					int posX = (int)(Math.floor(mc.player.posX) + x);
					int posY = (int)(Math.floor(mc.player.posY) + y);
					int posZ = (int)(Math.floor(mc.player.posZ) + z);
					BlockPos blockPos = new BlockPos(posX, posY, posZ);
					Block block = mc.world.getBlockState(blockPos).getBlock();
					float xDiff = (float)(mc.player.posX - posX);
					float yDiff = (float)(mc.player.posY - posY);
					float zDiff = (float)(mc.player.posZ - posZ);
					float currentDistance =
						BlockUtils.getBlockDistance(xDiff, yDiff, zDiff);
					MovingObjectPosition fakeObjectMouseOver =
						mc.objectMouseOver;
					if(fakeObjectMouseOver == null)
						continue;
					fakeObjectMouseOver.setBlockPos(blockPos);
					if(Block.getIdFromBlock(block) != 0 && posY >= 0
						&& currentDistance <= wurst.mods.nukerMod.yesCheatRange)
					{
						if(nukerMode == 1
							&& Block.getIdFromBlock(block) != NukerMod.id)
							continue;
						if(nukerMode == 3
							&& block.getPlayerRelativeBlockHardness(mc.player,
								mc.world, blockPos) < 1)
							continue;
						if(closest == null)
						{
							closest = blockPos;
							closestDistance = currentDistance;
						}else if(currentDistance < closestDistance)
						{
							closest = blockPos;
							closestDistance = currentDistance;
						}
					}
				}
		return closest;
	}
	
	private void nukeAll()
	{
		int nukerMode = wurst.mods.nukerMod.getMode();
		for(int y = (int)wurst.mods.nukerMod.normalRange; y >= (nukerMode == 2
			? 0 : -wurst.mods.nukerMod.normalRange); y--)
			for(int x =
				(int)wurst.mods.nukerMod.normalRange; x >= -wurst.mods.nukerMod.normalRange
					- 1; x--)
				for(int z =
					(int)wurst.mods.nukerMod.normalRange; z >= -wurst.mods.nukerMod.normalRange; z--)
				{
					int posX = (int)(Math.floor(mc.player.posX) + x);
					int posY = (int)(Math.floor(mc.player.posY) + y);
					int posZ = (int)(Math.floor(mc.player.posZ) + z);
					if(x == 0 && y == -1 && z == 0)
						continue;
					BlockPos blockPos = new BlockPos(posX, posY, posZ);
					Block block = mc.world.getBlockState(blockPos).getBlock();
					float xDiff = (float)(mc.player.posX - posX);
					float yDiff = (float)(mc.player.posY - posY);
					float zDiff = (float)(mc.player.posZ - posZ);
					float currentDistance =
						BlockUtils.getBlockDistance(xDiff, yDiff, zDiff);
					MovingObjectPosition fakeObjectMouseOver =
						mc.objectMouseOver;
					fakeObjectMouseOver
						.setBlockPos(new BlockPos(posX, posY, posZ));
					if(Block.getIdFromBlock(block) != 0 && posY >= 0
						&& currentDistance <= wurst.mods.nukerMod.normalRange)
					{
						if(nukerMode == 1
							&& Block.getIdFromBlock(block) != NukerMod.id)
							continue;
						if(nukerMode == 3
							&& block.getPlayerRelativeBlockHardness(mc.player,
								mc.world, blockPos) < 1)
							continue;
						if(!mc.player.onGround)
							continue;
						EnumFacing side = fakeObjectMouseOver.sideHit;
						mc.player.connection
							.sendPacket(new C07PacketPlayerDigging(
								Action.START_DESTROY_BLOCK, blockPos, side));
						mc.player.connection
							.sendPacket(new C07PacketPlayerDigging(
								Action.STOP_DESTROY_BLOCK, blockPos, side));
					}
				}
	}
}
