package info.gomeow.listener;

import info.gomeow.Depositchest;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClickListener implements Listener {

    public Depositchest plugin;

    public InventoryClickListener(Depositchest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player) {
            if(event.getSlotType() == SlotType.CONTAINER) {
                if(event.getInventory().getType() == InventoryType.CHEST) {
                    Player player = (Player) event.getWhoClicked();
                    if(event.getInventory().getItem(event.getSlot()) != null) {
                        InventoryHolder holder = event.getInventory().getHolder();
                        if(holder instanceof Chest) {
                            Chest eventChest = (Chest) holder;
                            Location chestLoc = eventChest.getLocation();
                            String world = chestLoc.getWorld().getName();
                            Integer x = chestLoc.getBlockX();
                            Integer y = chestLoc.getBlockY();
                            Integer z = chestLoc.getBlockZ();
                            for(String nameKey:plugin.chestConfig.getChests().getKeys(false)) {
                                for(String key:plugin.chestConfig.getChests().getConfigurationSection(nameKey).getKeys(false)) {
                                    String world1 = plugin.chestConfig.getChests().getString(nameKey + "." + key + ".World");
                                    Integer x1 = plugin.chestConfig.getChests().getInt(nameKey + "." + key + ".X");
                                    Integer y1 = plugin.chestConfig.getChests().getInt(nameKey + "." + key + ".Y");
                                    Integer z1 = plugin.chestConfig.getChests().getInt(nameKey + "." + key + ".Z");
                                    String loc1 = world + ", " + x.toString() + ", " + y.toString() + ", " + z.toString();
                                    String loc2 = world1 + ", " + x1.toString() + ", " + y1.toString() + ", " + z1.toString();
                                    if(loc1.equalsIgnoreCase(loc2)) {
                                        if(!player.getName().equals(nameKey)) {
                                            if(player.hasPermission("depositchest.bypass")) {
                                                player.sendMessage("You took from " + nameKey + "\'s chest!");
                                            } else {
                                                player.sendMessage("You do not have permission to take from that chest!");
                                                event.setCancelled(true);
                                            }
                                        }
                                    }
                                }
                            }

                        } else if(holder instanceof DoubleChest) {
                            DoubleChest eventChest = (DoubleChest) holder;
                            Location chestLoc = eventChest.getLocation();
                            String world = chestLoc.getWorld().getName();
                            Integer x = chestLoc.getBlockX();
                            Integer y = chestLoc.getBlockY();
                            Integer z = chestLoc.getBlockZ();
                            for(String nameKey:plugin.chestConfig.getChests().getKeys(false)) {
                                for(String key:plugin.chestConfig.getChests().getConfigurationSection(nameKey).getKeys(false)) {
                                    String world1 = plugin.chestConfig.getChests().getString(nameKey + "." + key + ".World");
                                    Integer x1 = plugin.chestConfig.getChests().getInt(nameKey + "." + key + ".X");
                                    Integer y1 = plugin.chestConfig.getChests().getInt(nameKey + "." + key + ".Y");
                                    Integer z1 = plugin.chestConfig.getChests().getInt(nameKey + "." + key + ".Z");
                                    String loc3 = world + ", " + x.toString() + ", " + y.toString() + ", " + z.toString();
                                    String loc4 = world1 + ", " + x1.toString() + ", " + y1.toString() + ", " + z1.toString();
                                    if(loc3.equalsIgnoreCase(loc4)) {
                                        if(!player.getName().equals(nameKey)) {
                                            if(player.hasPermission("depositchest.bypass")) {
                                                player.sendMessage("You took from " + nameKey + "\'s chest!");
                                            } else {
                                                player.sendMessage("You do not have permission to take from that chest!");
                                                event.setCancelled(true);
                                            }
                                        }
                                        return;
                                    } else {
                                        for(BlockFace face:plugin.faces) {
                                            Location chestLoc2 = eventChest.getLocation().getBlock().getRelative(face).getLocation();
                                            String world2 = chestLoc2.getWorld().getName();
                                            Integer x2 = chestLoc2.getBlockX();
                                            Integer y2 = chestLoc2.getBlockY();
                                            Integer z2 = chestLoc2.getBlockZ();
                                            String loc5 = world1 + ", " + x1.toString() + ", " + y1.toString() + ", " + z1.toString();
                                            String loc6 = world2 + ", " + x2.toString() + ", " + y2.toString() + ", " + z2.toString();
                                            if(loc5.equalsIgnoreCase(loc6)) {
                                                if(!player.getName().equals(nameKey)) {
                                                    if(player.hasPermission("depositchest.bypass")) {
                                                        player.sendMessage("You took from " + nameKey + "\'s chest!");
                                                    } else {
                                                        player.sendMessage("You do not have permission to take from that chest!");
                                                        event.setCancelled(true);
                                                    }
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

