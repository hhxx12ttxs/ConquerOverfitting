package com.computerosity.bukkit.treasurehunt.huntitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.computerosity.bukkit.treasurehunt.TreasureHunt;

// =============================================================
// Class       : TreasureHuntItem
//
// Description : A component of a treasure hunt
//
// Author      : Junkman
// =============================================================
public class Clue extends HuntItem
{
	private boolean isStart = false;

	public Clue(TreasureHunt _parent, Location _location)
	{
		type = ItemType.CLUE;
		parent = _parent;
		itemid = parent.generateUID();
		location = _location;
		world = location.getWorld();
		isStart = false;

		// Update sign text
		setSignText();
	}

	public Clue(TreasureHunt _parent, Location _location, Integer _itemid, boolean _isStart)
	{
		type = ItemType.CLUE;
		parent = _parent;
		itemid = _itemid;
		if (_itemid == null)
			itemid = parent.generateUID();
		else
			itemid = _itemid;
		location = _location;
		world = location.getWorld();
		isStart = _isStart;

		// Update sign text
		setSignText();
	}

	public void setIsStart(boolean start)
	{
		isStart = start;
		setSignText();
	}

	public boolean getIsStart()
	{
		return isStart;
	}

	public int getItemId()
	{
		return itemid;
	}

	// TODO: Splitting the name over lines could probably be optimised..
	public void setSignText()
	{
		Block block = getBlock();
		if (block == null) return;

		// Check block is a sign
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) return;

		Sign sign = (Sign) block.getState();

		// If length is over 15 characters, split over lines
		String huntName = parent.getName();
		if (huntName.length() > 15)
		{
			String words[] = huntName.split(" ");
			String lines[] =
			{
					"", "", ""
			};
			int idx = 0;
			for (int i = 0; i < words.length; i++)
			{
				if (idx < 3)
				{
					// If the word is over 15 characters, just put it straight
					// in
					if (words[i].length() > 15)
					{
						lines[idx] = words[i];
						idx++;
					}
					else
					{
						// If this word, and the current line are less than 15,
						// concatenate them
						if (lines[idx].length() + words[i].length() < 15)
						{
							lines[idx] += " " + words[i];
						}
						else
						{
							// Otherwise go onto next line
							idx++;
							if (idx < 3) lines[idx] = words[i];
						}
					}
				}
			}

			// Setup the sign text
			if (lines[2] != "")
			{
				sign.setLine(0, lines[0]);
				sign.setLine(1, lines[1]);
				sign.setLine(2, lines[2]);
			}
			else
			{
				sign.setLine(0, "TREASURE HUNT");
				sign.setLine(1, lines[0]);
				sign.setLine(2, lines[1]);
			}
		}
		else
		{
			sign.setLine(0, "TREASURE HUNT");
			sign.setLine(1, huntName);
			sign.setLine(2, "");
		}

		if (isStart)
			sign.setLine(3, "secret: " + parent.getSecret());
		else
			sign.setLine(3, "");

		sign.update(true);
	}

	@Override
	public boolean hasLocation(Location search)
	{
		if (location.getX() == search.getX() && location.getY() == search.getY() && location.getZ() == search.getZ())
			return true;
		else
			return false;
	}
}

