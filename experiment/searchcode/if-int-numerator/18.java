package mobstats;

import java.util.ArrayList;
import java.util.Random;

import mobstats.entities.StatsEntity;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item that should be dropped for certain mobs in certain zones.
 * 
 * @author Justin Stauch
 * @since May 21, 2012
 * 
 * copyright 2012ÂŠ Justin Stauch, All Rights Reserved
 */
public class Drop {
    private ArrayList<ItemStack> drops;
    private int startZone, endZone, numerator, denominator;
    private ArrayList<EntityType> mobs;
    
    MobStats plugin;
    
    /**
     * Creates a new Drop with the given items, zones, and mobs.
     * 
     * @param drops List of ItemStacks to drop.
     * @param startZone Zone to start dropping the items in.
     * @param endZone Zone to be the last zone to drop the items in.
     * @param numerator The numerator to use in deciding if the items are dropped.
     * @param denominator The denominator used in deciding if the items are dropped.
     * @param mobs List of the mobs to drop for.
     */
    public Drop(ArrayList<ItemStack> drops, int startZone, int endZone, int numerator, int denominator, ArrayList<EntityType> mobs, MobStats plugin) {
        this.drops = new ArrayList<ItemStack>();
        this.mobs = new ArrayList<EntityType>();
        this.drops.addAll(drops);
        this.mobs.addAll(mobs);
        this.startZone = startZone;
        this.endZone = endZone;
        this.numerator = numerator;
        this.denominator = denominator;
        this.plugin = plugin;
    }
    
    /**
     * Drops the items if it is supposed to and the random generator tells it to.
     * 
     * @param event The event that was thrown to be used to find the required information.
     */
    public void drop(LivingEntity entity) {
        if (!(((CraftEntity) entity).getHandle() instanceof StatsEntity)) {
            throw new IllegalArgumentException("The entity wasn't changed to a proper entity.");
        }
        int level = ((StatsEntity) ((CraftEntity) entity).getHandle()).getLevel();
        if (level >= startZone && level <= endZone) {
            if (!mobs.isEmpty() && !mobs.contains(entity.getType())) {
                return;
            }
            Random random = new Random();
            int chosen = random.nextInt(denominator);
            if (chosen < numerator) {
                for (ItemStack x : drops) {
                    entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), x);
                }
            }
        }
    }
}
