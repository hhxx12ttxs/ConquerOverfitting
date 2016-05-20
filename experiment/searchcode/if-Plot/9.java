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
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtFunds;
import com.crimsonrpg.api.flag.FlagPtName;
import com.crimsonrpg.api.flag.FlagPtSize;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 *
 * @author simplyianm
 */
public class CommandPlotExpand extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only expand plots ingame.");
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
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.ADMIN_EXPAND)) {
            citizen.sendError("You aren't allowed to expand this plot.");
            return;
        }
        
        //Check if the plot has enough money to expand
        int expandCost = PlotUtils.getExpandCost(plot);
        FlagPtFunds plotFunds = plot.getFlag(FlagPtFunds.class);
        if (plotFunds.getFunds() < expandCost) {
            citizen.sendError("This plot does not contain enough money to expand! (Costs " + expandCost + " coins)");
            return;
        }
        
        Plot closestMatch = PlotUtils.getNearestPlot(plot.getLocation(), plot);
        
        if (closestMatch != null) {
        
            //The distance the plot is from the other plot
            double realDistanceSquared = closestMatch.getDistanceSquared(plot.getLocation());

            //The distance the plot must be from the other plot
            double requiredDistance = closestMatch.getSize() + plot.getSize() + 1;
            double requiredDistanceSquared = requiredDistance * requiredDistance;

            //Compare le distances
            if (!closestMatch.getLocation().equals(plot.getLocation()) &&
                    realDistanceSquared < requiredDistanceSquared) {
                citizen.sendError("You can't expand this plot; doing so would overlap " + closestMatch.getName() + ".");
                return;
            }
        }
        
        plot.getFlag(FlagPtSize.class).expand(1);
        plot.getFlag(FlagPtFunds.class).subtract(expandCost);
        citizen.sendInfo("You have expanded " + plot.getFlag(FlagPtName.class).getName() + " by 1.");
    }
    
    
}

