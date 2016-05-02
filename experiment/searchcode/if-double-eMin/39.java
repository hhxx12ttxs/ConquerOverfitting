/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 * 
 * changed by inori 03/17/2012
 * line 91:limits MaxHeight changed to 255
 */
public class PermissionGroup {
        protected int xmax;
        protected int ymax;
        protected int zmax;
        protected int resmax;
        protected double costperarea;
        protected boolean tpaccess;
        protected int subzonedepth;
        protected FlagPermissions flagPerms;
        protected Map<String,Boolean> creatorDefaultFlags;
        protected Map<String,Map<String,Boolean>> groupDefaultFlags;
        protected Map<String,Boolean> residenceDefaultFlags;
        protected boolean messageperms;
        protected String defaultEnterMessage;
        protected String defaultLeaveMessage;
        protected int maxLeaseTime;
        protected int leaseGiveTime;
        protected double renewcostperarea;
        protected boolean canBuy;
        protected boolean canSell;
        protected boolean buyIgnoreLimits;
        protected boolean cancreate;
        protected String groupname;
        protected int maxPhysical;
        protected boolean unstuck;
        protected int minHeight;
        protected int maxHeight;
        protected int maxRents;
        protected int maxRentables;
        protected boolean selectCommandAccess;
        protected boolean itemListAccess;

        public PermissionGroup(String name)
        {
            flagPerms = new FlagPermissions();
            creatorDefaultFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
            residenceDefaultFlags = Collections.synchronizedMap(new HashMap<String,Boolean>());
            groupDefaultFlags = Collections.synchronizedMap(new HashMap<String,Map<String,Boolean>>());
            groupname = name;
        }
        
        public PermissionGroup(String name, ConfigurationSection node)
        {
            this(name);
            this.parseGroup(node);
        }

