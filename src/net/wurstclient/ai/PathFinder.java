/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.WurstClient;
import net.wurstclient.compatibility.WBlock;
import net.wurstclient.compatibility.WMinecraft;

public class PathFinder
{
	private final WurstClient wurst = WurstClient.INSTANCE;
	
	private final boolean invulnerable =
		WMinecraft.getPlayer().capabilities.isCreativeMode;
	private final boolean creativeFlying =
		WMinecraft.getPlayer().capabilities.isFlying;
	protected final boolean flying =
		creativeFlying || wurst.mods.flightMod.isActive();
	private final boolean immuneToFallDamage =
		invulnerable || wurst.mods.noFallMod.isActive();
	private final boolean noSlowdownActive =
		wurst.mods.noSlowdownMod.isActive();
	private final boolean jesus = wurst.mods.jesusMod.isActive();
	private final boolean spider = wurst.mods.spiderMod.isActive();
	protected boolean fallingAllowed = true;
	
	private final PathPos start;
	protected PathPos current;
	private final BlockPos goal;
	
	private final HashMap<PathPos, Float> costMap = new HashMap<>();
	protected final HashMap<PathPos, PathPos> prevPosMap = new HashMap<>();
	private final PathQueue queue = new PathQueue();
	
	protected int thinkSpeed = 1024;
	protected int thinkTime = 200;
	private int iterations;
	
	protected boolean done;
	protected boolean failed;
	private final ArrayList<PathPos> path = new ArrayList<>();
	
	public PathFinder(BlockPos goal)
	{
		start = new PathPos(new BlockPos(WMinecraft.getPlayer()));
		this.goal = goal;
		
		costMap.put(start, 0F);
		queue.add(start, getHeuristic(start));
	}
	
	public PathFinder(PathFinder pathFinder)
	{
		this(pathFinder.goal);
		thinkSpeed = pathFinder.thinkSpeed;
		thinkTime = pathFinder.thinkTime;
	}
	
	public void think()
	{
		if(done)
			throw new IllegalStateException("Path was already found!");
		
		int i = 0;
		for(; i < thinkSpeed && !checkFailed(); i++)
		{
			// get next position from queue
			current = queue.poll();
			
			// check if path is found
			if(checkDone())
				return;
			
			// add neighbors to queue
			for(PathPos next : getNeighbors(current))
			{
				// check cost
				float newCost = costMap.get(current) + getCost(current, next);
				if(costMap.containsKey(next) && costMap.get(next) <= newCost)
					continue;
				
				// add to queue
				costMap.put(next, newCost);
				prevPosMap.put(next, current);
				queue.add(next, newCost + getHeuristic(next));
			}
		}
		iterations += i;
	}
	
	protected boolean checkDone()
	{
		return done = goal.equals(current);
	}
	
	private boolean checkFailed()
	{
		return failed = queue.isEmpty() || iterations >= thinkSpeed * thinkTime;
	}
	
	private ArrayList<PathPos> getNeighbors(PathPos pos)
	{
		ArrayList<PathPos> neighbors = new ArrayList<>();
		
		// abort if too far away
		if(Math.abs(start.getX() - pos.getX()) > 256
			|| Math.abs(start.getZ() - pos.getZ()) > 256)
			return neighbors;
		
		// get all neighbors
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();
		
		BlockPos northEast = north.east();
		BlockPos southEast = south.east();
		BlockPos southWest = south.west();
		BlockPos northWest = north.west();
		
		BlockPos up = pos.up();
		BlockPos down = pos.down();
		
		// flying
		boolean flying = canFlyAt(pos);
		// walking
		boolean onGround = canBeSolid(down);
		
		// player can move sideways if flying, standing on the ground, jumping,
		// or inside of a block that allows sideways movement (ladders, webs,
		// etc.)
		if(flying || onGround || pos.isJumping()
			|| canMoveSidewaysInMidairAt(pos) || canClimbUpAt(pos.down()))
		{
			// north
			if(checkHorizontalMovement(pos, north))
				neighbors.add(new PathPos(north));
			
			// east
			if(checkHorizontalMovement(pos, east))
				neighbors.add(new PathPos(east));
			
			// south
			if(checkHorizontalMovement(pos, south))
				neighbors.add(new PathPos(south));
			
			// west
			if(checkHorizontalMovement(pos, west))
				neighbors.add(new PathPos(west));
			
			// north-east
			if(checkDiagonalMovement(pos, EnumFacing.NORTH, EnumFacing.EAST))
				neighbors.add(new PathPos(northEast));
			
			// south-east
			if(checkDiagonalMovement(pos, EnumFacing.SOUTH, EnumFacing.EAST))
				neighbors.add(new PathPos(southEast));
			
			// south-west
			if(checkDiagonalMovement(pos, EnumFacing.SOUTH, EnumFacing.WEST))
				neighbors.add(new PathPos(southWest));
			
			// north-west
			if(checkDiagonalMovement(pos, EnumFacing.NORTH, EnumFacing.WEST))
				neighbors.add(new PathPos(northWest));
		}
		
		// up
		if(pos.getY() < 256 && canGoThrough(up.up())
			&& (flying || onGround || canClimbUpAt(pos))
			&& (flying || canClimbUpAt(pos) || goal.equals(up)
				|| canSafelyStandOn(north) || canSafelyStandOn(east)
				|| canSafelyStandOn(south) || canSafelyStandOn(west)))
			neighbors.add(new PathPos(up, onGround));
		
		// down
		if(pos.getY() > 0 && canGoThrough(down)
			&& (flying || canFallBelow(pos)))
			neighbors.add(new PathPos(down));
		
		return neighbors;
	}
	
