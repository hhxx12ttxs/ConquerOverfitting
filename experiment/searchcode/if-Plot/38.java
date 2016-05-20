/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.listener;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.citizen.MessageLevel;
import com.crimsonrpg.core.plots.CrimsonPlots;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtName;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Represents the PlayerListener for CrimsonPlots
 */
public class CPPlayerListener extends PlayerListener {
	/**
	 * TODO: put this in a session attribute
	 */
	private Map<Citizen, Block> citizenLocs = new HashMap<Citizen, Block>();

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) event.getPlayer());
		String pname = citizen.getName();
		Block oldBlock;
		Block newBlock = citizen.getLocation().getBlock();
		oldBlock = citizenLocs.containsKey(citizen) == true ? citizenLocs.get(citizen) : newBlock;
		citizenLocs.put(citizen, newBlock);

		//Check if the player is in a new block
		if (oldBlock != newBlock) {
			Plot oldPlot = PlotUtils.getPlotFromLocation(oldBlock.getLocation());
			Plot newPlot = PlotUtils.getPlotFromLocation(newBlock.getLocation());

			if (newPlot != null && !PlotUtils.getRole(citizen, newPlot).hasPrivilege(Privilege.GUEST_ENTER)) {
				Location oldLoc = oldBlock.getLocation();
				float newYaw = citizen.getLocation().getYaw();
				float newPitch = citizen.getLocation().getPitch();
				citizen.getBukkitEntity().teleport(new Location(oldBlock.getWorld(), oldLoc.getBlockX(), oldLoc.getBlockY(), oldLoc.getBlockZ(), newYaw, newPitch));
				citizen.sendError("You can't enter this plot.");
				citizenLocs.put(citizen, oldBlock);
				return;
			}

			//Walking through wilderness
			if (oldPlot == null && newPlot == null) {
				//citizen.sendMessage("You are in wilderness.");
				//Into newPlot
			} else if (oldPlot == null && newPlot != null) {
				citizen.sendMessage("You have entered " + newPlot.getFlag(FlagPtName.class).getName() + ".");

				//Out of oldPlot
			} else if (oldPlot != null && newPlot == null) {
				citizen.sendMessage("You have left " + oldPlot.getFlag(FlagPtName.class).getName() + " and entered the wilderness.");

				//Through oldPlot or newPlot
			} else if (oldPlot.equals(newPlot)) {
				//player.sendMessage("You are moving through " + newPlot + ".");
				//Between oldPlot and newPlot
			} else if (!oldPlot.equals(newPlot)) {
				citizen.sendMessage("You have left " + oldPlot.getFlag(FlagPtName.class).getName() + " and entered " + newPlot.getFlag(FlagPtName.class).getName() + ".");

				//Shouldn't happen
			} else {
				CrimsonPlots.LOGGER.warning("[Crimson] Error with plot movement");
			}
		}
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) event.getPlayer());

		//Check for flint and steel
		if (citizen.getBukkitEntity().getItemInHand().getType().equals(Material.FLINT_AND_STEEL)) {
			Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
			if (plot != null && !PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.BUILDER_FIRE)) {
				citizen.sendMessage("You can't use flint and steel in this plot.", MessageLevel.ERROR);
				event.setCancelled(true);
				return;
			}
		}

		//Check if they are trying to use something they aren't allowed to use
		int interactedWith = event.getClickedBlock().getTypeId();
		if (interactedWith == Material.LEVER.getId() || interactedWith == Material.STONE_BUTTON.getId()) {
			Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());

			if (plot != null && !PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MEMBER_USE)) {
				citizen.sendError("You can't use that.");
				event.setCancelled(true);
				return;
			}
		}

	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) player);
		boolean bed = event.isBedSpawn();
		Location spawn = event.getPlayer().getWorld().getSpawnLocation();
		if (bed == true) {
			event.setRespawnLocation(spawn);
			citizen.sendMessage("Your bed respawn has been cancelled; you can only spawn in your faction city.", MessageLevel.INFO);
		}
	}

}

