/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtRoles;

/**
 * Allows a player to inspect someone in their plot.
 */
public class CommandPlotInspect extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only inspect people ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        //Check if there is a name input
        if (args.length < 1) {
            citizen.sendError("You didn't input a name!");
            return;
        }
        
        String toInspectName = args[0];
        Player inspectedPlayer = Bukkit.getServer().getPlayer(toInspectName);
        
        //Get the real name if the player is online
        if (inspectedPlayer != null) {
            toInspectName = inspectedPlayer.getName();
        }
        
        //Check if the player is in a plot
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to inspect players in the plot.
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MEMBER_INSPECT)) {
            citizen.sendError("You aren't allowed to inspect members to this plot.");
            return;
        }
        
        //Send the inspected's info
        citizen.sendPlotMessage("=== Info for " + toInspectName + " ===");
        citizen.sendPlotMessage("Role: " + plot.getFlag(FlagPtRoles.class).getRole(toInspectName).getName());
    }
}

