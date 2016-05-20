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
 * Deposits money into the current plot.
 */
public class CommandPlotDeposit extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        //Check if the person is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only deposit money into plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        //Check if the player said how much they wanted to deposit
        if (args.length < 1) {
            citizen.sendError("You didn't input any money!");
            return;
        }
        
        //Check if the money amount is valid
        int toDeposit = 0;
        try {
            toDeposit = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            citizen.sendError("\"" + args[0] + "\" is not a valid sum of money.");
            return;
        }
        
        //Check if the player has enough money
        FlagCzMoney wallet = citizen.getFlag(FlagCzMoney.class);
        if (wallet.getMoney() < toDeposit) {
            citizen.sendError("You don't have enough money to deposit that sum!");
            return;
        }
        
        //Check if the player is in a plot
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to deposit to the plot
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MEMBER_DEPOSIT)) {
            citizen.sendError("You aren't allowed to disband this plot.");
            return;
        }
        
        //Deposit the money
        plot.getFlag(FlagPtFunds.class).add(toDeposit);
        wallet.subtract(toDeposit);
        citizen.sendInfo("You have deposited " + toDeposit + " coins into the plot.");
    }
    
}

