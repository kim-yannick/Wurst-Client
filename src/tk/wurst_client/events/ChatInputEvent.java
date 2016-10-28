/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import tk.wurst_client.events.listeners.ChatInputListener;

public class ChatInputEvent extends CancellableEvent<ChatInputListener>
{
	private IChatComponent component;
	private List<ChatLine> chatLines;
	
	public ChatInputEvent(IChatComponent component, List<ChatLine> chatLines)
	{
		this.component = component;
		this.chatLines = chatLines;
	}
	
	public IChatComponent getComponent()
	{
		return component;
	}
	
	public void setComponent(IChatComponent component)
	{
		this.component = component;
	}
	
	public List<ChatLine> getChatLines()
	{
		return chatLines;
	}
	
	@Override
	public void fire(ArrayList<ChatInputListener> listeners)
	{
		for(int i = 0; i < listeners.size(); i++)
		{
			listeners.get(i).onReceivedMessage(this);
			if(isCancelled())
				break;
		}
	}
	
	@Override
	public Class<ChatInputListener> getListenerType()
	{
		return ChatInputListener.class;
	}
}
