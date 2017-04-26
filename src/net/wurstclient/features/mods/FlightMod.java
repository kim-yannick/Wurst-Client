/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.AxisAlignedBB;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Feature;
import net.wurstclient.features.Mod;
import net.wurstclient.features.SearchTags;
import net.wurstclient.features.special_features.YesCheatSpf.BypassLevel;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"FlyHack", "fly hack", "flying"})
@Mod.Info(help = "Mods/Flight")
@Mod.Bypasses
public final class FlightMod extends Mod implements UpdateListener
{
	public final SliderSetting speed =
		new SliderSetting("Speed", 1, 0.05, 5, 0.05, ValueDisplay.DECIMAL);
	
	public double flyHeight;
	private double startY;
	
	public final CheckboxSetting flightKickBypass =
		new CheckboxSetting("Flight-Kick-Bypass", false);
	
	public FlightMod()
	{
		super("Flight",
			"Allows you to you fly.\n"
				+ "Bypasses NoCheat+ if YesCheat+ is enabled.\n"
				+ "Bypasses MAC if AntiMAC is enabled.");
	}
	
	@Override
	public String getRenderName()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal()
			|| !flightKickBypass.isChecked())
			return getName();
		
		return getName() + "[Kick: " + (flyHeight <= 300 ? "Safe" : "Unsafe")
			+ "]";
	}
	
	@Override
	public void initSettings()
	{
		settings.add(speed);
		settings.add(flightKickBypass);
	}
	
	public void updateFlyHeight()
	{
		double h = 1;
		AxisAlignedBB box = WMinecraft.getPlayer().getEntityBoundingBox()
			.expand(0.0625, 0.0625, 0.0625);
		for(flyHeight = 0; flyHeight < WMinecraft.getPlayer().posY; flyHeight +=
			h)
		{
			AxisAlignedBB nextBox = box.offset(0, -flyHeight, 0);
			
			if(WMinecraft.getWorld().checkBlockCollision(nextBox))
			{
				if(h < 0.0625)
					break;
				
				flyHeight -= h;
				h /= 2;
			}
		}
	}
	
	public void goToGround()
	{
		if(flyHeight > 300)
			return;
		
		double minY = WMinecraft.getPlayer().posY - flyHeight;
		
		if(minY <= 0)
			return;
		
		for(double y = WMinecraft.getPlayer().posY; y > minY;)
		{
			y -= 8;
			if(y < minY)
				y = minY;
			
			Position packet = new Position(WMinecraft.getPlayer().posX, y,
				WMinecraft.getPlayer().posZ, true);
			WConnection.sendPacket(packet);
		}
		
		for(double y = minY; y < WMinecraft.getPlayer().posY;)
		{
			y += 8;
			if(y > WMinecraft.getPlayer().posY)
				y = WMinecraft.getPlayer().posY;
			
			Position packet = new Position(WMinecraft.getPlayer().posX, y,
				WMinecraft.getPlayer().posZ, true);
			WConnection.sendPacket(packet);
		}
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.jetpackMod, wurst.mods.glideMod,
			wurst.mods.noFallMod, wurst.special.yesCheatSpf};
	}
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.jetpackMod.isEnabled())
			wurst.mods.jetpackMod.setEnabled(false);
		
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
		{
			double startX = WMinecraft.getPlayer().posX;
			startY = WMinecraft.getPlayer().posY;
			double startZ = WMinecraft.getPlayer().posZ;
			for(int i = 0; i < 4; i++)
			{
				WConnection.sendPacket(new CPacketPlayer.Position(startX,
					startY + 1.01, startZ, false));
				WConnection.sendPacket(
					new CPacketPlayer.Position(startX, startY, startZ, false));
			}
			WMinecraft.getPlayer().jump();
		}
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() > BypassLevel.ANTICHEAT.ordinal())
		{
			if(!WMinecraft.getPlayer().onGround)
				if(mc.gameSettings.keyBindJump.pressed
					&& WMinecraft.getPlayer().posY < startY - 1)
					WMinecraft.getPlayer().motionY = 0.2;
				else
					WMinecraft.getPlayer().motionY = -0.02;
		}else if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() == BypassLevel.ANTICHEAT.ordinal())
		{
			updateMS();
			if(!WMinecraft.getPlayer().onGround)
				if(mc.gameSettings.keyBindJump.pressed && hasTimePassedS(2))
				{
					WMinecraft.getPlayer().setPosition(
						WMinecraft.getPlayer().posX,
						WMinecraft.getPlayer().posY + 8,
						WMinecraft.getPlayer().posZ);
					updateLastMS();
				}else if(mc.gameSettings.keyBindSneak.pressed)
					WMinecraft.getPlayer().motionY = -0.4;
				else
					WMinecraft.getPlayer().motionY = -0.02;
			WMinecraft.getPlayer().jumpMovementFactor = 0.04F;
		}else
		{
			updateMS();
			
			WMinecraft.getPlayer().capabilities.isFlying = false;
			WMinecraft.getPlayer().motionX = 0;
			WMinecraft.getPlayer().motionY = 0;
			WMinecraft.getPlayer().motionZ = 0;
			WMinecraft.getPlayer().jumpMovementFactor = speed.getValueF();
			
			if(mc.gameSettings.keyBindJump.pressed)
				WMinecraft.getPlayer().motionY += speed.getValue();
			if(mc.gameSettings.keyBindSneak.pressed)
				WMinecraft.getPlayer().motionY -= speed.getValue();
			
			if(flightKickBypass.isChecked())
			{
				updateFlyHeight();
				WConnection.sendPacket(new CPacketPlayer(true));
				
				if(flyHeight <= 290 && hasTimePassedM(500)
					|| flyHeight > 290 && hasTimePassedM(100))
				{
					goToGround();
					updateLastMS();
				}
			}
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
}
