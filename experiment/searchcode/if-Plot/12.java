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
import com.crimsonrpg.api.flag.FlagCnMoney;
import com.crimsonrpg.api.flag.FlagCnName;
import com.crimsonrpg.api.flag.FlagCnRanks;
import com.crimsonrpg.api.flag.FlagCzClan;
import com.crimsonrpg.api.flag.FlagPtClan;
import com.crimsonrpg.flaggables.api.Flag;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.flag.FlagPtFounder;
import com.crimsonrpg.api.flag.FlagPtLocation;
import com.crimsonrpg.api.flag.FlagPtName;
import com.crimsonrpg.api.flag.FlagPtSize;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.util.StringStuff;
import java.util.ArrayList;
import org.bukkit.Location;

/**
 * Creates a plot for the clan.
 */
public class CommandClanPlot extends CrimsonCommand {
	@Override
	public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You can only use this game ingame.");
			return;
		}

		Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);

		if (args.length < 1) {
			citizen.sendMessage("You didn't specify a plot name.", MessageLevel.ERROR);
			return;
		}

		//Check if the plot name is valid
		String plotName = StringStuff.join(args);
		if (PlotUtils.validateName(plotName) > 0) {
			citizen.sendMessage("That is an invalid plot name.", MessageLevel.ERROR);
			return;
		}

		Clan clan = citizen.getFlag(FlagCzClan.class).getClan();

		//Check if the clan exists
		if (clan == null) {
			citizen.sendMessage("You aren't in a clan.", MessageLevel.ERROR);
			return;
		}

		//Check clan privileges
		if (!clan.getFlag(FlagCnRanks.class).getRank(citizen).hasPrivilege(FlagCnRanks.Privilege.MEMBER_CREATE_PLOT)) {
			citizen.sendMessage("You're not allowed to create plots for your clan.", MessageLevel.ERROR);
			return;
		}

		//Check the clan's money
		FlagCnMoney clanMoneyFlag = clan.getFlag(FlagCnMoney.class);
		int money = clanMoneyFlag.getMoney();

		//TODO: make this exponentially more expensive
		if (money < 5000) {
			citizen.sendMessage("Your clan needs 5000 coins to purchase a plot.", MessageLevel.ERROR);
			return;
		}

		//Check if the name is taken
		String id = PlotUtils.getPlotHash(plotName);

		//Check if the plot name is already taken
		for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
			if (plot.getId().equals(id)) {
				citizen.sendMessage("Sorry, that plot name is already taken.", MessageLevel.ERROR);
				return;
			}
		}

		//Make the plot

		//Name flag
		final FlagPtName nameFlag = new FlagPtName();
		nameFlag.setName(plotName);

		//Clan flag
		final FlagPtClan clanFlag = new FlagPtClan();
		clanFlag.setClan(clan);

		//Founder flag
		final FlagPtFounder founderFlag = new FlagPtFounder();
		founderFlag.setFounderType("clan");
		founderFlag.setName(clan.getFlag(FlagCnName.class).getName());

		//Size flag
		final FlagPtSize sizeFlag = new FlagPtSize();
		sizeFlag.setSize(10);

		//Location flag
		Location location = citizen.getLocation();
		Location roundedLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		final FlagPtLocation locationFlag = new FlagPtLocation();
		locationFlag.setLocation(roundedLocation);

		//Create the plot
		Plot plot = CrimsonAPI.getPlotManager().create(id);
		plot.addFlags(new ArrayList<Flag>() {
			{
				add(nameFlag);
				add(clanFlag);
				add(sizeFlag);
				add(founderFlag);
				add(locationFlag);
			}

		});

		//Notify
		citizen.sendMessage("The plot '" + plotName + "' has been created for your clan.", MessageLevel.INFO);
	}

}

