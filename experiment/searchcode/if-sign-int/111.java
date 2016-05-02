package com.n9works.bukkit.pvp;

import com.n9works.bukkit.TheArtifact;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class PvPSigns implements Listener {
    private final Logger log = Logger.getLogger("Artifact-PvPSign");

    private final TheArtifact plugin;
    private final List<Block> signs1v1 = new ArrayList<>();
    private final List<Block> signs2v2 = new ArrayList<>();
    private final List<Block> signsVota = new ArrayList<>();
    private final List<Block> signsOverload = new ArrayList<>();

    public PvPSigns(TheArtifact plugin) {
        this.plugin = plugin;
        signs1v1.add(plugin.world.getBlockAt(91, 99, -53));
        signs1v1.add(plugin.world.getBlockAt(59, 67, -134));
        signs1v1.add(plugin.world.getBlockAt(68, 67, -134));
        signs2v2.add(plugin.world.getBlockAt(91, 99, -51));
        signs2v2.add(plugin.world.getBlockAt(74, 67, -134));
        signs2v2.add(plugin.world.getBlockAt(83, 67, -134));
        signsVota.add(plugin.world.getBlockAt(91, 99, -49));
        signsVota.add(plugin.world.getBlockAt(83, 60, -111));
        signsOverload.add(plugin.world.getBlockAt(47, 66, -116));
        update1v1Signs(0);
        update2v2Signs(0);
        updateVotASigns(0);
        updateOverloadSigns(0);
        log.info("Pvp Signs loaded");
    }

    private void updateSign(Sign sign, int num) {
        if (num > 0) {
            sign.setLine(1, "Queued: " + ChatColor.GREEN + num);
        } else {
            sign.setLine(1, "Queued: " + ChatColor.DARK_GRAY + num);
        }
        sign.setLine(2, ChatColor.RED + "Click" + ChatColor.BLUE + " to");
        sign.setLine(3, ChatColor.BLUE + "Queue");
        sign.update();
    }

    public void update1v1Signs(int num) {
        for (Block b : signs1v1) {
            if (b == null) continue;
            Sign sign = (Sign) b.getState();
            sign.setLine(0, ChatColor.BLUE + "1v1 Arena");
            updateSign(sign, num);
        }
    }

    public void update2v2Signs(int num) {
        for (Block b : signs2v2) {
            if (b == null) continue;
            Sign sign = (Sign) b.getState();
            sign.setLine(0, ChatColor.BLUE + "2v2 Arena");
            updateSign(sign, num);
        }
    }

    public void updateVotASigns(int num) {
        for (Block b : signsVota) {
            if (b == null) continue;
            Sign sign = (Sign) b.getState();
            sign.setLine(0, ChatColor.BLUE + "VotA");
            updateSign(sign, num);
        }
    }

    void updateOverloadSigns(int num) {
        for (Block b : signsOverload) {
            if (b == null) continue;
            Sign sign = (Sign) b.getState();
            sign.setLine(0, ChatColor.BLUE + "Overload");
            updateSign(sign, num);
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.isCancelled()) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = event.getClickedBlock();
                if (block.getType().equals(Material.WALL_SIGN)) {
                    if (isPvPSign(block, signs1v1)) {
                        log.info("Right click 1v1 sign");
                        plugin.queue.queuePlayer(event.getPlayer(), "1v1");
                    } else if (isPvPSign(block, signs2v2)) {
                        log.info("Right click 2v2 sign");
                        plugin.queue.queuePlayer(event.getPlayer(), "2v2");
                    } else if (isPvPSign(block, signsVota)) {
                        log.info("Right click VotA sign");
                        plugin.queue.queuePlayer(event.getPlayer(), "vota");
                    } else if (isPvPSign(block, signsOverload)) {
                        log.info("Right click Overload sign");
                        plugin.queue.queuePlayer(event.getPlayer(), "overload");
                    }
                }
            }
        }
    }

    private boolean isPvPSign(Block block, List<Block> list) {
        for (Block b : list) {
            if (b.getLocation().equals(block.getLocation())) {
                return true;
            }
        }
        return false;
    }
}

