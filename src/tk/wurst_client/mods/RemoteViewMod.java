/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.UUID;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.ChatUtils;
import tk.wurst_client.utils.EntityUtils;

@Mod.Info(
	description = "Allows you to see the world as someone else.\n"
		+ "Use the .rv command to make it target a specific entity.",
	name = "RemoteView",
	tags = "remote view",
	tutorial = "Mods/RemoteView")
public class RemoteViewMod extends Mod implements UpdateListener
{
	private EntityPlayerSP newView = null;
	private double oldX;
	private double oldY;
	private double oldZ;
	private float oldYaw;
	private float oldPitch;
	private EntityLivingBase otherView = null;
	private static UUID otherID = null;
	private boolean wasInvisible;
	
	@Override
	public void onEnable()
	{
		if(EntityUtils.getClosestEntityRaw(false) == null)
		{
			ChatUtils.message("There is no nearby entity.");
			setEnabled(false);
			return;
		}
		oldX = mc.player.posX;
		oldY = mc.player.posY;
		oldZ = mc.player.posZ;
		oldYaw = mc.player.rotationYaw;
		oldPitch = mc.player.rotationPitch;
		mc.player.noClip = true;
		if(otherID == null)
			otherID = EntityUtils.getClosestEntityRaw(false).getUniqueID();
		otherView = EntityUtils.searchEntityByIdRaw(otherID);
		wasInvisible = otherView.isInvisibleToPlayer(mc.player);
		EntityOtherPlayerMP fakePlayer =
			new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
		fakePlayer.clonePlayer(mc.player, true);
		fakePlayer.copyLocationAndAnglesFrom(mc.player);
		fakePlayer.rotationYawHead = mc.player.rotationYawHead;
		mc.world.addEntityToWorld(-69, fakePlayer);
		ChatUtils.message("Now viewing " + otherView.getName() + ".");
		wurst.events.add(UpdateListener.class, this);
	}
	
	public static void onEnabledByCommand(String viewName)
	{
		try
		{
			if(otherID == null && !viewName.equals(""))
				otherID =
					EntityUtils.searchEntityByNameRaw(viewName).getUniqueID();
			wurst.mods.remoteViewMod.toggle();
		}catch(NullPointerException e)
		{
			ChatUtils.error("Entity not found.");
		}
	}
	
	@Override
	public void onUpdate()
	{
		if(EntityUtils.searchEntityByIdRaw(otherID) == null)
		{
			setEnabled(false);
			return;
		}
		newView = mc.player;
		otherView = EntityUtils.searchEntityByIdRaw(otherID);
		newView.copyLocationAndAnglesFrom(otherView);
		mc.player.motionX = 0;
		mc.player.motionY = 0;
		mc.player.motionZ = 0;
		mc.player = newView;
		otherView.setInvisible(true);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		if(otherView != null)
		{
			ChatUtils
				.message("No longer viewing " + otherView.getName() + ".");
			otherView.setInvisible(wasInvisible);
			mc.player.noClip = false;
			mc.player.setPositionAndRotation(oldX, oldY, oldZ, oldYaw,
				oldPitch);
			mc.world.removeEntityFromWorld(-69);
		}
		newView = null;
		otherView = null;
		otherID = null;
	}
}
