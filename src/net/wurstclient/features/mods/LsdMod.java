/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.wurstclient.compatibility.WPotionEffects;
import net.wurstclient.compatibility.WPlayer;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.HelpPage;
import net.wurstclient.features.Mod;

@HelpPage("Mods/LSD")
@Mod.Bypasses
@Mod.DontSaveState
public final class LsdMod extends Mod implements UpdateListener
{
	public LsdMod()
	{
		super("LSD", "Thousands of colors!");
	}
	
	@Override
	public void onToggle()
	{
		if(!OpenGlHelper.shadersSupported)
			mc.renderGlobal.loadRenderers();
	}
	
	@Override
	public void onEnable()
	{
		if(OpenGlHelper.shadersSupported)
			if(mc.getRenderViewEntity() instanceof EntityPlayer)
			{
				if(mc.entityRenderer.theShaderGroup != null)
					mc.entityRenderer.theShaderGroup.deleteShaderGroup();
				
				mc.entityRenderer.shaderIndex = 19;
				
				if(mc.entityRenderer.shaderIndex != EntityRenderer.SHADER_COUNT)
					mc.entityRenderer
						.loadShader(EntityRenderer.SHADERS_TEXTURES[19]);
				else
					mc.entityRenderer.theShaderGroup = null;
			}
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		
		WPlayer.removePotionEffect(WPotionEffects.NAUSEA);
		
		if(mc.entityRenderer.theShaderGroup != null)
		{
			mc.entityRenderer.theShaderGroup.deleteShaderGroup();
			mc.entityRenderer.theShaderGroup = null;
		}
		
		mc.gameSettings.smoothCamera = false;
	}
	
	@Override
	public void onUpdate()
	{
		if(!OpenGlHelper.shadersSupported)
			WPlayer.addPotionEffect(WPotionEffects.NAUSEA);
		
		mc.gameSettings.smoothCamera = isEnabled();
	}
}
