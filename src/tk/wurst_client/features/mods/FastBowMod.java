/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;

@Mod.Info(
	description = "Turns your bow into a machine gun.\n"
		+ "Tip: This works with BowAimbot.",
	name = "FastBow",
	noCheatCompatible = false,
	tags = "RapidFire, BowSpam, fast bow, rapid fire, bow spam",
	help = "Mods/FastBow")
@Mod.Bypasses
public class FastBowMod extends Mod implements UpdateListener
{
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.bowAimbotMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(mc.player.getHealth() > 0
			&& (mc.player.onGround
				|| Minecraft.getMinecraft().player.capabilities.isCreativeMode)
			&& mc.player.inventory.getCurrentItem() != null
			&& mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow
			&& mc.gameSettings.keyBindUseItem.pressed)
		{
			mc.playerController.processRightClick(mc.player, mc.world,
				mc.player.inventory.getCurrentItem());
			mc.player.inventory.getCurrentItem().getItem().onItemRightClick(
				mc.player.inventory.getCurrentItem(), mc.world, mc.player);
			for(int i = 0; i < 20; i++)
				mc.player.connection.sendPacket(new CPacketPlayer(false));
			Minecraft.getMinecraft().getNetHandler()
				.sendPacket(new C07PacketPlayerDigging(Action.RELEASE_USE_ITEM,
					new BlockPos(0, 0, 0), EnumFacing.DOWN));
			mc.player.inventory.getCurrentItem().getItem().onPlayerStoppedUsing(
				mc.player.inventory.getCurrentItem(), mc.world, mc.player, 10);
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
}
