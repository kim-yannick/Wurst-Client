/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.commands;

import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WPlayerController;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.special_features.YesCheatSpf.BypassLevel;

@Cmd.Info(description = "Drops all your items on the ground.",
	name = "drop",
	syntax = {"[infinite]"},
	help = "Commands/drop")
public final class DropCmd extends Cmd implements UpdateListener
{
	private int timer;
	private int counter;
	private boolean infinite;
	
	@Override
	public void execute(String[] args) throws CmdError
	{
		if(args.length > 1)
			syntaxError();
		if(args.length == 1)
			if(args[0].equalsIgnoreCase("infinite"))
				infinite = !infinite;
			else
				syntaxError();
		else
			infinite = false;
		timer = 0;
		counter = 9;
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(infinite)
		{
			Item item = null;
			while(item == null)
				item = Item.getItemById(new Random().nextInt(431));
			WConnection.sendPacket(new CPacketCreativeInventoryAction(-1,
				new ItemStack(item, 64)));
			return;
		}
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.OLDER_NCP.ordinal())
		{
			timer++;
			if(timer >= 5)
			{
				WPlayerController.windowClick_THROW(counter);
				counter++;
				timer = 0;
				if(counter >= 45)
					wurst.events.remove(UpdateListener.class, this);
			}
		}else
		{
			for(int i = 9; i < 45; i++)
				WPlayerController.windowClick_THROW(i);
			wurst.events.remove(UpdateListener.class, this);
		}
	}
}
