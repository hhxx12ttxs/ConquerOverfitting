package com.n9works.bukkit;

import com.google.common.collect.ImmutableList;
import com.n9works.bukkit.story.Mote;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import static com.n9works.bukkit.utils.LocationUtil.parseCoord;

public class OreExchanger extends AbstractFeature {
    private final Logger log = Logger.getLogger("TheArtifact-Exchanger");
    public Location chestLoc;
    private Location depositLoc;
    private final TheArtifact plugin;
    public Boolean locked = false;
    public String playerUsing = "";
    private BukkitTask lockTask;

    public OreExchanger(TheArtifact plugin) {
        this.plugin = plugin;
        plugin.exchanger = this;
    }

    @Override
    public void load(Map<String, Object> data) {
        chestLoc = parseCoord(plugin, (String) data.get("chest"));
        depositLoc = parseCoord(plugin, (String) data.get("depositButton"));
    }

    @Override
    public boolean onButtonPressed(Player clicker, Location loc) {
        if (loc.equals(depositLoc)) {
            Inventory inv = TheArtifact.getChestInventory(chestLoc);
            boolean somethingDone = false;
            double value = getExchangerReturnValue(inv);
            double emeraldsReceived = value;
            int cellsNeeded = (int) Math.ceil(value / 64);
            if (freeCells(inv) >= cellsNeeded) {
                removeConvertedItems(inv);
                int amount = 64;
                while (value > 0 && amount >= 1) {
                    amount = (int) Math.floor(Math.min(64, value));
                    ItemStack items = new ItemStack(Material.EMERALD, Math.min(64, amount));
                    value -= amount;
                    TheArtifact.getChestInventory(chestLoc).addItem(items);
                    somethingDone = true;
                }
            } else {
                clicker.sendMessage("Not enough room to exchange for emeralds.");
            }

            if (somethingDone) {
                clicker.sendMessage(ChatColor.GRAY + "*Thunk*");
                clicker.playSound(clicker.getLocation(), Sound.ANVIL_LAND, (float) .5, 1);
                log.info(clicker.getName() + " received " + emeraldsReceived + " emeralds at the exchanger.");
                locked = true;
                playerUsing = clicker.getName();
                plugin.achievements.checkSpecificAchievement(playerUsing, "general", "exchanged");

                //Lock the exchanger for x seconds so that the player has time to grab the emeralds and not be stolen from.
                if (lockTask != null) lockTask.cancel();
                lockTask = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin,
                        () -> {
                            locked = false;
                            playerUsing = "";
                            log.info("Exchanger now available");
                        }, 20 * 15
                );
            }
            return true;
        }
        return false;
    }

    private Inventory removeConvertedItems(Inventory inv) {

        //Return the inventory with all the items that will be converted already removed.
        for (ItemStack items : inv.getContents()) {
            if (items != null
                    && items.getAmount() > 0
                    && Economy.rates.containsKey(items.getType())
                    && (!items.getType().equals(Material.GOLD_NUGGET)
                    || (items.getType().equals(Material.GOLD_NUGGET) && Mote.isMass(items)))) {
                inv.remove(items);
            }
        }
        return inv;
    }

    private double getExchangerReturnValue(Inventory inv) {
        double value = 0.0;

        for (ItemStack items : inv.getContents()) {
            if (items != null
                    && items.getAmount() > 0
                    && Economy.rates.containsKey(items.getType())
                    && (!items.getType().equals(Material.GOLD_NUGGET)
                    || (items.getType().equals(Material.GOLD_NUGGET) && Mote.isMass(items)))) {
                value += items.getAmount() * Economy.rates.get(items.getType());
            }
        }
        return value;
    }

    private int freeCells(Inventory inv) {
        //Determine free cells in the exchanger. This should also assume the converted items will be missing (converted)
        int rv = 0;
        for (ItemStack stack : inv) {
            if (stack == null || stack.getAmount() == 0 || Economy.rates.containsKey(stack.getType())) {
                rv++;
            }
        }
        return rv;
    }

    @Override
    public Collection<Location> getInterestingButtons() {
        return ImmutableList.of(depositLoc);
    }
}

