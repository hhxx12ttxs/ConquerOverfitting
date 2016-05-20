/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.citizen.MessageLevel;
import com.crimsonrpg.api.clan.Clan;
import com.crimsonrpg.api.flag.FlagCnName;
import com.crimsonrpg.api.flag.FlagPtClan;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtFounder;
import com.crimsonrpg.api.flag.FlagPtFunds;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.flag.FlagPtSalePrice;
import com.crimsonrpg.util.StringStuff;

/**
 * Gets some information about the current plot.
 */
public class CommandPlotInfo extends CrimsonCommand {
	@Override
	public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You may only get plot information ingame.");
			return;
		}

		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
		Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());

		//Check if the player is in a plot
		if (plot == null) {
			citizen.sendError("You are not in a plot.");
			return;
		}

		int salePrice = plot.getFlag(FlagPtSalePrice.class).getSalePrice();

		//Calculate owner
		String owner = "";
		Clan clan = plot.getFlag(FlagPtClan.class).getClan();
		if (clan == null) {
			owner = StringStuff.join(plot.getFlag(FlagPtRoles.class).getMembers(Role.ADMIN).toArray(), ", ");
		} else {
			owner = clan.getFlag(FlagCnName.class).getName();
		}

		citizen.sendInfo("=== Info for " + plot.getName() + " ===");

		//General info
		citizen.sendMessage("Founder: " + ChatColor.WHITE + plot.getFlag(FlagPtFounder.class).getName(), MessageLevel.INFO);
		citizen.sendInfo("Size: " + ChatColor.WHITE + plot.getSize());
		citizen.sendInfo("Level: " + ChatColor.WHITE + plot.getFlag(FlagPtLevel.class).getLevel().getName());
		citizen.sendInfo("Distance from center: " + ChatColor.WHITE + ((int) citizen.getLocation().distance(plot.getLocation())) + " blocks");
		citizen.sendMessage("Owner(s): " + ChatColor.WHITE + owner, MessageLevel.INFO);
		if (salePrice > 0) {
			citizen.sendInfo(ChatColor.GREEN + "This plot is for sale for " + salePrice + " coins.");
		}

		//Member info
		if (PlotUtils.getRole(citizen, plot).getRank() < Role.MEMBER.getRank()) {
			return;
		}
		citizen.sendInfo("--- Member Info ---");
		citizen.sendInfo("You are a " + PlotUtils.getRole(citizen, plot).getName() + " of this plot.");
		citizen.sendInfo("Funds: " + ChatColor.WHITE + plot.getFlag(FlagPtFunds.class).getFunds());

		//Owner info
		if (PlotUtils.getRole(citizen, plot).getRank() < Role.OWNER.getRank()) {
			return;
		}
		citizen.sendInfo("--- Owner Info ---");
		citizen.sendInfo("None so far");

	}

}

