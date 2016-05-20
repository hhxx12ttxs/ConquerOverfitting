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
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtRoles;

/**
 * Promotes a citizen.
 */
public class CommandPlotPromote extends CrimsonCommand {
    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You may only promote people in plots ingame.");
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
            citizen.sendError("You can't promote an offline player.");
            return;
        }
        
        Citizen target = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) toAddPlayer);
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());
        
        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }
        
        //Check if the citizen is allowed to promote people in the plot
        Role citizenRole = PlotUtils.getRole(citizen, plot);
        if (!citizenRole.hasPrivilege(Privilege.MOD_PROMOTE)) {
            citizen.sendError("You aren't allowed to promote members in this plot.");
            return;
        }
        
        //Check if the citizen is a member of the plot
        Role toPromoteRole = PlotUtils.getRole(target, plot);
        if (toPromoteRole == null || toPromoteRole.getRank() <= Role.DEFAULT.getRank()) {
            citizen.sendError("The player you chose is not part of the plot!");
            return;
        }
        
        //Check if the citizen can promote this player
        if (toPromoteRole.getRank() + 1 >= citizenRole.getRank()) { //The +1 is to see if the promoted rank is still lower.
            citizen.sendError("You cannot promote that player any further.");
            return;
        }
        
        //Promote the citizen
        plot.getFlag(FlagPtRoles.class).promote(target);
        Role newRole = PlotUtils.getRole(target, plot);
        
        citizen.sendInfo("You have promoted " + target.getName() + " to a " + newRole.getName() + ".");
        target.sendPlotMessage("You have been promoted in " + plot.getName() + " to a " + newRole.getName() + ".");
    }
    
}

