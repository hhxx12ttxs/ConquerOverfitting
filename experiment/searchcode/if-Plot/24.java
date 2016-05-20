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
import com.crimsonrpg.api.flag.FlagCzLastPlotRequest;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 * Accepts an invite to a plot.
 */
public class CommandPlotAccept extends CrimsonCommand {
    
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only invite people to plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        //Check if the player was invited
        FlagCzLastPlotRequest lastPlotRequestAttribute = citizen.getFlag(FlagCzLastPlotRequest.class);
        Plot plot = lastPlotRequestAttribute.getLastPlot();
        if (plot == null) {
            citizen.sendError("You have not been invited to a plot.");
            return;
        }
        
        //Add the member to the plot
        FlagPtRoles roles = plot.getFlag(FlagPtRoles.class);
        roles.setRole(citizen, Role.MEMBER);
        
        //Notify all
        PlotUtils.sendMemberMessage(plot, citizen.getName() + " has joined the plot " + plot.getName() + ".");
        
        citizen.sendInfo("You are now a member of " + plot.getName() + ".");
        
        lastPlotRequestAttribute.setLastPlot(null);
    }
    
}

