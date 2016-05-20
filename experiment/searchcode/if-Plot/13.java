/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.clans.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.citizen.MessageLevel;
import com.crimsonrpg.api.clan.Clan;
import com.crimsonrpg.api.flag.FlagCnBase;
import com.crimsonrpg.api.flag.FlagCnRanks;
import com.crimsonrpg.api.flag.FlagCzClan;
import com.crimsonrpg.api.flag.FlagPtClan;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.flag.FlagPtName;
import com.crimsonrpg.util.CrimsonCommand;

/**
 * Sets a clan's base to the current plot if the plot is a clan plot.
 */
public class CommandClanSetBase extends CrimsonCommand {
	@Override
	public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You can only use this game ingame.");
			return;
		}

		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
		Clan clan = citizen.getFlag(FlagCzClan.class).getClan();

		//Check if the clan exists
		if (clan == null) {
			citizen.sendMessage("You aren't in a clan.", MessageLevel.ERROR);
			return;
		}

		//Check the player's clan privileges
		if (!clan.getFlag(FlagCnRanks.class).getRank(citizen).hasPrivilege(FlagCnRanks.Privilege.FOUNDER_SET_BASE)) {
			citizen.sendMessage("You aren't allowed to set your clan's base.", MessageLevel.ERROR);
			return;
		}

		Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());

		//Check if the plot exists
		if (plot == null) {
			citizen.sendMessage("You aren't in a plot.", MessageLevel.ERROR);
			return;
		}

		Clan plotClan = plot.getFlag(FlagPtClan.class).getClan();

		//Check if the plot is part of the clan
		if (plotClan == null || !plotClan.equals(clan)) {
			citizen.sendMessage("The plot you are in isn't owned by your clan.", MessageLevel.ERROR);
			citizen.sendMessage("DEBUG: " + plotClan + " " + clan);
			return;
		}

		//Set the clan base
		clan.getFlag(FlagCnBase.class).setPlot(plot).setSpawn(citizen.getLocation().getBlock().getLocation());
		plot.getFlag(FlagPtClan.class).setClan(clan);

		//Notify
		citizen.sendMessage("The plot base has been set to " + plot.getFlag(FlagPtName.class).getName() + ".");
	}

}