	private boolean checkHorizontalMovement(BlockPos current, BlockPos next)
	{
		if(isPassable(next) && (canFlyAt(current) || canGoThrough(next.down())
			|| canSafelyStandOn(next.down())))
			return true;
		
		return false;
	}
	
	private boolean checkDiagonalMovement(BlockPos current,
		EnumFacing direction1, EnumFacing direction2)
	{
		BlockPos horizontal1 = current.offset(direction1);
		BlockPos horizontal2 = current.offset(direction2);
		BlockPos next = horizontal1.offset(direction2);
		
		if(isPassable(horizontal1) && isPassable(horizontal2)
			&& checkHorizontalMovement(current, next))
			return true;
		
		return false;
	}
	
	protected boolean isPassable(BlockPos pos)
	{
		return canGoThrough(pos) && canGoThrough(pos.up())
			&& canGoAbove(pos.down());
	}
	
	protected boolean canBeSolid(BlockPos pos)
	{
		Material material = WBlock.getMaterial(pos);
		Block block = WBlock.getBlock(pos);
		return material.blocksMovement() && !(block instanceof BlockSign)
			|| block instanceof BlockLadder || jesus
				&& (material == Material.WATER || material == Material.LAVA);
	}
	
	private boolean canGoThrough(BlockPos pos)
	{
		// check if loaded
		if(!WMinecraft.getWorld().isBlockLoaded(pos, false))
			return false;
		
		// check if solid
		Material material = WBlock.getMaterial(pos);
		Block block = WBlock.getBlock(pos);
		if(material.blocksMovement() && !(block instanceof BlockSign))
			return false;
		
		// check if trapped
		if(block instanceof BlockTripWire
			|| block instanceof BlockPressurePlate)
			return false;
		
		// check if safe
		if(!invulnerable
			&& (material == Material.LAVA || material == Material.FIRE))
			return false;
		
		return true;
	}
	
	private boolean canGoAbove(BlockPos pos)
	{
		// check for fences, etc.
		Block block = WBlock.getBlock(pos);
		if(block instanceof BlockFence || block instanceof BlockWall
			|| block instanceof BlockFenceGate)
			return false;
		
		return true;
	}
	
	private boolean canSafelyStandOn(BlockPos pos)
	{
		// check if solid
		Material material = WBlock.getMaterial(pos);
		if(!canBeSolid(pos))
			return false;
		
		// check if safe
		if(!invulnerable
			&& (material == Material.CACTUS || material == Material.LAVA))
			return false;
		
		return true;
	}
	
	private boolean canFallBelow(PathPos pos)
	{
		// check if player can keep falling
		BlockPos down2 = pos.down(2);
		if(canGoThrough(down2))
			return true;
		
		// check if player can stand below
		if(!canSafelyStandOn(down2))
			return false;
		
		// check if fall damage is off
		if(immuneToFallDamage && fallingAllowed)
			return true;
		
		// check if fall ends with slime block
		if(WBlock.getBlock(down2) instanceof BlockSlime && fallingAllowed)
			return true;
		
		// check fall damage
		BlockPos prevPos = pos;
		for(int i = 0; i <= (fallingAllowed ? 3 : 1); i++)
		{
			// check if prevPos does not exist, meaning that the pathfinding
			// started during the fall and fall damage should be ignored because
			// it cannot be prevented
			if(prevPos == null)
				return true;
				
			// check if point is not part of this fall, meaning that the fall is
			// too short to cause any damage
			if(!pos.up(i).equals(prevPos))
				return true;
			
			// check if block resets fall damage
			Block prevBlock = WBlock.getBlock(prevPos);
			if(prevBlock instanceof BlockLiquid
				|| prevBlock instanceof BlockLadder
				|| prevBlock instanceof BlockVine
				|| prevBlock instanceof BlockWeb)
				return true;
			
			prevPos = prevPosMap.get(prevPos);
		}
		
		return false;
	}
	
	private boolean canFlyAt(BlockPos pos)
	{
		return flying
			|| !noSlowdownActive && WBlock.getMaterial(pos) == Material.WATER;
	}
	
