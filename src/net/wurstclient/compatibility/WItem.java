/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.compatibility;

import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;

public final class WItem
{
	public static boolean isNull(Item item)
	{
		return item == null;
	}
	
	public static boolean isNull(ItemStack stack)
	{
		return stack == null;
	}
	
	public static int getArmorType(ItemArmor armor)
	{
		return 3 - armor.armorType;
	}
	
	public static boolean isThrowable(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof ItemBow || item instanceof ItemSnowball
			|| item instanceof ItemEgg || item instanceof ItemEnderPearl
			|| item instanceof ItemPotion
				&& ItemPotion.isSplash(stack.getItemDamage());
	}
	
	public static boolean isPotion(ItemStack stack)
	{
		return stack != null && stack.getItem() instanceof ItemPotion;
	}
	
	public static Item getFromRegistry(ResourceLocation location)
	{
		return (Item)Item.REGISTRY.getObject(location);
	}
}
