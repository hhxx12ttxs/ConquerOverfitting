package com.computerosity.bukkit.treasurehunt.command;

import org.bukkit.Location;
import org.bukkit.World;

import com.computerosity.bukkit.treasurehunt.TreasureHunt;
import com.computerosity.bukkit.treasurehunt.TreasureHuntPlugin;
import com.computerosity.bukkit.treasurehunt.TreasureHuntPlayer;
import com.computerosity.bukkit.treasurehunt.huntitems.Clue;

// =============================================================
// Class       : TreasureHuntCommand
//
// Description : Process /thclue command
//
// Author      : Junkman
// =============================================================
public class CommandClue
{
	public CommandClue(TreasureHuntPlugin plugin)
	{
	}

	public boolean execute(TreasureHuntPlayer player, String arg)
	{
		// Check permissions
		if (!player.isPermitted("clue")) return true;

		if(player.getActiveHunt()==null)	
		{
			player.sendMessage("You're not on a treasure hunt!");
		}
		else
		{
			// TODO: This should search through memory
			double shortest = 0;
			boolean found = false;
			boolean isThisWorld = true;
			Location targetLocation = null;

			// Get the player's current position & world
			World pWorld = player.getPlayer().getWorld();
			Location pLocation = player.getPlayer().getLocation();

			// System.out.println(pWorld);
			double px = pLocation.getX();
			double py = pLocation.getY();
			double pz = pLocation.getZ();
			double ldx = 0, ldz = 0;
			
			for(Clue clue : player.getActiveHunt().getHunt().clues)
			{
				// Get clue location
				double lx = clue.location.getX(); 
				double ly = clue.location.getY(); 
				double lz = clue.location.getZ(); 
				World world = clue.location.getWorld();
				
				// Get distance from player to clue
				double dx = px-lx;
				double dy = py-ly;
				double dz = pz-lz;
				double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
				 
				// If no item selected or 
				// item is closer than last found item 
				//  or no item found and is the prize 
				if(!found || (found && distance < shortest))
				{
					targetLocation = new Location(world,lx,ly,lz);
					ldx = dx;
					ldz = dz;
					shortest = distance;
					found=true;
					isThisWorld=(world.equals(pWorld));
				}
			}
			
			// Ok, nothings found - go for the prize
			if (!found)
			{
				TreasureHunt hunt = player.getActiveHunt().getHunt();
				isThisWorld = (hunt.getPrize().location.getWorld().equals(player.getPlayer().getWorld()));
				ldx = px - hunt.getPrize().location.getX();
				ldz = pz - hunt.getPrize().location.getZ();
				targetLocation = hunt.getPrize().location;
			}

			if (!isThisWorld)
			{
				player.sendMessage("You'll need to search another world to find the next item!");
			}
			else
			{
				// Set compass target
				// TODO: configuration based, and request based?
				// TODO: Restore previous target when finished...
				if (targetLocation != null) player.getPlayer().setCompassTarget(targetLocation);

				if ((int) ldx == 0 && (int) ldz == 0)
					player.sendMessage("It's around here somewhere, could but above or below...!");
				else
				{
					String xdir = (ldx > 0) ? "north" : "south";
					String zdir = (ldz > 0) ? "east" : "west";
					player.sendMessage("Head " + (int) Math.abs(ldx) + " " + xdir + " and " + (int) Math.abs(ldz) + " " + zdir);
				}
			}
		}	

		return true;
	}
}

