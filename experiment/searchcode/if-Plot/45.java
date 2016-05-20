/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.citizen.MessageLevel;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.flag.FlagPtPVP;

/**
 * Demotes a citizen.
 */
public class CommandPlotPVP extends CrimsonCommand {

    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You can not do this in console.");
            return;
        }

        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);

        //Check for permissions
        if (!citizen.hasPermission("crimson.rank.admin")) {
            citizen.sendMessage("You're not allowed to use this command.", MessageLevel.ERROR);
            return;
        }

        //Get the plot
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        if (plot == null) {
            citizen.sendMessage("You are not in a plot.", MessageLevel.ERROR);
            return;
        }

        //Toggle and output
        boolean pvpStatus = plot.getFlag(FlagPtPVP.class).toggle();
        citizen.sendMessage("Plot PvP " + (pvpStatus ? "enabled" : "disabled") + ".", MessageLevel.PLOT);
    }

}

