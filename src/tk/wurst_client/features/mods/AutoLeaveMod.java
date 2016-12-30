/*
 * Copyright � 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.settings.ModeSetting;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;

@Mod.Info(
	description = "Automatically leaves the server when your health is low.\n"
		+ "The Chars, TP and SelfHurt modes can bypass CombatLog and similar plugins.",
	name = "AutoLeave",
	tags = "AutoDisconnect, auto leave, auto disconnect",
	help = "Mods/AutoLeave")
@Mod.Bypasses
public class AutoLeaveMod extends Mod implements UpdateListener
{
	public SliderSetting health =
		new SliderSetting("Health", 4, 0.5, 9.5, 0.5, ValueDisplay.DECIMAL);
	public ModeSetting mode = new ModeSetting("Mode",
		new String[]{"Quit", "Chars", "TP", "SelfHurt"}, 0);
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.commands.leaveCmd};
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + mode.getSelectedMode() + "]";
	}
	
	@Override
	public void initSettings()
	{
		settings.add(health);
		settings.add(mode);
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
		// check gamemode
		if(mc.player.capabilities.isCreativeMode)
			return;
		
		// check for other players
		if(mc.isSingleplayer() || Minecraft.getMinecraft().player.connection
			.getPlayerInfoMap().size() == 1)
			return;
		
		// check health
		if(mc.player.getHealth() > health.getValueF() * 2F)
			return;
		
		// leave server
		switch(mode.getSelected())
		{
			case 0:
				mc.world.sendQuittingDisconnectingPacket();
				break;
			
			case 1:
				mc.player.connection.sendPacket(new CPacketChatMessage("�"));
				break;
			
			case 2:
				mc.player.connection.sendPacket(
					new CPacketPlayer.Position(3.1e7d, 100, 3.1e7d, false));
				break;
			
			case 3:
				mc.player.connection
					.sendPacket(new CPacketUseEntity(mc.player, Action.ATTACK));
				break;
		}
		
		// disable
		setEnabled(false);
	}
}
