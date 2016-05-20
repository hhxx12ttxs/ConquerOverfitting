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
import com.crimsonrpg.api.flag.FlagPtSize;

/**
 * Changes the size of the plot.
 */
public class CommandPlotSize extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only expand plots ingame.");
            return;
        }
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        //Check if the money amount is valid
        int size = 0;
        try {
            size = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            citizen.sendError("\"" + args[0] + "\" is not a valid sum of money.");
            return;
        }
        
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to disband the plot
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.SADMIN_SIZE)) {
            citizen.sendError("You aren't allowed to size this plot.");
            return;
        }
        
        Plot closestMatch = PlotUtils.getNearestPlot(plot.getLocation(), plot);
        
        if (closestMatch != null) {
        
            //The distance the plot is from the other plot
            double realDistanceSquared = closestMatch.getDistanceSquared(plot.getLocation());

            //The distance the plot must be from the other plot
            double requiredDistance = closestMatch.getSize() + size;
            double requiredDistanceSquared = requiredDistance * requiredDistance;

            //Compare le distances
            if (!closestMatch.getLocation().equals(plot.getLocation()) &&
                realDistanceSquared < requiredDistanceSquared) {
                citizen.sendError("You can't size this plot to " + size + "; doing so would overlap " + closestMatch.getName() + ".");
                return;
            }
        
        }
        
        plot.getFlag(FlagPtSize.class).setSize(size);
        citizen.sendInfo("The size of the plot " + plot.getName() + " has been set to " + plot.getSize() + ".");
    }
    
}

