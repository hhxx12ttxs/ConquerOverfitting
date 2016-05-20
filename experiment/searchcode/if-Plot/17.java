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
import com.crimsonrpg.api.flag.FlagCzMoney;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.flag.FlagPtSalePrice;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 * Puts your plot up for sale for the given price.
 */
public class CommandPlotBuy extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only buy plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        //Check if the player is allowed to possess another plot
        if (!citizen.hasPermission("crimson.rank.classic")) {
            int amount = PlotUtils.getPlotsOf(citizen).size();
            if (amount >= 3) {
                citizen.sendError("You aren't allowed to possess any more plots.");
            }
        }
        
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen has enough money to buy the plot
        FlagPtSalePrice salePriceFlag = plot.getFlag(FlagPtSalePrice.class);
        int salePrice = salePriceFlag.getSalePrice();
        FlagCzMoney wallet = citizen.getFlag(FlagCzMoney.class);
        int money = wallet.getMoney();
        if (money < salePrice) {
            citizen.sendError("You don't have enough money to purchase this plot; it costs " + salePrice + " coins.");
            return;
        }
        
        //Subtract money
        wallet.subtract(salePrice);
        
        //Notify previous members
        plot.getFlag(FlagPtSalePrice.class).setSalePrice(-1);
        PlotUtils.sendMemberMessage(plot, "Your plot " + plot.getName() + " has been sold to " + citizen.getName() + " for " + salePrice + " coins.");
        
        //Repossess the plot
        FlagPtRoles roleFlag = new FlagPtRoles();
        roleFlag.reset();
        roleFlag.setRole(citizen, Role.OWNER);
        plot.setFlag(roleFlag);
        
        //Notify
        citizen.sendInfo("You have bought the plot " + plot.getName() + " for " + salePrice + " coins.");
    }
    
}

