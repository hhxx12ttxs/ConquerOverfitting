/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtRoles;

/**
 * Demotes a citizen.
 */
public class CommandPlotDemote extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only demote people in plots ingame.");
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
            citizen.sendError("You can't demote an offline player.");
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
        Role citizenRole = PlotUtils.getRole(citizen, plot);
        if (!citizenRole.hasPrivilege(Privilege.MOD_DEMOTE)) {
            citizen.sendError("You aren't allowed to demote members in this plot.");
            return;
        }
        
        //Check if the citizen is a member of the plot
        Role toDemoteRole = PlotUtils.getRole(target, plot);
        if (toDemoteRole == null || toDemoteRole.getRank() <= Role.DEFAULT.getRank()) {
            citizen.sendError("The player you chose is not part of the plot!");
            return;
        }
        
        //Check if the person needs to be kicked instead of demoted
        if (toDemoteRole.getRank() == Role.MEMBER.getRank()) {
            citizen.sendError("That player cannot be demoted any further; use /plotkick to kick the player from the plot.");
            return;
        }
        
        //Check if the citizen can demote this player
        if (toDemoteRole.getRank() >= citizenRole.getRank()) {
            citizen.sendError("You cannot demote that player.");
            return;
        }
        
        //Demote the citizen
        plot.getFlag(FlagPtRoles.class).demote(target);
        Role newRole = PlotUtils.getRole(target, plot);
        
        citizen.sendInfo("The player " + target.getName() + " has been demoted to a " + newRole.getName() + ".");
        target.sendMessage(ChatColor.DARK_RED + "You have been demoted in " + plot.getName() + " to a " + newRole.getName() + ".");
    }
    
}

