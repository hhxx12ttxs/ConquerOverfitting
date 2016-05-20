/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.flaggables.api.Flag;
import com.crimsonrpg.flaggables.api.GenericFlaggable;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.plot.Role;
import com.crimsonrpg.api.flag.FlagPtLocation;
import com.crimsonrpg.api.flag.FlagPtName;
import com.crimsonrpg.api.flag.FlagPtRoles;
import com.crimsonrpg.api.flag.FlagPtSize;

/**
 * Represents a plot.
 * 
 * TODO: Add delegates for lots of flags...
 */
public class SimplePlot extends GenericFlaggable implements Plot {

    /**
     * Represents the parent of this plot, if any.
     */
    protected Plot parent;

    /**
     * Represents the flags that this plot contains.
     */
    private Map<Class, Flag> flags = new HashMap<Class, Flag>();

    public SimplePlot(String id) {
        super(id);
    }

    /**
     * Gets the immediate parent of this plot.
     * 
     * @return Plot Parent
     */
    @Override
    public Plot getParent() {
        return parent;
    }

    public boolean contains(Location location) {
        int size = this.getSize();
        return this.getLocation().distanceSquared(location) <= size * size;
    }

    /**
     * Gets the size of this plot.
     * 
     * @return Size
     */
    @Override
    public int getSize() {
        return getFlag(FlagPtSize.class).getSize();
    }

    /**
     * Gets the location of this plot.
     * 
     * @return Location
     */
    @Override
    public Location getLocation() {
        return getFlag(FlagPtLocation.class).getLocation();
    }

    /**
     * Gets the distance between this plot and another location.
     * 
     * @param location
     * @return The distance between the plots
     */
    @Override
    public double getDistance(Location location) {
        return getLocation().distance(location);
    }

    /**
     * Gets the squared distance between this plot and another location.
     * 
     * @param location
     * @return The distance between the plots
     */
    @Override
    public double getDistanceSquared(Location location) {
        return getLocation().distanceSquared(location);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Plot) {
            Plot plot = (Plot) object;
            return getId().equals(plot.getId());
        }
        return false;
    }

    /**
     * Disbands the plot.
     */
    @Override
    public void disband() {
        CrimsonAPI.getPlotManager().destroy(this);
    }

    @Override
    public List<Plot> getChildren() {
        List<Plot> childList = new ArrayList<Plot>();
        for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
            if (plot.getParent() != null && plot.getParent().equals(this)) {
                childList.add(plot);
            }
        }
        return childList;
    }

    @Override
    public String getName() {
        return getFlag(FlagPtName.class).getName();
    }

}

