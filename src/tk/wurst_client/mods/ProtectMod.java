/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.entity.EntityLivingBase;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.EntityUtils;

@Mod.Info(
	description = "A bot that follows the closest entity and protects it.",
	name = "Protect",
	tutorial = "Mods/Protect")
public class ProtectMod extends Mod implements UpdateListener
{
	private EntityLivingBase friend;
	private EntityLivingBase enemy;
	private float range = 6F;
	private double distanceF = 2D;
	private double distanceE = 3D;
	private float speed;
	
	@Override
	public String getRenderName()
	{
		if(friend != null)
			return "Protecting " + friend.getName();
		else
			return "Protect";
	}
	
	@Override
	public void onEnable()
	{
		friend = null;
		EntityLivingBase en = EntityUtils.getClosestEntity(false, true);
		if(en != null && mc.player.getDistanceToEntity(en) <= range)
			friend = en;
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(friend == null || friend.isDead || friend.getHealth() <= 0
			|| mc.player.getHealth() <= 0)
		{
			friend = null;
			enemy = null;
			setEnabled(false);
			return;
		}
		if(enemy != null && (enemy.getHealth() <= 0 || enemy.isDead))
			enemy = null;
		double xDistF = Math.abs(mc.player.posX - friend.posX);
		double zDistF = Math.abs(mc.player.posZ - friend.posZ);
		double xDistE = distanceE;
		double zDistE = distanceE;
		if(enemy != null && mc.player.getDistanceToEntity(enemy) <= range)
		{
			xDistE = Math.abs(mc.player.posX - enemy.posX);
			zDistE = Math.abs(mc.player.posZ - enemy.posZ);
		}else
			EntityUtils.faceEntityClient(friend);
		if((xDistF > distanceF || zDistF > distanceF)
			&& (enemy == null || mc.player.getDistanceToEntity(enemy) > range)
			|| xDistE > distanceE || zDistE > distanceE)
			mc.gameSettings.keyBindForward.pressed = true;
		else
			mc.gameSettings.keyBindForward.pressed = false;
		if(mc.player.isCollidedHorizontally && mc.player.onGround)
			mc.player.jump();
		if(mc.player.isInWater() && mc.player.posY < friend.posY)
			mc.player.motionY += 0.04;
		if(wurst.mods.yesCheatMod.isActive())
			speed = wurst.mods.killauraMod.yesCheatSpeed;
		else
			speed = wurst.mods.killauraMod.normalSpeed;
		updateMS();
		if(hasTimePassedS(speed) && EntityUtils.getClosestEnemy(friend) != null)
		{
			enemy = EntityUtils.getClosestEnemy(friend);
			if(mc.player.getDistanceToEntity(enemy) <= range)
			{
				if(wurst.mods.autoSwordMod.isActive())
					AutoSwordMod.setSlot();
				wurst.mods.criticalsMod.doCritical();
				wurst.mods.blockHitMod.doBlock();
				EntityUtils.faceEntityClient(enemy);
				mc.player.swingItem();
				mc.playerController.attackEntity(mc.player, enemy);
				updateLastMS();
			}
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		if(friend != null)
			mc.gameSettings.keyBindForward.pressed = false;
	}
	
	public void setFriend(EntityLivingBase friend)
	{
		this.friend = friend;
	}
}
