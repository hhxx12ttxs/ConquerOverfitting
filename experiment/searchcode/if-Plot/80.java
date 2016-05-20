package com.n9works.bukkit.towns;

import com.n9works.bukkit.ConfigFile;
import com.n9works.bukkit.CustomEffect;
import com.n9works.bukkit.TheArtifact;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class SpawnPlot implements Listener {
    private final Logger log = Logger.getLogger("Plots");
    private final TheArtifact plugin;
    private final Map<String, Block> plotSigns = new HashMap<>();
    private ConfigFile config;

    public SpawnPlot(TheArtifact plugin) {
        this.plugin = plugin;

        plotSigns.put("plot1", plugin.world.getBlockAt(-17, 65, -89));
        plotSigns.put("plot2", plugin.world.getBlockAt(-17, 66, -70));
        plotSigns.put("plot3", plugin.world.getBlockAt(-21, 66, -53));
        plotSigns.put("plot4", plugin.world.getBlockAt(-13, 66, -37));
        plotSigns.put("plot5", plugin.world.getBlockAt(-14, 70, -17));
        plotSigns.put("plot6", plugin.world.getBlockAt(-14, 73, -1));
        plotSigns.put("plot7", plugin.world.getBlockAt(-14, 79, 14));
        plotSigns.put("plot8", plugin.world.getBlockAt(-14, 83, 30));
        plotSigns.put("plot9", plugin.world.getBlockAt(8, 80, 23));
        plotSigns.put("plot10", plugin.world.getBlockAt(9, 74, 5));
        plotSigns.put("plot11", plugin.world.getBlockAt(9, 72, -15));
        plotSigns.put("plot12", plugin.world.getBlockAt(3, 66, -34));
        plotSigns.put("plot13", plugin.world.getBlockAt(3, 64, -57));
        plotSigns.put("plot14", plugin.world.getBlockAt(2, 64, -74));
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin wgPlugin = plugin.server.getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) wgPlugin;
    }

    public void load() {
        config = new ConfigFile(plugin, "plots.yml");

        for (String plot : plotSigns.keySet()) {
            Block b = plotSigns.get(plot);
            if (!b.getType().equals(Material.SIGN_POST)) continue;


            if (getPlotByTown(plot) == null) {
                //Available plot
                Sign sign = (Sign) plotSigns.get(plot).getState();
                sign.setLine(0, ChatColor.DARK_AQUA + plot);
                sign.setLine(1, ChatColor.GREEN + "-Available-");
                sign.setLine(2, ChatColor.BLUE + "Break sign");
                sign.setLine(3, ChatColor.BLUE + "to claim!");
                sign.update();
            } else {
                Sign sign = (Sign) plotSigns.get(plot).getState();
                sign.setLine(0, ChatColor.DARK_AQUA + plot);
                sign.setLine(1, ChatColor.RED + "-Claimed-");
                sign.setLine(2, "");
                sign.setLine(3, "");
                sign.update();
            }
        }

        log.info("Spawn Plots loaded");
    }

    String getPlotByTown(String plot) {
        return config.get("plots." + plot + ".town", null);
    }

    public String getTownPlot(String town) {
        for (String plot : plotSigns.keySet()) {
            String townPlot = getPlotByTown(plot);
            if (townPlot != null && townPlot.equals(town)) return plot;
        }
        return null;
    }

    Boolean hasTownPlot(String town) {
        for (String plot : config.getAll("plots")) {
            String townPlot = config.get("plots." + plot + ".town", null);
            if (townPlot != null && townPlot.equals(town)) return true;
        }
        return false;
    }

    void claimPlot(String plot, String town, Player p) {
        config.set("plots." + plot + ".town", town);
        config.save();

        RegionManager region = getWorldGuard().getRegionManager(plugin.world);
        DefaultDomain domain = new DefaultDomain();
        domain.addPlayer(p.getName());
        for (String subowner : plugin.getGenerator(town).subowners) {
            domain.addPlayer(subowner);
        }
        region.getRegion(plot).setOwners(domain);

        try {
            region.save();
        } catch (ProtectionDatabaseException e) {
            e.printStackTrace();
        }

        p.sendMessage(ChatColor.AQUA + "You have claimed " + ChatColor.GREEN + plot +
                ChatColor.AQUA + " for " + ChatColor.GOLD + town + ChatColor.AQUA + ".");
        p.sendMessage(ChatColor.AQUA + "You are now free to build within this area.");

        plugin.achievements.checkSpecificAchievement(p.getName(), "home", "townplot");

        load();
    }

    public void updatePlot(Generator g) {
        String plot = getTownPlot(g.name);

        //If plot is null then that town doesn't have a town plot
        if (plot == null) return;

        RegionManager region = getWorldGuard().getRegionManager(plugin.world);
        DefaultDomain domain = new DefaultDomain();
        domain.addPlayer(g.owner);
        for (String subowner : g.subowners) {
            domain.addPlayer(subowner);
        }
        region.getRegion(plot).setOwners(domain);

        try {
            region.save();
        } catch (ProtectionDatabaseException e) {
            e.printStackTrace();
        }

        log.info("Updated town plot members for: " + g.name);
    }

    String getPlotFromBlock(Block b) {
        for (String plot : plotSigns.keySet()) {
            if (plotSigns.get(plot).equals(b)) {
                return plot;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        Block b = event.getBlock();

        if (plotSigns.containsValue(b) && b.getType().equals(Material.SIGN_POST)) {
            //Valid plot sign
            Player p = event.getPlayer();
            if (!plugin.isTownOwner(p.getName())) {
                p.sendMessage(ChatColor.AQUA + "Only town owners may claim town plots.");
                event.setCancelled(true);
            } else {
                String plot = getPlotFromBlock(b);
                String town = plugin.getAccount(p.getName()).town;
                String plotTown = getTownPlot(town);

                if (plot != null && plotTown != null) {
                    //This is a valid plot sign and the town DOES have a plot.
                    if (!plotSigns.get(plotTown).equals(b)) {
                        p.sendMessage(ChatColor.AQUA + "You have already claimed a plot for " +
                                ChatColor.GOLD + town);
                        event.setCancelled(true);
                        return;
                    } else {
                        return;
                    }
                }

                //Otherwise, claim away!
                claimPlot(plot, plugin.getAccount(p.getName()).town, p);
                plugin.world.playSound(b.getLocation(), Sound.LEVEL_UP, 1, 1);
                plugin.effect(b.getLocation(), CustomEffect.SPELL);
            }
        }
    }
}

