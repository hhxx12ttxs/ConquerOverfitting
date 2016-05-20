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
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtLevel.Level;
import com.crimsonrpg.api.flag.FlagPtRoles;

/**
 * Downgrades a plot.
 */
public class CommandPlotDowngrade extends CrimsonCommand {
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
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.OWNER_DOWNGRADE)) {
            citizen.sendError("You aren't allowed to downgrade this plot.");
            return;
        }
        
        FlagPtLevel levelFlag = plot.getFlag(FlagPtLevel.class);
        
        //Check if the plot is at a minimum level
        Level prevLevel = levelFlag.getPreviousLevel();
        if (prevLevel == null) {
            citizen.sendError("Your plot is already at the minimum level.");
            return;
        }
        
        //Check if the plot is small enough
        if (plot.getSize() > prevLevel.getMaxSize()) {
            citizen.sendError("Your plot is too big to downgrade. (Needs to have a size less than " + plot.getSize() + " blocks)");
            return;
        }
        
        //Check if there aren't too many members
        int maxMembers = prevLevel.getMaxMembers();
        if (plot.getFlag(FlagPtRoles.class).getMemberAmount() > maxMembers) {
            citizen.sendError("You currently have too many members in your plot; you can only have " + maxMembers + " in the next level down.");
        }
        
        levelFlag.downgrade();
        citizen.sendInfo("Plot " + plot.getName() + " downgraded to a " + ChatColor.WHITE + levelFlag.getLevel().getName() + ChatColor.YELLOW + ".");
    }
}

