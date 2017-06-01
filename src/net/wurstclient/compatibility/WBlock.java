/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.compatibility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public final class WBlock
{
	private static final AxisAlignedBB CHEST_AABB =
		new AxisAlignedBB(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
	
	public static IBlockState getState(BlockPos pos)
	{
		return WMinecraft.getWorld().getBlockState(pos);
	}
	
	public static Block getBlock(BlockPos pos)
	{
		return getState(pos).getBlock();
	}
	
	public static int getId(BlockPos pos)
	{
		return Block.getIdFromBlock(getBlock(pos));
	}
	
	public static String getName(Block block)
	{
		return "" + Block.blockRegistry.getNameForObject(block);
	}
	
	public static Material getMaterial(BlockPos pos)
	{
		return getBlock(pos).getMaterial();
	}
	
	public static AxisAlignedBB getBoundingBox(BlockPos pos)
	{
		Block block = getBlock(pos);
		
		if(block instanceof BlockChest)
			return CHEST_AABB.offset(pos);
		
		return block.getSelectedBoundingBox(WMinecraft.getWorld(), pos);
	}
	
	public static boolean canBeClicked(BlockPos pos)
	{
		return getBlock(pos).canCollideCheck(getState(pos), false);
	}
	
	public static float getHardness(BlockPos pos)
	{
		return getBlock(pos).getPlayerRelativeBlockHardness(
			WMinecraft.getPlayer(), WMinecraft.getWorld(), pos);
	}
}
