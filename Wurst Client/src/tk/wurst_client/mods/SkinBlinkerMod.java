/*
 * Copyright � 2014 - 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EnumPlayerModelParts;
import tk.wurst_client.WurstClient;
import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(category = Mod.Category.FUN,
	description = "Makes your skin blink.\n"
		+ "Requires a skin with a jacket, a hat or something similar.",
	name = "SkinBlinker")
public class SkinBlinkerMod extends Mod implements UpdateListener
{
	@Override
	public void onEnable()
	{
		WurstClient.INSTANCE.eventManager.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		updateMS();
		if(hasTimePassedS(5f))
		{
			updateLastMS();
			Set activeParts =
				Minecraft.getMinecraft().gameSettings.func_178876_d();
			for(EnumPlayerModelParts part : EnumPlayerModelParts.values())
				Minecraft.getMinecraft().gameSettings.func_178878_a(part,
					!activeParts.contains(part));
		}
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.eventManager.remove(UpdateListener.class, this);
		for(EnumPlayerModelParts part : EnumPlayerModelParts.values())
			Minecraft.getMinecraft().gameSettings.func_178878_a(part, true);
	}
}
