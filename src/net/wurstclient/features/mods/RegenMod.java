/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.network.play.client.CPacketPlayer;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;

@Mod.Info(
	description = "Regenerates your health 1000 times faster.\n"
		+ "Can cause unwanted \"Flying is not enabled!\" kicks.",
	name = "Regen",
	tags = "GodMode, god mode",
	help = "Mods/Regen")
@Mod.Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public final class RegenMod extends Mod implements UpdateListener
{
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
		if(!WMinecraft.getPlayer().capabilities.isCreativeMode
			&& WMinecraft.getPlayer().getFoodStats().getFoodLevel() > 17
			&& WMinecraft.getPlayer().getHealth() < 20
			&& WMinecraft.getPlayer().getHealth() != 0
			&& WMinecraft.getPlayer().onGround)
			for(int i = 0; i < 1000; i++)
				WConnection.sendPacket(new CPacketPlayer());
	}
}
