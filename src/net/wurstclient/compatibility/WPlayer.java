/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.compatibility;

import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class WPlayer
{
	public static void swingArmClient()
	{
		WMinecraft.getPlayer().swingArm();
	}
	
	public static void swingArmPacket()
	{
		WMinecraft.getPlayer().connection.sendPacket(new CPacketAnimation());
	}
	
	public static float getCooldown()
	{
		return 1;
	}
	
	public static void addPotionEffect(Potion potion)
	{
		WMinecraft.getPlayer()
			.addPotionEffect(new PotionEffect(potion.getId(), 10801220));
	}
	
	public static void removePotionEffect(Potion potion)
	{
		WMinecraft.getPlayer().removePotionEffect(potion.getId());
	}
}
