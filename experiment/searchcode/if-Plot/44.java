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
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtNPCs;
import com.crimsonrpg.util.CrimsonCommand;

/**
 * Creates a plot NPC.
 */
public class CommandPlotHireBanker extends CrimsonCommand {

    @Override
    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        //Check if the person is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("You can't hire bankers from the console.");
            return;
        }
        
        sender.sendMessage("TODO: this");
        if (true) return;
        
        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
        Plot plot = PlotUtils.getPlotFromLocation(citizen.getLocation());

        //Check if the player is in a plot
        if (plot == null) {
            citizen.sendError("You are not in a plot.");
            return;
        }

        //Check if the player named the NPC
        if (args.length < 1) {
            citizen.sendError("You did not input a name.");
            return;
        }

        //Check if the name is short enough
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(args[0]);
        for (int i = 1; i < args.length; i++) {
            nameBuilder.append(" ").append(args[i]);
        }
        String name = nameBuilder.toString();

        if (name.length() > 16) {
            citizen.sendError("NPC names can only be up to 16 characters long.");
            return;
        }

        //Check if the player is allowed to hire bankers
        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MOD_NPC_BANKER)) {
            citizen.sendError("You are not allowed to hire bankers.");
            return;
        }
return;
//        //Check if the NPC already exists
//        FlagPtNPCs npcFlag = plot.getFlag(FlagPtNPCs.class);
//        if (npcFlag.getNPC(name) != null) {
//            citizen.sendError("An NPC with that name already exists in this plot.");
//            return;
//        }
//
//        //Create the NPC
//        npcFlag.addNPC(name, citizen.getLocation(), FlagPtNPCs.BANKER);
//        citizen.sendPlotMessage("The banker " + name + " has been hired for the plot.");
    }

}

