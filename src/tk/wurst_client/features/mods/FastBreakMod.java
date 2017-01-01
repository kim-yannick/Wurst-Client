/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import tk.wurst_client.features.Feature;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;

@Mod.Info(
	description = "Allows you to break blocks faster.\n"
		+ "Tip: This works with Nuker.",
	name = "FastBreak",
	tags = "SpeedyGonzales, fast break, speedy gonzales",
	help = "Mods/FastBreak")
@Mod.Bypasses
public class FastBreakMod extends Mod
{
	public float speed = 2;
	
	@Override
	public void initSettings()
	{
		settings.add(
			new SliderSetting("Speed", speed, 1, 5, 0.05, ValueDisplay.DECIMAL)
			{
				@Override
				public void update()
				{
					speed = (float)getValue();
				}
			});
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.fastPlaceMod,
			wurst.mods.autoMineMod, wurst.mods.nukerMod};
	}
}
