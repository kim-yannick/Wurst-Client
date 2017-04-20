/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.navigator.gui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.client.gui.GuiButton;
import net.wurstclient.WurstClient;
import net.wurstclient.compatibility.WMath;
import net.wurstclient.compatibility.WSoundEvents;
import net.wurstclient.features.Feature;
import net.wurstclient.files.ConfigFiles;
import net.wurstclient.font.Fonts;
import net.wurstclient.navigator.PossibleKeybind;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.utils.MiscUtils;
import net.wurstclient.utils.RenderUtils;

public class NavigatorFeatureScreen extends NavigatorScreen
{
	private Feature feature;
	private NavigatorMainScreen parent;
	private ButtonData activeButton;
	private GuiButton primaryButton;
	private int sliding = -1;
	private String text;
	private ArrayList<ButtonData> buttonDatas = new ArrayList<>();
	private ArrayList<SliderSetting> sliders = new ArrayList<>();
	private ArrayList<CheckboxSetting> checkboxes = new ArrayList<>();
	
	public NavigatorFeatureScreen(Feature feature, NavigatorMainScreen parent)
	{
		this.feature = feature;
		this.parent = parent;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if(!button.enabled)
			return;
		
		WurstClient wurst = WurstClient.INSTANCE;
		switch(button.id)
		{
			case 0:
			feature.doPrimaryAction();
			primaryButton.displayString = feature.getPrimaryAction();
			break;
			case 1:
			MiscUtils.openLink("https://www.wurstclient.net/wiki/"
				+ feature.getHelpPage() + "/");
			wurst.navigator.analytics.trackEvent("help", "open",
				feature.getName());
			break;
		}
		
		wurst.navigator.addPreference(feature.getName());
		ConfigFiles.NAVIGATOR.save();
	}
	
	@Override
	protected void onResize()
	{
		buttonDatas.clear();
		
		// primary button
		String primaryAction = feature.getPrimaryAction();
		boolean hasPrimaryAction = !primaryAction.isEmpty();
		boolean hasHelp = !feature.getHelpPage().isEmpty();
		if(hasPrimaryAction)
		{
			primaryButton = new GuiButton(0, width / 2 - 151, height - 65,
				hasHelp ? 149 : 302, 18, primaryAction);
			buttonList.add(primaryButton);
		}
		
		// help button
		if(hasHelp)
			buttonList
				.add(new GuiButton(1, width / 2 + (hasPrimaryAction ? 2 : -151),
					height - 65, hasPrimaryAction ? 149 : 302, 20, "Help"));
		
		// type
		text = "Type: " + feature.getType();
		
		// description
		String description = feature.getDescription();
		if(!description.isEmpty())
			text += "\n\nDescription:\n" + description;
		
		// area
		Rectangle area = new Rectangle(middleX - 154, 60, 308, height - 103);
		
		// settings
		ArrayList<Setting> settings = feature.getSettings();
		if(!settings.isEmpty())
		{
			text += "\n\nSettings:";
			sliders.clear();
			checkboxes.clear();
			for(Setting setting : settings)
				setting.addToFeatureScreen(this);
		}
		
		// keybinds
		ArrayList<PossibleKeybind> possibleKeybinds =
			feature.getPossibleKeybinds();
		if(!possibleKeybinds.isEmpty())
		{
			// heading
			text += "\n\nKeybinds:";
			
			// add keybind button
			ButtonData addKeybindButton =
				new ButtonData(area.x + area.width - 16,
					area.y + Fonts.segoe15.getStringHeight(text) - 7, 12, 8,
					"+", 0x00ff00)
				{
					@Override
					public void press()
					{
						// add keybind
						mc.displayGuiScreen(new NavigatorNewKeybindScreen(
							possibleKeybinds, NavigatorFeatureScreen.this));
					}
				};
			buttonDatas.add(addKeybindButton);
			
			// keybind list
			HashMap<String, String> possibleKeybindsMap = new HashMap<>();
			for(PossibleKeybind possibleKeybind : possibleKeybinds)
				possibleKeybindsMap.put(possibleKeybind.getCommand(),
					possibleKeybind.getDescription());
			TreeMap<String, PossibleKeybind> existingKeybinds = new TreeMap<>();
			boolean noKeybindsSet = true;
			for(Entry<String, TreeSet<String>> entry : WurstClient.INSTANCE.keybinds
				.entrySet())
				for(String command : entry.getValue())
				{
					String keybindDescription =
						possibleKeybindsMap.get(command);
					if(keybindDescription != null)
					{
						if(noKeybindsSet)
							noKeybindsSet = false;
						text +=
							"\n" + entry.getKey() + ": " + keybindDescription;
						existingKeybinds.put(entry.getKey(),
							new PossibleKeybind(command, keybindDescription));
					}
				}
			if(noKeybindsSet)
				text += "\nNone";
			else
			{
				// remove keybind button
				buttonDatas.add(new ButtonData(addKeybindButton.x,
					addKeybindButton.y, addKeybindButton.width,
					addKeybindButton.height, "-", 0xff0000)
				{
					@Override
					public void press()
					{
						// remove keybind
						mc.displayGuiScreen(new NavigatorRemoveKeybindScreen(
							existingKeybinds, NavigatorFeatureScreen.this));
					}
				});
				addKeybindButton.x -= 16;
			}
		}
		
		// see also
		Feature[] seeAlso = feature.getSeeAlso();
		if(seeAlso.length != 0)
		{
			text += "\n\nSee also:";
			for(Feature seeAlsoFeature : seeAlso)
			{
				int y = 60 + getTextHeight() + 2;
				String name = seeAlsoFeature.getName();
				text += "\n- " + name;
				buttonDatas.add(new ButtonData(middleX - 148, y,
					Fonts.segoe15.getStringWidth(name) + 1, 8, "", 0x404040)
				{
					@Override
					public void press()
					{
						mc.displayGuiScreen(
							new NavigatorFeatureScreen(seeAlsoFeature, parent));
					}
				});
			}
		}
		
		// text height
		setContentHeight(Fonts.segoe15.getStringHeight(text));
	}
	
