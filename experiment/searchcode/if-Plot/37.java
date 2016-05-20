/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;

/**
 * Represents the CrimsonPlots block listener.
 */
public class CPBlockListener extends BlockListener {
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) event.getPlayer());
		Plot plot = PlotUtils.getPlotFromLocation(event.getBlock().getLocation());

		if (plot != null && !PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.BUILDER_DESTROY)) {
			citizen.sendError("You're not allowed to break blocks in " + ChatColor.YELLOW + plot.getName() + ChatColor.RED + ".");
			event.setCancelled(true);
			return;
		}
	}

	@Override
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) event.getPlayer());
		Plot plot = PlotUtils.getPlotFromLocation(event.getBlock().getLocation());

		if (plot != null && !PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.BUILDER_PLACE)) {
			citizen.sendError("You're not allowed to place blocks in " + ChatColor.YELLOW + plot.getName() + ChatColor.RED + ".");
			event.setCancelled(true);
			return;
		}
	}

}

