<<<<<<< HEAD
package mobstats;

import java.util.ArrayList;
import java.util.Random;

import mobstats.entities.StatsEntity;

import net.minecraft.server.ItemStack;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 * An area of between two zones that provides equipment to certain mobs.
 * 
 * @author Justin Stauch
 * @since November 11, 2012
 */
public class MobEquipment {
    private final ItemStack[] items;
    private final int startZone, endZone, numerator, denominator;
    private final ArrayList<EntityType> mobs;
    
    /**
     * Creates a new MobEquipment with the given items, zones, and mobs.
     * 
     * @param items List of ItemStacks to use for the equipment.
     * @param startZone Zone to start dropping the items in.
     * @param endZone Zone to be the last zone to drop the items in.
     * @param numerator The numerator to use in deciding if the items are dropped.
     * @param denominator The denominator used in deciding if the items are dropped.
     * @param mobs List of the mobs to drop for.
     */
    public MobEquipment(ItemStack[] items, int startZone, int endZone, int numerator, int denominator, ArrayList<EntityType> mobs) {
        this.items = new ItemStack[5];
        for (int x = 0; x < items.length; x++) {
            this.items[x] = items[x];
        }
        this.mobs = new ArrayList<EntityType>();
        this.mobs.addAll(mobs);
        this.startZone = startZone;
        this.endZone = endZone;
        this.numerator = numerator;
        this.denominator = denominator;
    }
    
    public ItemStack[] getEquipment(LivingEntity entity) {
        if (((CraftLivingEntity) entity).getHandle() instanceof StatsEntity) {
            int level = ((StatsEntity) ((CraftLivingEntity) entity).getHandle()).getLevel();
            if (level >= startZone && level <= endZone) {
                if (mobs.contains(entity.getType())) {
                    Random random = new Random();
                    int chance = random.nextInt(denominator);
                    if (chance < numerator) {
                        return items;
                    }
                }
            }
        }
        return new ItemStack[5];
    }
    
    protected int getNumerator() {
        return numerator;
    }
    
    protected int getDenominator() {
        return denominator;
    }
    
    protected int getStartZone() {
        return startZone;
    }
    
    protected int getEndZone() {
        return endZone;
    }
}
=======
/**
 * 
 */
package p183;

import peutils.Utils;

/**
 * @author Son-Huy TRAN http://www.wolframalpha.com/
 */
public class P183_MaximumProductOfParts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int sum = 0;

		for (int N = 5; N <= 10000; N++) {
			double max = N / Math.E;
			int floorMax = (int) Math.floor(max);
			int ceilMax = (int) Math.ceil(max);
			double fFloorMax = floorMax * Math.log(N * 1.0 / floorMax);
			double fCeilMax = ceilMax * Math.log(N * 1.0 / ceilMax);

			int kmax = fCeilMax > fFloorMax ? ceilMax : floorMax;
			int denominator = kmax / Utils.largestCommonDivisor(kmax, N);

			while (denominator % 2 == 0) {
				denominator /= 2;
			}

			while (denominator % 5 == 0) {
				denominator /= 5;
			}

			if (denominator > 1) {
				sum += N;
			} else {
				sum -= N;
			}
		}

		System.out.println(sum);
	}
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
