package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;

import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaDoubleChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.utils.BlockUtils;
import com.afforess.minecartmaniacore.utils.ComparableLocation;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.StringUtils;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public class ItemCollectionManager {
    
    public static boolean isItemCollectionSign(final Sign sign) {
        return sign.getLine(0).toLowerCase().contains("collect item");
    }
    
    public static boolean isItemDepositSign(final Sign sign) {
        return sign.getLine(0).toLowerCase().contains("deposit item");
    }
    
    public static boolean isTrashItemSign(final Sign sign) {
        return sign.getLine(0).toLowerCase().contains("trash item");
    }
    
    public static boolean isFurnaceFuelLine(final String line) {
        return line.toLowerCase().contains("fuel:");
    }
    
    public static boolean isFurnaceSmeltLine(final String line) {
        return line.toLowerCase().contains("smelt:");
    }
    
    /**
     * Merges lines on a sign into a single line for processing, when the direction on the lines match. Needed for support of the '!' character.
     */
    public static ArrayList<String> getItemLines(final Sign sign) {
        final HashMap<CompassDirection, String> directions = new HashMap<CompassDirection, String>(5);
        final ArrayList<String> lines = new ArrayList<String>(3);
        for (int line = 1; line < 4; line++) {
            final String text = StringUtils.removeBrackets(sign.getLine(line)).trim();
            if (!text.isEmpty() && !isFurnaceFuelLine(text) && !isFurnaceSmeltLine(text) && !isBrewingStandBottomLine(text) && !isBrewingStandTopLine(text)) {
                final CompassDirection direction = ItemUtils.getLineItemDirection(text);
                if (!directions.containsKey(direction)) {
                    directions.put(direction, text);
                } else {
                    String format = text;
                    if (direction != CompassDirection.NO_DIRECTION) {
                        format = format.substring(2);
                    }
                    directions.put(direction, directions.get(direction) + ":" + format);
                }
            }
        }
        
        MinecartManiaLogger.getInstance().debug("Merged Item Strings");
        final Iterator<Entry<CompassDirection, String>> i = directions.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<CompassDirection, String> entry = i.next();
            lines.add(entry.getValue());
            MinecartManiaLogger.getInstance().debug("Item String: " + entry.getValue());
        }
        return lines;
    }
    
    public static MinecartManiaInventory getMinecartManiaInventory(final Block block) {
        MinecartManiaInventory inventory = null;
        if (block.getState() instanceof Chest) {
            inventory = MinecartManiaWorld.getMinecartManiaChest((Chest) block.getState());
            //check for double chest
            if ((inventory != null) && (((MinecartManiaChest) inventory).getNeighborChest() != null)) {
                inventory = new MinecartManiaDoubleChest((MinecartManiaChest) inventory, ((MinecartManiaChest) inventory).getNeighborChest());
            }
        } else if (block.getState() instanceof Dispenser) {
            inventory = MinecartManiaWorld.getMinecartManiaDispenser((Dispenser) block.getState());
        } else if (block.getState() instanceof Furnace) {
            inventory = MinecartManiaWorld.getMinecartManiaFurnace((Furnace) block.getState());
        } else if (block.getState() instanceof BrewingStand) {
            inventory = MinecartManiaWorld.getMinecartManiaBrewingStand((BrewingStand) block.getState());
        }
        return inventory;
    }
    
    public static ArrayList<ItemContainer> getItemContainers(final Location location, final CompassDirection direction, final boolean collection) {
        final ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
        final HashSet<Block> blocks = BlockUtils.getAdjacentBlocks(location, 1);
        final HashSet<Block> toSkip = new HashSet<Block>();
        for (final Block block : blocks) {
            if ((getMinecartManiaInventory(block) != null) && !toSkip.contains(block)) {
                final MinecartManiaInventory inventory = getMinecartManiaInventory(block);
                if (inventory instanceof MinecartManiaDoubleChest) {
                    final MinecartManiaChest other = MinecartManiaChest.getNeighborChest(block.getWorld(), block.getX(), block.getY(), block.getZ());
                    toSkip.add(other.getLocation().getBlock());
                }
                final ArrayList<String> lines = getItemLines(((Sign) location.getBlock().getState()));
                for (final String text : lines) {
                    if (!text.isEmpty() && !isFurnaceFuelLine(text) && !isFurnaceSmeltLine(text) && !isBrewingStandBottomLine(text) && !isBrewingStandTopLine(text)) {
                        ItemContainer temp = null;
                        if (collection) {
                            MinecartManiaLogger.getInstance().debug("Found Inventory To Collect From");
                            temp = new ItemCollectionContainer(inventory, text, direction);
                        } else {
                            if (inventory instanceof MinecartManiaFurnace) {
                                MinecartManiaLogger.getInstance().debug("Found Furnace To Deposit From");
                                temp = new FurnaceDepositItemContainer((MinecartManiaFurnace) inventory, text, direction);
                            } else if (inventory instanceof MinecartManiaBrewingStand) {
                                MinecartManiaLogger.getInstance().debug("Found Brewing Stand To Deposit From");
                                temp = new BrewingStandDepositItemContainer((MinecartManiaBrewingStand) inventory, text, direction);
                            } else {
                                MinecartManiaLogger.getInstance().debug("Found Inventory To Deposit From");
                                temp = new ItemDepositContainer(inventory, text, direction);
                            }
                        }
                        if (temp != null) {
                            containers.add(temp);
                        }
                    }
                }
            }
        }
        return containers;
    }
    
    public static ArrayList<ItemContainer> getTrashItemContainers(final Location location, final CompassDirection direction) {
        final ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
        final ArrayList<String> lines = getItemLines(((Sign) location.getBlock().getState()));
        for (final String text : lines) {
            if (!text.isEmpty() && !isFurnaceFuelLine(text) && !isFurnaceSmeltLine(text) && !isBrewingStandBottomLine(text) && !isBrewingStandTopLine(text)) {
                containers.add(new TrashItemContainer(text, direction));
            }
        }
        return containers;
    }
    
    public static ArrayList<ItemContainer> getFurnaceContainers(final Location location, final CompassDirection direction) {
        final ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
        final HashSet<Block> blocks = BlockUtils.getAdjacentBlocks(location, 1);
        for (final Block block : blocks) {
            if ((getMinecartManiaInventory(block) != null) && (getMinecartManiaInventory(block) instanceof MinecartManiaFurnace)) {
                final MinecartManiaFurnace furnace = (MinecartManiaFurnace) getMinecartManiaInventory(block);
                for (int line = 0; line < 4; line++) {
                    final String text = ((Sign) location.getBlock().getState()).getLine(line);
                    if (isFurnaceFuelLine(text)) {
                        containers.add(new FurnaceFuelContainer(furnace, text, direction));
                    } else if (isFurnaceSmeltLine(text)) {
                        containers.add(new FurnaceSmeltContainer(furnace, text, direction));
                    }
                }
            }
        }
        return containers;
    }
    
    public static ArrayList<ItemContainer> getBrewingStandContainers(final Location location, final CompassDirection direction) {
        final ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
        final HashSet<Block> blocks = BlockUtils.getAdjacentBlocks(location, 1);
        for (final Block block : blocks) {
            if ((getMinecartManiaInventory(block) != null) && (getMinecartManiaInventory(block) instanceof MinecartManiaBrewingStand)) {
                final MinecartManiaBrewingStand brewingStand = (MinecartManiaBrewingStand) getMinecartManiaInventory(block);
                for (int line = 0; line < 4; line++) {
                    final String text = ((Sign) location.getBlock().getState()).getLine(line);
                    if (isBrewingStandTopLine(text)) {
                        containers.add(new BrewingStandTopContainer(brewingStand, text, direction));
                    } else if (isBrewingStandBottomLine(text)) {
                        containers.add(new BrewingStandBottomContainer(brewingStand, text, direction));
                    }
                }
            }
        }
        return containers;
    }
    
    private static boolean isBrewingStandTopLine(final String line) {
        return line.toLowerCase().contains("top:");
    }
    
    private static boolean isBrewingStandBottomLine(final String line) {
        return line.toLowerCase().contains("bottom:");
    }
    
    private static void bracketizeSign(final Sign sign) {
        for (int line = 0; line < 4; line++) {
            if (!sign.getLine(line).trim().isEmpty()) {
                sign.setLine(line, StringUtils.addBrackets(StringUtils.removeBrackets(sign.getLine(line))));
            }
        }
    }
    
    public static void createItemContainers(final MinecartManiaStorageCart minecart, final HashSet<ComparableLocation> available) {
        final ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
        for (final Location loc : available) {
            final BlockState state = loc.getBlock().getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            final Sign sign = (Sign) state;
            if (isItemCollectionSign(sign)) {
                MinecartManiaLogger.getInstance().debug("Found Collect Item Sign");
                bracketizeSign(sign);
                containers.addAll(getItemContainers(sign.getBlock().getLocation(), minecart.getDirection(), true));
            } else if (isItemDepositSign(sign)) {
                MinecartManiaLogger.getInstance().debug("Found Deposit Item Sign");
                bracketizeSign(sign);
                containers.addAll(getItemContainers(sign.getBlock().getLocation(), minecart.getDirection(), false));
            } else if (isTrashItemSign(sign)) {
                MinecartManiaLogger.getInstance().debug("Found Trash Item Sign");
                bracketizeSign(sign);
                containers.addAll(getTrashItemContainers(sign.getBlock().getLocation(), minecart.getDirection()));
            }
            containers.addAll(getFurnaceContainers(sign.getBlock().getLocation(), minecart.getDirection()));
            containers.addAll(getBrewingStandContainers(sign.getBlock().getLocation(), minecart.getDirection()));
        }
        minecart.setDataValue("ItemContainerList", containers);
    }
    
    @SuppressWarnings("unchecked")
    public static void updateContainerDirections(final MinecartManiaStorageCart minecart) {
        final ArrayList<ItemContainer> containers = (ArrayList<ItemContainer>) minecart.getDataValue("ItemContainerList");
        if (containers != null) {
            for (final ItemContainer container : containers) {
                container.addDirection(minecart.getDirectionOfMotion());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void processItemContainer(final MinecartManiaStorageCart minecart) {
        final ArrayList<ItemContainer> containers = (ArrayList<ItemContainer>) minecart.getDataValue("ItemContainerList");
        if (containers != null) {
            for (final ItemContainer container : containers) {
                container.doCollection(minecart);
            }
        }
    }
    
}

