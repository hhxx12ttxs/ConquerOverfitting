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
import com.crimsonrpg.api.flag.FlagPtFunds;
import com.crimsonrpg.api.flag.FlagPtName;

/**
 * Renames a plot.
 */
public class CommandPlotRename extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only expand plots ingame.");
            return;
        }
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
         
        //Check for a plot name
        if (args.length < 1) {
            citizen.sendError("You didn't enter a plot name!");
            return;
        }

        //Get the plot name from args
        StringBuilder out = new StringBuilder();
        out.append(args[0]);
        if (args.length >= 2) {
            for (int i = 1; i < args.length; i++) {
                out.append(' ').append(args[i]);
            }
        }
        String plotName = out.toString();
        
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to disband the plot
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.OWNER_RENAME)) {
            citizen.sendError("You aren't allowed to rename this plot.");
            return;
        }
        
        //Check if the plot has enough money for a rename
        if (plot.getFlag(FlagPtFunds.class).getFunds() < 1000) {
            citizen.sendError("This plot does not contain enough money to be renamed! (Costs 1000 coins)");
            return;
        }
        
        
        //Check for alphanumeric and spaces
        if (!(plotName.matches("[a-zA-Z0-9\\s]+"))) {
            citizen.sendError("Plot names can only consist of letters, numbers, and spaces.");
            return;
        }        
        
        //Get the id
        String id = Integer.toHexString(plotName.toLowerCase().hashCode());
        
        //Check if the id is already taken
        for (Plot plott : CrimsonAPI.getPlotManager().getList()) {
            if (plott.getId().equals(id)) {
                citizen.sendError("Sorry, that plot name is already taken.");
                return;
            }
        }
        
        //Rename the plot
        plot.setId(id);
        plot.getFlag(FlagPtName.class).setName(plotName);
        citizen.sendInfo("Plot renamed to " + plot.getName() + ".");
    }   
}

