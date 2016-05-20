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
import com.crimsonrpg.api.flag.FlagPtFunds;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtLevel.Level;

/**
 *
 * @author simplyianm
 */
public class CommandPlotUpgrade extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only upgrade plots ingame.");
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
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.OWNER_UPGRADE)) {
            citizen.sendError("You aren't allowed to upgrade this plot.");
            return;
        }
        
        FlagPtLevel levelFlag = plot.getFlag(FlagPtLevel.class);
        
        //Check if the plot is maximum size
        Level nextLevel = levelFlag.getNextLevel();
        if (nextLevel == null) {
            citizen.sendError("Your plot is already at the maximum level.");
            return;
        }
        
        //Check funds
        FlagPtFunds funds = plot.getFlag(FlagPtFunds.class);
        int coinsUpgrade = nextLevel.getUpgradeCost();
        if (funds.getFunds() < coinsUpgrade) {
            citizen.sendError("Your plot does not have enough funds to be upgraded. (Need " + nextLevel.getUpgradeCost() +")");
            return;
        }
        
        //Check if the plot is big enough
        if (nextLevel.getMinSize() > plot.getSize()) {
            citizen.sendError("Your plot is not big enough to be upgraded.");
            return;
        }
        
        //Subtract the funds and upgrade
        funds.subtract(coinsUpgrade);
        levelFlag.upgrade();
        citizen.sendInfo("Plot " + plot.getName() + " upgraded to a " + ChatColor.WHITE + levelFlag.getLevel().getName() + ChatColor.YELLOW + ".");
    }
    
}

