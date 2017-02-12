/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import tk.wurst_client.features.Feature;
import tk.wurst_client.gui.mods.GuiOpSign;
import tk.wurst_client.utils.ChatUtils;

@Mod.Info(
	description = "Enable this mod, place a sign and click it to get OP.\n"
		+ "Can also be used to run any other command.\n"
		+ "Only works on servers running Minecraft 1.8 - 1.8.5 without Spigot!\n"
		+ "Type .sv to check the server version.",
	name = "OP-Sign",
	tags = "Force OP,OP Sign,Sign OP,sign hack,admin hack,OpSign",
	help = "Mods/OP-Sign_(Force_OP)")
@Mod.Bypasses
public class OpSignMod extends Mod
{
	public String command;
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.special.bookHackSpf, wurst.mods.forceOpMod,
			wurst.special.sessionStealerSpf};
	}
	
	@Override
	public void onEnable()
	{
		mc.displayGuiScreen(new GuiOpSign(this, mc.currentScreen));
	}
	
	public void setCommand(String cmd)
	{
		command = cmd.replace("\"", "\\\\\"");
		ChatUtils.message("Command set. Place & right click a sign to run it.");
	}
}