	@Override
	protected void onKeyPress(char typedChar, int keyCode)
	{
		if(keyCode == 1)
		{
			parent.setExpanding(false);
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	protected void onMouseClick(int x, int y, int button)
	{
		Rectangle area = new Rectangle(width / 2 - 154, 60, 308, height - 103);
		if(!area.contains(x, y))
			return;
		
		// buttons
		if(activeButton != null)
		{
			WSoundEvents.playButtonClick();
			activeButton.press();
			WurstClient.INSTANCE.navigator.addPreference(feature.getName());
			ConfigFiles.NAVIGATOR.save();
			return;
		}
		
		// sliders
		area.height = 12;
		for(int i = 0; i < sliders.size(); i++)
		{
			area.y = sliders.get(i).getY() + scroll;
			if(area.contains(x, y))
			{
				sliding = i;
				return;
			}
		}
		
		// checkboxes
		for(int i = 0; i < checkboxes.size(); i++)
		{
			CheckboxSetting checkbox = checkboxes.get(i);
			area.y = checkbox.getY() + scroll;
			if(area.contains(x, y))
			{
				checkbox.toggle();
				WurstClient wurst = WurstClient.INSTANCE;
				wurst.navigator.addPreference(feature.getName());
				ConfigFiles.NAVIGATOR.save();
				return;
			}
		}
	}
	
	@Override
	protected void onMouseDrag(int x, int y, int button, long timeDragged)
	{
		if(button != 0)
			return;
		if(sliding != -1)
		{
			// percentage from mouse location (not the actual percentage!)
			float mousePercentage =
				WMath.clamp((x - (middleX - 150)) / 298F, 0, 1);
			
			// update slider value
			SliderSetting slider = sliders.get(sliding);
			slider.setValue(
				slider.getMinimum() + slider.getRange() * mousePercentage);
		}
	}
	
	@Override
	protected void onMouseRelease(int x, int y, int button)
	{
		if(sliding != -1)
		{
			WurstClient wurst = WurstClient.INSTANCE;
			sliding = -1;
			
			wurst.navigator.addPreference(feature.getName());
			ConfigFiles.NAVIGATOR.save();
		}
	}
	
	@Override
	protected void onUpdate()
	{
		
	}
	
	@Override
	protected void onRender(int mouseX, int mouseY, float partialTicks)
	{
		// title bar
		drawCenteredString(Fonts.segoe22, feature.getName(), middleX, 32,
			0xffffff);
		glDisable(GL_TEXTURE_2D);
		
		// background
		int bgx1 = middleX - 154;
		int bgx2 = middleX + 154;
		int bgy1 = 60;
		int bgy2 = height - 43;
		
		// scissor box
		RenderUtils.scissorBox(bgx1, bgy1, bgx2,
			bgy2 - (buttonList.isEmpty() ? 0 : 24));
		glEnable(GL_SCISSOR_TEST);
		
		// sliders
		for(SliderSetting slider : sliders)
		{
			// rail
			int x1 = bgx1 + 2;
			int x2 = bgx2 - 2;
			int y1 = slider.getY() + scroll + 4;
			int y2 = y1 + 4;
			setColorToForeground();
			drawEngravedBox(x1, y1, x2, y2);
			
			int width = x2 - x1;
			
			// lock
			boolean renderAsDisabled = slider.isDisabled() || slider.isLocked();
			if(!renderAsDisabled && slider.isLimited())
			{
				glColor4f(0.75F, 0.125F, 0.125F, 0.25F);
				
				double ratio = width / slider.getRange();
				
				drawQuads(x1, y1, (int)(x1
					+ ratio * (slider.getUsableMin() - slider.getMinimum())),
					y2);
				drawQuads(
					(int)(x2 + ratio
						* (slider.getUsableMax() - slider.getMaximum())),
					y1, x2, y2);
			}
			
			// knob
			float percentage = slider.getPercentage();
			x1 = bgx1 + (int)((width - 6) * percentage) + 1;
			x2 = x1 + 8;
			y1 -= 2;
			y2 += 2;
			if(renderAsDisabled)
				glColor4f(0.5F, 0.5F, 0.5F, 0.75F);
			else
			{
				float factor = 2 * percentage;
				glColor4f(factor, 2 - factor, 0F, 0.75F);
			}
			drawBox(x1, y1, x2, y2);
			
			// value
			String value = slider.getValueString();
			x1 = bgx2 - Fonts.segoe15.getStringWidth(value) - 2;
			y1 -= 12;
			drawString(Fonts.segoe15, value, x1, y1,
				renderAsDisabled ? 0xaaaaaa : 0xffffff);
			glDisable(GL_TEXTURE_2D);
		}
		
		// buttons
		activeButton = null;
		for(ButtonData buttonData : buttonDatas)
		{
			// positions
			int x1 = buttonData.x;
			int x2 = x1 + buttonData.width;
			int y1 = buttonData.y + scroll;
			int y2 = y1 + buttonData.height;
			
			// color
			float alpha;
			if(buttonData.isLocked())
				alpha = 0.25F;
			else if(mouseX >= x1 && mouseX <= x2 && mouseY >= y1
				&& mouseY <= y2)
			{
				alpha = 0.75F;
				activeButton = buttonData;
			}else
				alpha = 0.375F;
			float[] rgb = buttonData.color.getColorComponents(null);
			glColor4f(rgb[0], rgb[1], rgb[2], alpha);
			
			// button
			drawBox(x1, y1, x2, y2);
			
			// text
			drawCenteredString(Fonts.segoe15, buttonData.buttonText,
				(x1 + x2) / 2, y1 + (buttonData.height - 10) / 2 - 1,
				buttonData.isLocked() ? 0xaaaaaa : buttonData.textColor);
			glDisable(GL_TEXTURE_2D);
		}
		
		// checkboxes
		for(CheckboxSetting checkbox : checkboxes)
		{
			// positions
			int x1 = bgx1 + 2;
			int x2 = x1 + 10;
			int y1 = checkbox.getY() + scroll + 2;
			int y2 = y1 + 10;
			
			// hovering
			boolean hovering = !checkbox.isLocked() && mouseX >= x1
				&& mouseX <= bgx2 - 2 && mouseY >= y1 && mouseY <= y2;
			
			// box
			if(hovering)
				glColor4f(0.375F, 0.375F, 0.375F, 0.25F);
			else
				glColor4f(0.25F, 0.25F, 0.25F, 0.25F);
			drawBox(x1, y1, x2, y2);
			
			// check
			if(checkbox.isChecked())
			{
				// check
				glColor4f(0F, 1F, 0F, hovering ? 0.75F : 0.375F);
				glBegin(GL_QUADS);
				{
					glVertex2i(x1 + 3, y1 + 5);
					glVertex2i(x1 + 4, y1 + 6);
					glVertex2i(x1 + 4, y1 + 8);
					glVertex2i(x1 + 2, y1 + 6);
					
					glVertex2i(x1 + 7, y1 + 2);
					glVertex2i(x1 + 8, y1 + 3);
					glVertex2i(x1 + 4, y1 + 8);
					glVertex2i(x1 + 4, y1 + 6);
				}
				glEnd();
				
				// shadow
				glColor4f(0.125F, 0.125F, 0.125F, hovering ? 0.75F : 0.375F);
				glBegin(GL_LINE_LOOP);
				{
					glVertex2i(x1 + 3, y1 + 5);
					glVertex2i(x1 + 4, y1 + 6);
					glVertex2i(x1 + 7, y1 + 2);
					glVertex2i(x1 + 8, y1 + 3);
					
					glVertex2i(x1 + 4, y1 + 8);
					glVertex2i(x1 + 2, y1 + 6);
				}
				glEnd();
				
			}
			
			// name
			x1 += 12;
			y1 -= 1;
			drawString(Fonts.segoe15, checkbox.getName(), x1, y1,
				checkbox.isLocked() ? 0xaaaaaa : 0xffffff);
			glDisable(GL_TEXTURE_2D);
		}
		
		// text
		drawString(Fonts.segoe15, text, bgx1 + 2, bgy1 + scroll, 0xffffff);
		
		// scissor box
		glDisable(GL_SCISSOR_TEST);
		
		// buttons below scissor box
		for(int i = 0; i < buttonList.size(); i++)
		{
			GuiButton button = buttonList.get(i);
			
			// positions
			int x1 = button.xPosition;
			int x2 = x1 + button.getButtonWidth();
			int y1 = button.yPosition;
			int y2 = y1 + 18;
			
			// color
			boolean hovering =
				mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
			if(feature.isEnabled() && button.id == 0)
				if(feature.isBlocked())
					glColor4f(hovering ? 1F : 0.875F, 0F, 0F, 0.25F);
				else
					glColor4f(0F, hovering ? 1F : 0.875F, 0F, 0.25F);
			else if(hovering)
				glColor4f(0.375F, 0.375F, 0.375F, 0.25F);
			else
				glColor4f(0.25F, 0.25F, 0.25F, 0.25F);
			
			// button
			glDisable(GL_TEXTURE_2D);
			drawBox(x1, y1, x2, y2);
			
			// text
			drawCenteredString(Fonts.segoe18, button.displayString,
				(x1 + x2) / 2, y1 + 2, 0xffffff);
		}
		
		// GL resets
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
	}
	
	public Feature getFeature()
	{
		return feature;
	}
	
	public int getMiddleX()
	{
		return middleX;
	}
	
	public void addText(String text)
	{
		this.text += text;
	}
	
	public int getTextHeight()
	{
		return Fonts.segoe15.getStringHeight(text);
	}
	
	public void addButton(ButtonData button)
	{
		buttonDatas.add(button);
	}
	
	public void addSlider(SliderSetting slider)
	{
		sliders.add(slider);
	}
	
	public void addCheckbox(CheckboxSetting checkbox)
	{
		checkboxes.add(checkbox);
	}
	
	public abstract class ButtonData extends Rectangle
	{
		public String buttonText;
		public Color color;
		public int textColor = 0xffffff;
		
		public ButtonData(int x, int y, int width, int height,
			String buttonText, int color)
		{
			super(x, y, width, height);
			this.buttonText = buttonText;
			this.color = new Color(color);
		}
		
		public abstract void press();
		
		public boolean isLocked()
		{
			return false;
		}
	}
}
