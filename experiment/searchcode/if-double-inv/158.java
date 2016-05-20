package com.n9works.bukkit.story;

import com.n9works.bukkit.CustomEffect;
import com.n9works.bukkit.TheArtifact;
import com.n9works.bukkit.utils.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Logger;

/**
 * Special Gladiator Horse Rewards
 *
 * @author Cryptite
 */

class Ghost implements Listener {
    private final Logger log = Logger.getLogger("Ghost");

    //Horse Rewards
    private final HashMap<UUID, BukkitTask> ghostUIDs = new HashMap<>();
    private final TheArtifact plugin;

    public Ghost(final TheArtifact plugin) {
        this.plugin = plugin;
        log.info("Loaded Ghosts");

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                this::tryGhosts, 20, 1000
        );
    }

    void tryGhosts() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getWorld().getName().equals("world")) {
                Random r = new Random();
                int chance = r.nextInt(60);
                if (chance == 7) {
                    spawnGhost(getGhostSpawnLocation(p.getLocation()), p.getName(), true);
                }
            }
        }
    }

    private static float getAngleBetweenVectors(Vector v1, Vector v2) {
        return Math.abs((float) Math.toDegrees(v1.angle(v2)));
    }

    Location getGhostSpawnLocation(Location loc) {
        //First, by vector magic, we get a position behind the player
        Vector direction = loc.getDirection().normalize();
        Random r = new Random();

        //Pick a point behind the player randomly between 20 and 40 blocks or so
        direction.multiply(-1 * (20 + r.nextInt(40)));
        Location spawnPoint = loc.add(direction);
        try {
            spawnPoint = LocationUtil.getSafeDestination(loc);
        } catch (Exception e) {
            e.printStackTrace();
            // Couldn't get a safe block cause we're probably in a wall.
            // Default to getHighestBlockAt. Won't work well for people caving, but oh well.
            spawnPoint = loc.getWorld().getHighestBlockAt(loc).getLocation();
        }
        return spawnPoint;
    }

    void spawnGhost(Location loc, String player, Boolean hasRequirements) {

        //For 1.7 event, we're gonna spawn ghosts regardless of stealth now, hence the bool.
        if (hasRequirements) {
            if (day()) {
                log.info("[Ghost] No spawn for " + player + " due to day");
                return;
            }

            if (loc.getBlock().getBiome() == Biome.OCEAN) {
                log.info("[Ghost] No spawn for " + player + " due to Ocean");
                return;
            }

            Player spawnPlayer = plugin.getServer().getPlayerExact(player);
            double ghostDistance = Math.sqrt(spawnPlayer.getLocation().distanceSquared(loc));
            if (ghostDistance <= 10) {
                log.info("[Ghost] No spawn for " + player + " due to proximity.");
                return;
            }

            //Let's make sure no other nearby players can see where the ghost will spawn.
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getWorld().equals(loc.getWorld()) && !p.getName().equals(player)) {
//				log.info("LOS: " + getEntityInSight(p, 100));
                    double distance = Math.sqrt(p.getLocation().distanceSquared(loc));
                    if (distance < 100) {
                        float angle = getAngleBetweenVectors(p.getLocation().getDirection(), loc.getDirection());
                        if (angle < 60 && angle > -130) {
                            log.info("[Ghost] No spawn for " + p.getName() + " due to LOS");
                            return;
                        }
                    }
                }
            }

            log.info("[Ghost] Spawning a ghost near " + player);
        }
        final Creeper ghost = (Creeper) loc.getWorld().spawnEntity(loc, EntityType.CREEPER);

        //Set the creeper as charged (ghost-looking shield effect)
        ghost.setPowered(true);

        //Set the creeper as invulnerable
        ghost.setNoDamageTicks(10000);

        //Make it invisible, but the shield effect remains.
        ghost.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 1));

        //Puff o' spawn particles
        plugin.effect(ghost.getLocation(), CustomEffect.TOWN_AURA);
        plugin.effect(ghost.getLocation(), CustomEffect.TOWN_AURA);
        plugin.effect(ghost.getLocation(), CustomEffect.TOWN_AURA);

        Random r = new Random();
        //Setup the task to despawn the ghost after 15-45 seconds.
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> {
                    ghostUIDs.remove(ghost.getUniqueId());
                    plugin.effect(ghost.getLocation(), CustomEffect.TOWN_AURA);
                    ghost.remove();
                }, 20 * 20 + (20 * r.nextInt(30))
        );
        ghostUIDs.put(ghost.getUniqueId(), task);
    }

    private boolean day() {
        long time = plugin.getServer().getWorld("world").getTime();

        return time < 12300 || time > 23850;
    }

    private void checkMotes(final Player p, final Inventory inv, Boolean isChest, int motes) {
        if (!isChest) {
            // If the player is picking up this mote, based on how the event works,
            // we know that they already have one mote in their inventory.
            motes += 1;
        }
        for (ItemStack item : inv) {
            if (item != null) {
                if (item.getType() == Material.GLOWSTONE_DUST
                        && item.getItemMeta().hasLore()) {
                    motes += item.getAmount();
                }
            }
        }

        if (motes >= 5) {
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> upgradeMote(p, inv), 10
            );
        }
    }

    private void upgradeMote(Player p, Inventory inv) {
        //TODO: Need to make this work for in-chests as well.
        int totalMotes = 0;
        ItemStack strangeMote = null;
        for (ItemStack item : inv) {
            if (item != null
                    && item.getType() == Material.GLOWSTONE_DUST
                    && item.getItemMeta().hasLore()) {
                strangeMote = item;
                totalMotes += item.getAmount();
                inv.removeItem(item);
            }
        }

        int masses = totalMotes / 5;
        int motesLeft = totalMotes % 5;

        if (strangeMote != null && motesLeft > 0) {
            strangeMote.setAmount(motesLeft);
            inv.addItem(strangeMote);
        }

        p.getWorld().playSound(p.getLocation(), Sound.MAGMACUBE_WALK2, 3, 1);
        p.sendMessage(ChatColor.GRAY + "The strange motes in your bags have congealed.");
        ItemStack mote = new ItemStack(Material.GOLD_NUGGET);
        mote.setAmount(masses);
        ItemMeta item = mote.getItemMeta();
        item.setDisplayName(ChatColor.YELLOW + "Congealed Mass");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_GRAY + "This object is warm with energy.");
        item.setLore(lore);
        mote.setItemMeta(item);
        inv.addItem(mote);
        log.info("[Ghost] " + p.getName() + "'s motes have congealed into a mass!");
    }

    private Boolean isMote(ItemStack item) {
        return (item.getType() == Material.GLOWSTONE_DUST && item.getItemMeta().hasLore());
    }

    private Boolean isMote(Map<Integer, ItemStack> items) {
        //TODO: Inspect what's really going on here
        for (ItemStack item : items.values()) {
            if (isMote(item)) return true;
        }
        return false;
    }

    //Prevent ghosts from seeing players; they should just wander aimlessly.
    @EventHandler
    private void noSee(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player
                && ghostUIDs.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void pickupMote(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.GLOWSTONE_DUST) {
            checkMotes(e.getPlayer(), e.getPlayer().getInventory(), false, 0);
        }
    }

    @EventHandler
    private void depositMote(InventoryClickEvent e) {
        if (e.getRawSlot() == e.getSlot()
                && e.getAction() != InventoryAction.PICKUP_ALL
                && e.getAction() != InventoryAction.PICKUP_HALF
                && e.getAction() != InventoryAction.PICKUP_ONE
                && e.getAction() != InventoryAction.PICKUP_SOME
                && !e.isShiftClick()
                && isMote(e.getCursor())) {
            //Click was in the chest and was not a pickup event.
            checkMotes((Player) e.getWhoClicked(), e.getInventory(), true, e.getCursor().getAmount());
        } else {
            if (e.isShiftClick()
                    && isMote(e.getCurrentItem())) {
                //Shift click from bottom inventory means it's going into the chest
                checkMotes((Player) e.getWhoClicked(), e.getInventory(), true, e.getCurrentItem().getAmount());
            } else {
                if (isMote(e.getCursor())) {
                    checkMotes((Player) e.getWhoClicked(), e.getWhoClicked().getInventory(), true, e.getCursor().getAmount());
                }
            }
        }
    }

    @EventHandler
    private void depositMote(InventoryDragEvent e) {
        if (isMote(e.getNewItems())) {
            //This is how to determine which inventory the drag took place in.
            for (Integer slot : e.getRawSlots()) {
                if (slot <= e.getInventory().getSize()) {
                    //Top (Chest) inventory
                    checkMotes((Player) e.getWhoClicked(), e.getInventory(), true, e.getNewItems().size());
                    break;
                }
            }

            //Otherwise, bottom (player) inventory
            checkMotes((Player) e.getWhoClicked(), e.getWhoClicked().getInventory(), true, e.getNewItems().size());
        }
    }

    @EventHandler
    private void damaged(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        if (ghostUIDs.containsKey(ent.getUniqueId())
                && e.getDamager() instanceof Player) {
            ent.remove();
            ghostUIDs.remove(ent.getUniqueId());
            plugin.effect(ent.getLocation(), CustomEffect.LARGE_SMOKE);
            ent.getWorld().playSound(ent.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);

            ItemStack mote = new ItemStack(Material.GLOWSTONE_DUST);
            ItemMeta item = mote.getItemMeta();
            item.setDisplayName(ChatColor.GRAY + "Strange Mote");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_GRAY + "This item glows faintly.");
            item.setLore(lore);
            mote.setItemMeta(item);
            ent.getWorld().dropItemNaturally(ent.getLocation(), mote);
            Player p = (Player) e.getDamager();
            log.info("[Ghost] " + p.getName() + " killed a ghost!");
        }
    }
}

