package com.computerosity.bukkit.treasurehunt.huntitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.computerosity.bukkit.treasurehunt.TreasureHunt;

public class Prize extends HuntItem
{
	private Location location2 = null;
	private ItemStack[] chestContents = null;
	private String lastPlayer="";
	
	public Prize(TreasureHunt _parent, Location _location, String _lastPlayer)
	{
		type = ItemType.PRIZE;
		parent = _parent;
		location = _location;
		world = location.getWorld();
		setLastPlayer(_lastPlayer);
		findAssociatedLocation();
	}

	// For double chests, find the second half if it exists
	private void findAssociatedLocation()
	{
		double tx = location.getX();
		double ty = location.getY();
		double tz = location.getZ();

		Location tmp = new Location(world, tx + 1, ty, tz);
		Block tempBlock = tmp.getBlock();
		if (tempBlock.getType() == Material.CHEST)
		{
			location2 = tmp.clone();
			return;
		}

		tmp = new Location(world, tx - 1, ty, tz);
		tempBlock = tmp.getBlock();
		if (tempBlock.getType() == Material.CHEST)
		{
			location2 = tmp.clone();
			return;
		}

		tmp = new Location(world, tx, ty, tz + 1);
		tempBlock = tmp.getBlock();
		if (tempBlock.getType() == Material.CHEST)
		{
			location2 = tmp.clone();
			return;
		}

		tmp = new Location(world, tx, ty, tz - 1);
		tempBlock = tmp.getBlock();
		if (tempBlock.getType() == Material.CHEST)
		{
			location2 = tmp.clone();
			return;
		}
	}

	public void Save()
	{

	}

	public void updateWorldFromContents()
	{
		Chest chest = getChest();
		if(chest==null) return;
		
		Inventory inv = chest.getInventory();
		for (int i = 0; i < chestContents.length; i++)
			inv.setItem(i, chestContents[i]);
	}

	public void updateContentsFromWorld()
	{
		Chest chest = getChest();
		if(chest==null) return;
		
		Inventory inv = chest.getInventory();
		chestContents = inv.getContents();
	}

	public void clearChestContents()
	{
		Chest chest = getChest();
		if(chest==null) return;
		
		Inventory inv = chest.getInventory();
		inv.clear();
	}

	@Override
	public boolean hasLocation(Location search)
	{
		// Check location
		if (location.getX() == search.getX() && location.getY() == search.getY() && location.getZ() == search.getZ()) return true;

		if (location2 != null)
		{
			// Check location
			if (location2.getX() == search.getX() && location2.getY() == search.getY() && location2.getZ() == search.getZ()) return true;
		}

		return false;
	}

	public Chest getChest()
	{
		Block block = getBlock();
		if(block.getType()==Material.CHEST)
		{
			return (Chest)block.getState();
		}
		else
			return null;
	}

	public ItemStack[] getChestContents()
	{
		return chestContents;
	}

	public String getLastPlayer()
	{
		return lastPlayer;
	}

	public void setLastPlayer(String lastPlayer)
	{
		this.lastPlayer = lastPlayer;
	}

	public void setChestContents(ItemStack[] items)
	{
		chestContents = items;
	}
}

