/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots.listener;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EndermanPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.painting.PaintingBreakEvent;

import com.crimsonrpg.api.citizen.Citizen;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.plot.PlotUtils;
import com.crimsonrpg.api.plot.Privilege;
import com.crimsonrpg.api.flag.FlagPtPVP;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Represents the CrimsonPlots entity listener.
 */
public class CPEntityListener extends EntityListener {

    @Override
    public void onPaintingBreak(PaintingBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event instanceof PaintingBreakByEntityEvent)) {
            return;
        }
        PaintingBreakByEntityEvent ev = (PaintingBreakByEntityEvent) event;
        Entity remover = ev.getRemover();
        if (!(remover instanceof Player)) {
            return;
        }

        Citizen citizen = CrimsonAPI.getCitizenManager().getCitizen((SpoutPlayer) remover);
        Plot plot = PlotUtils.getPlotFromLocation(event.getPainting().getLocation());

        if (plot != null && !PlotUtils.getRole(citizen, plot).hasPrivilege(Privilege.BUILDER_DESTROY)) {
            citizen.sendMessage("You're not allowed to break paintings in " + ChatColor.YELLOW + plot.getName() + ChatColor.RED + ".");
            event.setCancelled(true);
            return;
        }
    }

    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Plot plot = PlotUtils.getPlotFromLocation(event.getLocation());
        if (plot != null) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        //Check if the combat is EvE
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        
        //Cast
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        //Check if the combat is PvP
        if (!(e.getDamager() instanceof Player && e.getEntity() instanceof Player)) {
            return;
        }

        //Get the plot (expensive)
        Plot plot = PlotUtils.getPlotFromLocation(e.getEntity().getLocation());

        //Check if the plot is PvP
        if (plot != null && !plot.getFlag(FlagPtPVP.class).isPVP()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEndermanPickup(EndermanPickupEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Plot plot = PlotUtils.getPlotFromLocation(event.getBlock().getLocation());
        if (plot != null) {
            event.setCancelled(true);
            return;
        }
    }

    @Override
    public void onEndermanPlace(EndermanPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Plot plot = PlotUtils.getPlotFromLocation(event.getLocation());
        if (plot != null) {
            event.setCancelled(true);
            return;
        }
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        //Cancel all explosions
        List<Block> blockList = event.blockList();
        for (Block block : blockList) {
            Plot plot = PlotUtils.getPlotFromLocation(block.getLocation());

            if (plot != null) {
                event.setCancelled(true);
                return;
            }

        }
    }

}

