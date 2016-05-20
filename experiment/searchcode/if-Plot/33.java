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
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtRoles;

/**
 * Kicks a person from your plot.
 */
public class CommandPlotKick extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only kick people out of plots ingame.");
            return;
        }
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        
        if (args.length < 1) {
            citizen.sendError("You didn't input a name!");
            return;
        }
        
        String toAddName = args[0];
        Player toAddPlayer = Bukkit.getServer().getPlayer(toAddName);
        
        if (toAddPlayer == null) {
            citizen.sendError("You can't kick an offline player.");
            return;
        }
        
        Citizen target = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) toAddPlayer);
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to invite people to the plot
        FlagPtRoles rolesFlag = plot.getFlag(FlagPtRoles.class);
        Role citizenRole = rolesFlag.getRole(citizen);
        if (!citizenRole.hasPrivilege(Privilege.MOD_KICK)) {
            citizen.sendError("You aren't allowed to kick members out of this plot.");
            return;
        }
        
        //Check if the citizen is a member of the plot
        Role toKickRole = rolesFlag.getRole(target);
        if (toKickRole == null || toKickRole.getRank() <= Role.DEFAULT.getRank()) {
            citizen.sendError("The player you chose is not part of the plot!");
            return;
        }
        
        //Check if the role of toKick is lower than the role of the kicker
        if (toKickRole.getRank() >= citizenRole.getRank()) {
            citizen.sendError("You aren't allowed to kick a player equal to or higher than your rank!");
            return;
        }
        
        //Kick the citizen
        rolesFlag.resetRole(target);
        target.sendMessage(ChatColor.DARK_RED + "You have been kicked out of " + plot.getName() + ".");
        
    }
    
}

