/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 * Leaves the plot.
 */
public class CommandPlotLeave extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof SpoutPlayer)) {
            sender.sendMessage("You may only leave plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is part of the plot
        FlagPtRoles roles = plot.getFlag(FlagPtRoles.class);
        Role citizenRole = roles.getRole(citizen);
        if (citizenRole.getRank() < Role.MEMBER.getRank() ||
                citizenRole.getRank() > Role.OWNER.getRank()) {
            citizen.sendError("You are not part of this plot to begin with.");
            return;
        }
        //Kick the citizen
        Role resetRole = roles.resetRole(citizen);
        citizen.sendMessage(ChatColor.DARK_RED + "You have left " + plot.getName() + ".");
        
        //TODO: if the person is now an outsider, make them teleport to an area outside the plot.
        
        //Notify all
        PlotUtils.sendMemberMessage(plot, citizen.getName() + " has left the plot.");
    }
    
}

