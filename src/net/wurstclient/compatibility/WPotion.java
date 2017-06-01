/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.compatibility;

import java.util.List;

import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class WPotion
{
	public static List<PotionEffect> getEffectsFromStack(ItemStack stack)
	{
		return new ItemPotion().getEffects(stack);
	}
	
	public static int getIdFromEffect(PotionEffect effect)
	{
		return effect.getPotionID();
	}
	
	public static int getIdFromResourceLocation(String location)
	{
		return Potion.func_180142_b(location).id;
	}
}
