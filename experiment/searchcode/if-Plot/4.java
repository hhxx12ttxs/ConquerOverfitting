/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.api.plot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.core.plots.PlotEventFactory;
import com.crimsonrpg.api.event.plot.PlotCheckRoleEvent;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.flag.FlagPtSize;
import com.crimsonrpg.util.MD5Helper;
import org.bukkit.Location;

/**
 *
 * @author simplyianm
 */
public class PlotUtils {
    public static int getValue(Plot plot) {
        int size = plot.getFlag(FlagPtSize.class).getSize();
        int levelValue = (int) (plot.getFlag(FlagPtLevel.class).getLevel().getUpgradeCost() * 0.75);
        int sizeValue = 0;
        for (int i = 10; i <= size; i++) sizeValue += (size < 50) ? (int) (size * size * 0.5) : (int) ((size * size * 0.75) - 600);
        sizeValue *= 0.75;
        return levelValue + sizeValue;
    }
    
    public static int getExpandCost(Plot plot) {
        int newSize = (plot.getFlag(FlagPtSize.class).getSize()) + 1;
        return (newSize < 50) ? (int) (newSize * newSize * 0.5) : (int) ((newSize * newSize * 0.75) - 600);
    }
    
    public static int getInfluenceExtent(Plot plot) {
        return (int) (plot.getFlag(FlagPtSize.class).getSize() * plot.getFlag(FlagPtLevel.class).getLevel().getInfluenceMultiplier());
    }
    
    public static boolean isMember(Citizen citizen, Plot plot) {
        int playerRank = plot.getFlag(FlagPtRoles.class).getRole(citizen).getRank();
        return (playerRank >= Role.MEMBER.getRank() && playerRank <= Role.OWNER.getRank());
    }
    
    public static void sendMemberMessage(Plot plot, String message) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) player);
            if (isMember(citizen, plot)) citizen.sendPlotMessage(message);
        }
    }
    
    public static List<Plot> getPlotsOf(Citizen citizen) {
        List<Plot> partOf = new ArrayList<Plot>();
        for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
            if (isMember(citizen, plot)) partOf.add(plot);
        }
        return partOf;
    }
    
    public static int validateName(String name) {
        //Check name length
        if (name.length() > 25) {
            return 1;
        }

        //Check for alphanumeric, apostrophes and spaces
        if (!(name.matches("[a-zA-Z0-9\\'\\s]+"))) {
            return 2;
        }
        
        return -1;
    }
    
    public static String getPlotHash(String plotName) {
        return MD5Helper.md5(plotName);
    }
    
        
    /**
     * Gets a list of plots in the specified location.
     * 
     * @param location
     * @return 
     */
    public static List<Plot> getPlotsInLocation(Location location) {
        List<Plot> correspondingPlots = new ArrayList<Plot>();
        for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
            if (plot.contains(location)) correspondingPlots.add(plot);
        }
        return correspondingPlots;
    }
    
    /**
     * Gets a plot from the specified location.
     * 
     * @param location
     * @return 
     */
    public static Plot getPlotFromLocation(Location location) {
        List<Plot> matchingPlots = getPlotsInLocation(location);
        if (matchingPlots == null) return null;
        
        Plot returnPlot = null;
        
        //Gets the plot with no children.
        for (Plot plot : matchingPlots) {
            if (plot.getChildren().isEmpty()) {
                returnPlot = plot;
                break;
            }
        }
        
        //Happens when a plot has children but there are no children in the location (rare event)
        if (returnPlot == null) {
            returnPlot = getDeepestChild(matchingPlots);
        }
        
        return returnPlot;
    }
    
    /**
     * Recursive helper function; gets the leaf of the haystack of plots.
     * 
     * @param haystack
     * @return 
     */
    private static Plot getDeepestChild(List<Plot> haystack) {
        if (haystack.isEmpty()) return null;
        if (haystack.size() == 1) return haystack.get(0);
        
        //Create a new haystack and search it
        List<Plot> newHaystack = new ArrayList<Plot>(haystack);
        for (Plot plot : newHaystack) {
            Plot parent = plot.getParent();
            if (newHaystack.contains(parent)) {
                newHaystack.remove(parent);
            }
        }
        
        return getDeepestChild(newHaystack);
    }
    
    /**
     * Gets the plot closest to a location.
     * 
     * @param location
     * @return 
     */
    public static Plot getNearestPlot(Location location) {
        return getNearestPlot(location, null);
    }

    public static Plot getNearestPlot(Location location, Plot exclude) {
        //Check if the plot has expand room
        Plot closestMatch = null;
        for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
            //Ensure no cross-world crap is going on.
            if (!plot.getLocation().getWorld().equals(location.getWorld())) {
                continue;
            }
            
            if (plot.equals(exclude)) {
                continue;
            }
            
            if (closestMatch == null) {
                closestMatch = plot;
                continue;
            }
            
            if (plot.getDistanceSquared(location) < closestMatch.getDistanceSquared(location)) {
                closestMatch = plot;
            }
        }
        return closestMatch;
    }
    
    /**
     * Gets the role of a citizen within a plot.
     * 
     * @param citizen
     * @param plot
     * @return 
     */
    public static Role getRole(Citizen citizen, Plot plot) {
        PlotCheckRoleEvent event = PlotEventFactory.callPlotCheckRoleEvent(plot, citizen);
        return event.getRole();
    }
}

