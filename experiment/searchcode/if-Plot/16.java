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
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtSalePrice;

/**
 * Puts your plot up for sale for the given price.
 */
public class CommandPlotSell extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only sell plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        if (args.length < 1) {
            citizen.sendError("You didn't input a sale price.");
            return;
        }
        
        String potentialSalePrice = args[0];
        int salePrice = 0;
        try {
             salePrice = Integer.parseInt(potentialSalePrice);
        } catch (NumberFormatException e) {
            citizen.sendError("The sale price you used is not valid.");
            return;
        }
        
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to sell the plot
        Role citizenRole = PlotUtils.getRole(citizen, plot);
        if (!citizenRole.hasPrivilege(Privilege.OWNER_SELL)) {
            citizen.sendError("You aren't allowed to sell this plot.");
            return;
        }
        
        //Put the plot up for sale
        plot.getFlag(FlagPtSalePrice.class).setSalePrice(salePrice);
        citizen.sendInfo("Your plot " + plot.getName() + " has successfully been put up for sale.");
    }
    
}