	private boolean canClimbUpAt(BlockPos pos)
	{
		// check if this block works for climbing
		Block block = WBlock.getBlock(pos);
		if(!spider && !(block instanceof BlockLadder)
			&& !(block instanceof BlockVine))
			return false;
		
		// check if any adjacent block is solid
		BlockPos up = pos.up();
		if(!canBeSolid(pos.north()) && !canBeSolid(pos.east())
			&& !canBeSolid(pos.south()) && !canBeSolid(pos.west())
			&& !canBeSolid(up.north()) && !canBeSolid(up.east())
			&& !canBeSolid(up.south()) && !canBeSolid(up.west()))
			return false;
		
		return true;
	}
	
	private boolean canMoveSidewaysInMidairAt(BlockPos pos)
	{
		// check feet
		Block blockFeet = WBlock.getBlock(pos);
		if(blockFeet instanceof BlockLiquid || blockFeet instanceof BlockLadder
			|| blockFeet instanceof BlockVine || blockFeet instanceof BlockWeb)
			return true;
		
		// check head
		Block blockHead = WBlock.getBlock(pos.up());
		if(blockHead instanceof BlockLiquid || blockHead instanceof BlockWeb)
			return true;
		
		return false;
	}
	
	private float getCost(BlockPos current, BlockPos next)
	{
		float cost = 1F;
		
		// diagonal movement
		if(current.getX() != next.getX() && current.getZ() != next.getZ())
			cost *= 1.4142135623730951F;
		
		// liquids
		Material nextMaterial = WBlock.getMaterial(next);
		if(nextMaterial == Material.WATER && !noSlowdownActive)
			cost *= 1.3164437838225804F;
		else if(nextMaterial == Material.LAVA)
			cost *= 4.539515393656079F;
		
		// soul sand
		if(!canFlyAt(next)
			&& WBlock.getBlock(next.down()) instanceof BlockSoulSand)
			cost *= 2.5F;
		
		return cost;
	}
	
	private float getHeuristic(BlockPos pos)
	{
		float dx = Math.abs(pos.getX() - goal.getX());
		float dy = Math.abs(pos.getY() - goal.getY());
		float dz = Math.abs(pos.getZ() - goal.getZ());
		return 1.001F * (dx + dy + dz - 0.5857864376269049F * Math.min(dx, dz));
	}
	
	public PathPos getCurrentPos()
	{
		return current;
	}
	
	public BlockPos getGoal()
	{
		return goal;
	}
	
	public Set<PathPos> getProcessedBlocks()
	{
		return prevPosMap.keySet();
	}
	
	public PathPos[] getQueuedBlocks()
	{
		return queue.toArray();
	}
	
	public int getQueueSize()
	{
		return queue.size();
	}
	
	public float getCost(BlockPos pos)
	{
		return costMap.get(pos);
	}
	
	public BlockPos getPrevPos(BlockPos pos)
	{
		return prevPosMap.get(pos);
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public boolean isFailed()
	{
		return failed;
	}
	
	public ArrayList<PathPos> formatPath()
	{
		if(!done && !failed)
			throw new IllegalStateException("No path found!");
		if(!path.isEmpty())
			throw new IllegalStateException("Path was already formatted!");
		
		// get last position
		PathPos pos;
		if(!failed)
			pos = current;
		else
		{
			pos = start;
			for(PathPos next : prevPosMap.keySet())
				if(getHeuristic(next) < getHeuristic(pos))
					pos = next;
		}
		
		// get positions
		while(pos != null)
		{
			path.add(pos);
			pos = prevPosMap.get(pos);
		}
		
		// reverse path
		Collections.reverse(path);
		
		return path;
	}
	
	public boolean isPathStillValid(int index)
	{
		if(path.isEmpty())
			throw new IllegalStateException("Path is not formatted!");
		
		// check player abilities
		if(invulnerable != WMinecraft.getPlayer().capabilities.isCreativeMode
			|| flying != (creativeFlying || wurst.mods.flightMod.isActive())
			|| immuneToFallDamage != (invulnerable
				|| wurst.mods.noFallMod.isActive())
			|| noSlowdownActive != wurst.mods.noSlowdownMod.isActive()
			|| jesus != wurst.mods.jesusMod.isActive()
			|| spider != wurst.mods.spiderMod.isActive())
			return false;
		
		// check path
		for(int i = Math.max(1, index); i < path.size(); i++)
			if(!getNeighbors(path.get(i - 1)).contains(path.get(i)))
				return false;
			
		return true;
	}
	
	public PathProcessor getProcessor()
	{
		if(flying)
			return new FlyPathProcessor(path, creativeFlying);
		
		return new WalkPathProcessor(path);
	}
	
	public void setThinkSpeed(int thinkSpeed)
	{
		this.thinkSpeed = thinkSpeed;
	}
	
	public void setThinkTime(int thinkTime)
	{
		this.thinkTime = thinkTime;
	}
	
	public void setFallingAllowed(boolean fallingAllowed)
	{
		this.fallingAllowed = fallingAllowed;
	}
}
