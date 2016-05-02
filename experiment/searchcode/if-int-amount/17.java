package me.hretsam.gills;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author The_Lynxy
 */
public class InventoryHandler {

    public static int getCount(Player player, int materialId, int data, boolean mustWear) {
        int blocksInInv = 0;
        ItemStack[] inv = player.getInventory().getContents();
        
        if (!mustWear) {
            for (int i = 0; i < inv.length; i++) {
                if (inv[i] != null && inv[i].getTypeId() == materialId && (inv[i].getDurability() == data || data == -1)) {
                    blocksInInv += inv[i].getAmount();
                }
            }
        }

        inv = player.getInventory().getArmorContents();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null && inv[i].getTypeId() == materialId) {
                blocksInInv += inv[i].getAmount();
            }
        }

        return blocksInInv;
    }

    public static int getCount(Player player, Material material, int data, boolean mustWear) {
        return getCount(player, material.getId(), data, mustWear);
    }

    public static ItemStack getFirstItem(Player player, int materialId) {
        ItemStack[] inv = player.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null && inv[i].getTypeId() == materialId) {
                return inv[i];
            }
        }

        inv = player.getInventory().getArmorContents();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null && inv[i].getTypeId() == materialId) {
                return inv[i];
            }
        }
        return null;
    }

    public static boolean contains(Player player, int materialId, int data, int amount, boolean mustWear) {
        return (getCount(player, materialId, data, mustWear) >= amount);
    }

    public static boolean contains(Player player, Material material, int data, int amount, boolean mustWear) {
        return contains(player, material.getId(), data, amount, mustWear);
    }

    /**
     * Returns true if all items did exist in the inventory
     * @param player
     * @param materialId
     * @param amount
     * @param data (-1 is wildcard)
     * @return 
     */
    public static boolean remove(Player player, int materialId, int data, int amount) {

        ItemStack[] inv = player.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null && inv[i].getTypeId() == materialId && (inv[i].getDurability() == data || data == -1)) {
                if (inv[i].getAmount() > amount) {
                    inv[i].setAmount(inv[i].getAmount() - amount);
                    amount = 0;
                } else {
                    amount -= inv[i].getAmount();
                    inv[i].setTypeId(0);
                }
            }
        }
        player.getInventory().setContents(inv);

        inv = player.getInventory().getArmorContents();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null && inv[i].getTypeId() == materialId) {
                if (inv[i].getAmount() > amount) {
                    inv[i].setAmount(inv[i].getAmount() - amount);
                    amount = 0;
                } else {
                    amount -= inv[i].getAmount();
                    inv[i].setTypeId(0);
                }
            }
        }
        player.getInventory().setArmorContents(inv);
        
        if (amount == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see InventoryHandler::remove(Player player, int materialId, int data, int amount)
     * @param player
     * @param material
     * @param data
     * @param amount 
     */
    public static boolean remove(Player player, Material material, int data, int amount) {
        return remove(player, material.getId(), data, amount);
    }

    public static boolean add(Player player, int materialId, int data, int amount) {
        int amt = amount;
        ItemStack item;
        while (amt > 0) {
            item = new ItemStack(materialId, (amt > 64 ? 64 : amt), (short) data, (byte) data);
            amt -= 64;
            player.getInventory().addItem(item);
        }
        return true;
    }

    public static boolean add(Player player, Material material, int data, int amount) {
        return add(player, material.getId(), data, amount);
    }
}

