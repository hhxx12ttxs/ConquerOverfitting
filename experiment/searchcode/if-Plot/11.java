/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.flaggables.api.Flag;

import com.crimsonrpg.api.flag.FlagCzMoney;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtFounder;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtLocation;
import com.crimsonrpg.api.flag.FlagPtName;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.flag.FlagPtSize;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.util.CrimsonCommand;
import com.crimsonrpg.util.StringStuff;

/**
 * Creates a plot with the size of 10.
 * This class is way longer than it should be. :/
 */
public class CommandPlotCreate extends CrimsonCommand {

    public void execute(CommandSender sender, Command cmnd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("The plot creation utility can only be used ingame.");
            return;
        }

        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) sender);

        //Check for a plot name
        if (args.length < 1) {
            citizen.sendError("You didn't enter a plot name!");
            return;
        }

        //Get the plot name from args
        String plotName = StringStuff.join(args);

        //Validate plot name
        int validation = PlotUtils.validateName(plotName);

        switch (validation) {
            case 1:
                citizen.sendError("That plot name is too long; please choose a different one.");
                return;
            case 2:
                citizen.sendError("Plot names can only consist of letters, numbers, apostrophes, and spaces.");
                return;
        }

        //Get the id
        String id = PlotUtils.getPlotHash(plotName);

        //Check if the plot name is already taken
        for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
            if (plot.getId().equals(id)) {
                citizen.sendError("Sorry, that plot name is already taken.");
                return;
            }
        }

        //Check if the player is allowed to create another plot
        if (!citizen.hasPermission("crimson.rank.classic")) {
            int amount = PlotUtils.getPlotsOf(citizen).size();
            if (amount >= 3) {
                citizen.sendError("You aren't allowed to create any more plots.");
            }
        }

        //Check if the plot is too close to other plots
        Plot closestMatch = PlotUtils.getNearestPlot(citizen.getLocation());

        if (closestMatch != null) {

            //The distance the player is from the plot
            double realDistanceSquared = closestMatch.getDistanceSquared(citizen.getLocation());

            //The distance the player must be from the plot
            double requiredDistance = (closestMatch.getSize() * closestMatch.getFlag(FlagPtLevel.class).getLevel().getInfluenceMultiplier()) + 10;
            double requiredDistanceSquared = requiredDistance * requiredDistance;

            //Compare le distances
            //TODO: "You must travel " x " north and " z " west."
            if (realDistanceSquared <= requiredDistanceSquared) {
                citizen.sendError("You can't create a plot here; you are too close to " + closestMatch.getName() + ".");
                return;
            }

        }

        //Get the plot cost
        int multiplier = 1;
        for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
            if (PlotUtils.getRole(citizen, plot).getRank() >= Role.MOD.getRank()) {
                multiplier += multiplier;
            }
        }
        int plotCost = multiplier * 1000;
        int plotDiamonds = multiplier;

        //Check money
        FlagCzMoney moneyAttribute = citizen.getFlag(FlagCzMoney.class);
        //Checks diamond count
        PlayerInventory inventory = citizen.getBukkitEntity().getInventory();
        ItemStack[] itemStacks = inventory.getContents();
        int diamondCount = 0;
        for (int i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] == null) {
                continue;
            }
            if (itemStacks[i].getType() == Material.DIAMOND) {
                diamondCount += itemStacks[i].getAmount();
            }
        }


        //Check for if the money and diamonds are a good amount
        if (moneyAttribute.getMoney() < plotCost || diamondCount < plotDiamonds) {
            citizen.sendError("You need " + plotCost + " coins and " + plotDiamonds + " diamonds to purchase a plot!");
            return;
        }

        //Create the roles flag
        FlagPtRoles rolesFlag = new FlagPtRoles();
        rolesFlag.setRole(citizen, Role.OWNER);

        //Create the size flag
        FlagPtSize sizeFlag = new FlagPtSize();
        sizeFlag.setSize(10);

        //Create the location flag
        Location location = citizen.getLocation();
        Location roundedLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        FlagPtLocation locationFlag = new FlagPtLocation();
        locationFlag.setLocation(roundedLocation);

        //Create the founder flag
        FlagPtFounder founderFlag = new FlagPtFounder();
        String founderType = "citizen";
        String founderName = citizen.getName();
        founderFlag.setData(founderType, founderName);

        //Create the name flag
        FlagPtName nameFlag = new FlagPtName();
        nameFlag.setName(plotName);

        //Create the flag list
        List<Flag> flagList = new ArrayList<Flag>();
        flagList.add(rolesFlag);
        flagList.add(sizeFlag);
        flagList.add(locationFlag);
        flagList.add(founderFlag);
        flagList.add(nameFlag);

        //Create the plot and take away the money.
        Plot createdPlot = CrimsonAPI.getPlotManager().create(id);
        createdPlot.addFlags(flagList);
        
        //Take away money
        moneyAttribute.subtract(plotCost);

        //Remove the diamonds
        int amountGivee = plotDiamonds;
        for (int i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] == null) {
                continue;
            }
            if (itemStacks[i].getType() != Material.DIAMOND) {
                continue;
            }

            if (amountGivee < itemStacks[i].getAmount()) {
                itemStacks[i].setAmount(itemStacks[i].getAmount() - amountGivee);
                diamondCount -= amountGivee;
                amountGivee = 0;
                break;

            } else if (amountGivee > itemStacks[i].getAmount()) {
                diamondCount -= itemStacks[i].getAmount();
                amountGivee -= itemStacks[i].getAmount();
                inventory.setItem(i, null);
            } else if (amountGivee == itemStacks[i].getAmount()) {
                inventory.setItem(i, null);
                break;
            }
        }

        citizen.sendInfo("You have created the plot " + ChatColor.GREEN + createdPlot.getName() + ChatColor.YELLOW + ".");

    }

}

