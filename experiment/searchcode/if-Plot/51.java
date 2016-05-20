package com.crimsonrpg.core.plots.commands;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.crimsonrpg.plots.commands;
//
//
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.getspout.spoutapi.player.SpoutPlayer;
//
//import com.crimsonrpg.citizens.api.Citizen;
//import com.crimsonrpg.citizens.api.CitizenAPI;
//import com.crimsonrpg.coreapi.npc.NPC.Type;
//import com.crimsonrpg.util.CrimsonCommand;
//import com.crimsonrpg.plots.api.Plot;
//import com.crimsonrpg.plots.api.CrimsonAPI;
//import com.crimsonrpg.plots.api.Privilege;
//import com.crimsonrpg.plots.flag.FlagNPCs;
//
///**
// * Creates a plot NPC.
// */
//public class CommandPlotHireGuard extends CrimsonCommand {
//    @Override
//    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
//        //Check if the person is a player
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("You can't hire Guards from the console.");
//            return;
//        }
//        
//        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);
//        Plot plot = CrimsonAPI.getPlotManager().getPlotFromLocation(citizen.getLocation());
//        
//        //Check if the player is in a plot
//        if (plot == null) {
//            citizen.sendError("You are not in a plot.");
//            return;
//        }
//        
//        //Check if the player named the NPC
//        if (args.length < 1) {
//            citizen.sendError("You did not input a name.");
//            return;
//        }
//        
//        //Check if the name is short enough
//        StringBuilder nameBuilder = new StringBuilder();
//        nameBuilder.append(args[0]);
//        for (int i = 1; i < args.length; i++) {
//            nameBuilder.append(" ").append(args[i]);
//        }
//        String name = nameBuilder.toString();
//        
//        if (name.length() > 16) {
//            citizen.sendError("NPC names can only be up to 16 characters long.");
//            return;
//        }
//        
//        //Check if the player is allowed to hire bankers
//        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MOD_NPC_GUARD)) {
//            citizen.sendError("You are not allowed to hire Guards.");
//            return;
//        }
//        
//        //Check if the NPC already exists
//        FlagNPCs npcFlag = plot.getFlag(FlagNPCs.class);
//        if (npcFlag.getNPC(name) != null) {
//            citizen.sendError("An NPC with that name already exists in this plot.");
//            return;
//        }
//        
//        //Create the NPC
//        npcFlag.addNPC(name, citizen.getLocation(), Type.GUARD);
//        citizen.sendPlotMessage("The Guard " + name + " has been hired for the plot.");
//    }
//}

