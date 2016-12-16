/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.special.YesCheatSpf.BypassLevel;

@Mod.Info(description = "Automatically sneaks all the time.",
	name = "Sneak",
	tags = "AutoSneaking",
	help = "Mods/Sneak")
@Mod.Bypasses
public class SneakMod extends Mod implements UpdateListener
{
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
		{
			NetHandlerPlayClient sendQueue = mc.player.connection;
			sendQueue.sendPacket(new C0BPacketEntityAction(
				Minecraft.getMinecraft().player, Action.START_SNEAKING));
			sendQueue.sendPacket(new C0BPacketEntityAction(
				Minecraft.getMinecraft().player, Action.STOP_SNEAKING));
		}else
			mc.player.connection.sendPacket(new C0BPacketEntityAction(
				Minecraft.getMinecraft().player, Action.START_SNEAKING));
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		mc.gameSettings.keyBindSneak.pressed = false;
		mc.player.connection.sendPacket(
			new C0BPacketEntityAction(mc.player, Action.STOP_SNEAKING));
	}
}