        public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms)
        {
            this(name,node);
            flagPerms.setParent(parentFlagPerms);
        }

    private void parseGroup(ConfigurationSection limits) {
        if(limits == null)
            return;
        cancreate = limits.getBoolean("Residence.CanCreate", false);
        resmax = limits.getInt("Residence.MaxResidences", 0);
        maxPhysical = limits.getInt("Residence.MaxAreasPerResidence",2);
        xmax = limits.getInt("Residence.MaxEastWest", 0);
        ymax = limits.getInt("Residence.MaxUpDown", 0);
        zmax = limits.getInt("Residence.MaxNorthSouth", 0);
        minHeight = limits.getInt("Residence.MinHeight", 0);
        maxHeight = limits.getInt("Residence.MaxHeight", 255);
        tpaccess = limits.getBoolean("Residence.CanTeleport", false);
        subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);
        messageperms = limits.getBoolean("Messaging.CanChange", false);
        defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
        defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
        maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
        leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
        maxRents = limits.getInt("Rent.MaxRents", 0);
        maxRentables = limits.getInt("Rent.MaxRentables", 0);
        renewcostperarea = limits.getDouble("Economy.RenewCost", 0.02D);
        canBuy = limits.getBoolean("Economy.CanBuy", false);
        canSell = limits.getBoolean("Economy.CanSell", false);
        buyIgnoreLimits = limits.getBoolean("Economy.IgnoreLimits", false);
        costperarea = limits.getDouble("Economy.BuyCost", 0);
        unstuck = limits.getBoolean("Residence.Unstuck", false);
        selectCommandAccess = limits.getBoolean("Residence.SelectCommandAccess", true);
        itemListAccess = limits.getBoolean("Residence.ItemListAccess", true);
        ConfigurationSection node = limits.getConfigurationSection("Flags.Permission");
        Set<String> flags = null;
        if(node!=null)
            flags = node.getKeys(false);
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.Permission." + flagname, false);
                flagPerms.setFlag(flagname, access ? FlagState.TRUE : FlagState.FALSE);
            }
        }
        node = limits.getConfigurationSection("Flags.CreatorDefault");
        if(node!=null)
            flags = node.getKeys(false);
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.CreatorDefault." + flagname, false);
                creatorDefaultFlags.put(flagname, access);
            }

        }
        node = limits.getConfigurationSection("Flags.Default");
        if(node!=null)
            flags = node.getKeys(false);
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.Default." + flagname, false);
                residenceDefaultFlags.put(flagname, access);
            }
        }
        node = limits.getConfigurationSection("Flags.GroupDefault");
        Set<String> groupDef = null;
        if(node!=null)
            groupDef = node.getKeys(false);
        if (groupDef != null) {
            Iterator<String> groupit = groupDef.iterator();
            while (groupit.hasNext()) {
                String name = groupit.next();
                Map<String, Boolean> gflags = new HashMap<String, Boolean>();
                flags = limits.getConfigurationSection("Flags.GroupDefault." + name).getKeys(false);
                Iterator<String> flagit = flags.iterator();
                while (flagit.hasNext()) {
                    String flagname = flagit.next();
                    boolean access = limits.getBoolean("Flags.GroupDefault." + name + "." + flagname, false);
                    gflags.put(flagname, access);
                }
                groupDefaultFlags.put(name, gflags);
            }
        }
    }

    public int getMaxX() {
        return xmax;
    }

    public int getMaxY() {
        return ymax;
    }

    public int getMaxZ() {
        return zmax;
    }

    public int getMinHeight()
    {
        return minHeight;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public int getMaxZones() {
        return resmax;
    }
    public double getCostPerBlock()
    {
        return costperarea;
    }
    public boolean hasTpAccess()
    {
        return tpaccess;
    }
    public int getMaxSubzoneDepth()
    {
        return subzonedepth;
    }
    public boolean canSetEnterLeaveMessages()
    {
        return messageperms;
    }
    public String getDefaultEnterMessage()
    {
        return defaultEnterMessage;
    }
    public String getDefaultLeaveMessage()
    {
        return defaultLeaveMessage;
    }
    public int getMaxLeaseTime()
    {
        return maxLeaseTime;
    }
    public int getLeaseGiveTime()
    {
        return leaseGiveTime;
    }
    public double getLeaseRenewCost()
    {
        return renewcostperarea;
    }
    public boolean canBuyLand()
    {
        return canBuy;
    }
    public boolean canSellLand()
    {
        return canSell;
    }
    public int getMaxRents()
    {
        return maxRents;
    }
    public int getMaxRentables()
    {
        return maxRentables;
    }
    public boolean buyLandIgnoreLimits()
    {
        return buyIgnoreLimits;
    }
    public boolean hasUnstuckAccess()
    {
        return unstuck;
    }
    public int getMaxPhysicalPerResidence()
    {
        return maxPhysical;
    }
    public Set<Entry<String,Boolean>> getDefaultResidenceFlags()
    {
        return residenceDefaultFlags.entrySet();
    }
    public Set<Entry<String,Boolean>> getDefaultCreatorFlags()
    {
        return creatorDefaultFlags.entrySet();
    }
    public Set<Entry<String,Map<String,Boolean>>> getDefaultGroupFlags()
    {
        return groupDefaultFlags.entrySet();
    }

    public boolean canCreateResidences()
    {
        return cancreate;
    }
    public boolean hasFlagAccess(String flag)
    {
        return flagPerms.has(flag, false);
    }

    public boolean inLimits(CuboidArea area)
    {
        if(area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax)
        {
            return false;
        }
        return true;
    }

    public boolean selectCommandAccess()
    {
        return selectCommandAccess;
    }

    public boolean itemListAccess()
    {
        return itemListAccess;
    }

    public void printLimits(Player player)
    {
        player.sendMessage("§7---------------------------");
        player.sendMessage("§ePermissions Group:§3 "+Residence.getPermissionManager().getPermissionsGroup(player));
        player.sendMessage("§eResidence Group:§3 "+groupname);
        player.sendMessage("§eResidence Admin:§3 " + Residence.getPermissionManager().isResidenceAdmin(player));
        player.sendMessage("§eCan Create Residences:§3 "+cancreate);
        player.sendMessage("§eMax Residences:§3 "+resmax);
        player.sendMessage("§eMax East/West Size:§3 "+xmax);
        player.sendMessage("§eMax North/South Size:§3 "+zmax);
        player.sendMessage("§eMax Up/Down Size:§3 "+ymax);
        player.sendMessage("§eMin/Max Protection Height:§3 "+minHeight+ " to " + maxHeight);
        player.sendMessage("§eMax Subzone Depth:§3 "+subzonedepth);
        player.sendMessage("§eCan Set Enter/Leave Messages:§3 "+messageperms);
        player.sendMessage("§eNumber of Residences you own:§3 " + Residence.getResidenceManager().getOwnedZoneCount(player.getName()));
        if(Residence.getEconomyManager()!=null)
            player.sendMessage("§eResidence Cost Per Block:§3 " + costperarea);
        player.sendMessage("§eFlag Permissions:§3 " + flagPerms.listFlags());
        if(Residence.getConfigManager().useLeases())
        {
            player.sendMessage("§eMax Lease Days:§3 " + maxLeaseTime);
            player.sendMessage("§eLease Time Given on Renew:§3 " + leaseGiveTime);
            player.sendMessage("§eRenew Cost Per Block:§3 " + renewcostperarea);
        }
        player.sendMessage("§7---------------------------");
    }

}

