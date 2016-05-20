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
//import com.crimsonrpg.util.CrimsonCommand;
//import com.crimsonrpg.plots.api.Plot;
//import com.crimsonrpg.plots.api.CrimsonAPI;
//import com.crimsonrpg.plots.api.Privilege;
//import com.crimsonrpg.plots.flag.FlagNPCs;
//
///**
// * Creates a plot NPC.
// */
//public class CommandPlotFire extends CrimsonCommand {
//    @Override
//    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
//        //Check if the person is a player
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("You can't fire NPCs from the console.");
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
//        String name = args[0];
//        if (name.length() > 16) {
//            citizen.sendError("That is an impossible NPC name.");
//            return;
//        }
//        
//        //Check if the player is allowed to fire NPCs
//        if (!PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.MOD_FIRE_NPC)) {;
//            citizen.sendError("You are not allowed to fire NPCs.");
//            return;
//        }
//        
//        //Check if the NPC exists
//        FlagNPCs npcsFlag = plot.getFlag(FlagNPCs.class);
//        NPC theNPC = npcsFlag.getNPC(name);
//        if (theNPC == null) {
//            citizen.sendError("That NPC does not exist. Are you sure you typed the name in correctly?");
//            return;
//        }
//        
//        //Fire the npc
//        npcsFlag.removeNPC(name); //emovebitur.
//        citizen.sendInfo("You have fired " + name + " from the plot. Now the poor fellow is one of the 9 percent! :(");
//    }
//    
//}

