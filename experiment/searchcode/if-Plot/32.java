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
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtFunds;

/**
 * Withdraws money from the current plot.
 */
public class CommandPlotWithdraw extends CrimsonCommand {
    
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        //Check if the person is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only withdraw from plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        //Check if the player said how much they wanted to withdraw
        if (args.length < 1) {
            citizen.sendError("You didn't input an amount!");
            return;
        }
        
        //Check if the money amount is valid
        int toWithdraw = 0;
        try {
            toWithdraw = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            citizen.sendError("\"" + args[0] + "\" is not a valid sum of money.");
            return;
        }
        
        
        //Get the player's wallet
        FlagCzMoney wallet = citizen.getFlag(FlagCzMoney.class);
        
        //Check if the player is in a plot
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to withdraw from the plot
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.ADMIN_WITHDRAW)) {
            citizen.sendError("You aren't allowed to withdraw from this plot.");
            return;
        }
        
        //Check if the plot has enough money
        if (plot.getFlag(FlagPtFunds.class).getFunds() < toWithdraw) {
            citizen.sendError("The plot does not have that much money in it.");
            return;
        }
        
        //Deposit the money
        plot.getFlag(FlagPtFunds.class).subtract(toWithdraw);
        wallet.add(toWithdraw);
        citizen.sendInfo("You have withdrawn " + toWithdraw + " coins from this plot.");
    }
    
}

