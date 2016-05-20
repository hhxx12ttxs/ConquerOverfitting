/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.flag.FlagCzMoney;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtFunds;
import com.crimsonrpg.api.flag.FlagPtName;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 * Triggered when a plot is disbanded.
 */
public class CommandPlotDisband extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof SpoutPlayer)) {
            sender.sendMessage("You may only disband plots ingame.");
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
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.OWNER_DISBAND)) {
            citizen.sendError("You aren't allowed to disband this plot.");
            return;
        }
        
        int value = PlotUtils.getValue(plot);
        int funds = plot.getFlag(FlagPtFunds.class).getFunds();
        
        //Do the actions
        plot.disband();
        citizen.sendInfo("Plot " + plot.getFlag(FlagPtName.class).getName() + " disbanded. You have received " + (value + funds) + " coins as compensation.");
        citizen.getFlag(FlagCzMoney.class).add(value + funds);
    }
    
}

