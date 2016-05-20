/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.api.flag.FlagCzLastPlotRequest;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 * Allows a player to invite someone to their plot.
 */
public class CommandPlotInvite extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only invite people to plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        if (args.length < 1) {
            citizen.sendError("You didn't input a name!");
            return;
        }
        
        String targetName = args[0];
        Player targetPlayer = Bukkit.getServer().getPlayer(targetName);
        
        if (targetPlayer == null) {
            citizen.sendError("You can't add an offline player.");
            return;
        }
        
        Citizen target = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) targetPlayer);
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to invite people to the plot
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MOD_ADD_MEMBERS)) {
            citizen.sendError("You aren't allowed to add members to this plot.");
            return;
        }
        
        //Check if people can still be added to the plot
        if (plot.getFlag(FlagPtRoles.class).getMemberAmount() >= plot.getFlag(FlagPtLevel.class).getLevel().getMaxMembers()) {
            citizen.sendError("You aren't allowed to add any more members to this plot.");
            return;
        }
        
        //Check if the added person is allowed to join another plot
        if (!target.hasPermission("crimson.rank.classic")&& PlotUtils.getPlotsOf(target).size() >= 3) {
            citizen.sendError("That player isn't allowed to join any more plots.");
            return;
        }
        
        //Send the request
        target.getFlag(FlagCzLastPlotRequest.class).setLastPlot(plot);
        target.sendInfo("You have been invited to the plot " + plot.getName() + ". To accept, type '/acceptplot'.");
        citizen.sendInfo("An invitation to " + plot.getName() + " was sent to " + target.getName() + ".");
    }
    
}

