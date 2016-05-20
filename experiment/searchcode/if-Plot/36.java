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
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtAdminified;

/**
 * Makes a plot server admins only.
 */
public class CommandPlotAdminify extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only downgrade plots ingame.");
            return;
        }
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to disband the plot
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.SADMIN_ADMINIFY)) {
            citizen.sendError("You aren't allowed to adminify this plot.");
            return;
        }
        FlagPtAdminified adminifyFlag = plot.getFlag(FlagPtAdminified.class);
        boolean toggle = adminifyFlag.toggle();
        citizen.sendInfo("Plot " + (toggle ? "" : "de") +"adminified.");
    }
}

