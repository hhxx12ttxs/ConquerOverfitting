<<<<<<< HEAD
/*
	有理数的封装
	成员变量： 分子 分母	
	类方法： 四则运算
*/
import java.lang.Math;
public class Rational
{
	//成员变量
	int numerator;		//分子
	int denominator;	//分母

	//构造方法
	public Rational(int numerator_arg, int denominator_arg)
	{
		if (numerator_arg == 0)
		{
			this.numerator = 0;
			this.denominator = 1;
		}
		else
		{
			setNumeratorAndDenominator(numerator_arg, denominator_arg);
		}
	}

	//设置分子分母
	public void setNumeratorAndDenominator(int numerator_arg, int denominator_arg)
	{
		int c = gcd(Math.abs(numerator_arg), Math.abs(denominator_arg));
//		System.out.println("最大公约数: "+ c);
		this.numerator = numerator_arg / c;
		this.denominator = denominator_arg / c;
		if (this.numerator < 0 && this.denominator < 0)
		{
			this.numerator = -this.numerator;
			this.denominator = -this.denominator;
		}
	}

	//求两整数的最大公约数（递归方法）
	public int gcd(int a, int b)
	{
		if (a % b == 0)
		{
			return b;
		}
		else
		{
			return gcd(b, a%b);
		}
	}
	
	//获得分子
	public int getNumerator()
	{
		return this.numerator;
	}

	//获得分母
	public int getDenominator()
	{
		return this.denominator;

	}

	//加法元素
	public Rational add(Rational r)
	{
		int a = r.getNumerator();
		int b = r.getDenominator();
//		System.out.println("a = "+ a +", b = "+ b);
		int newNumerator = this.numerator * b + this.denominator * a;
		int newDenominator = this.denominator * b;
		Rational result = new Rational(newNumerator, newDenominator);

		return result;		
	}

	//减法运算
	public Rational sub(Rational r)
	{
		int a = r.getNumerator();
		int b = r.getDenominator();
		int newNumerator = this.numerator * b - this.denominator * a;
		int newDenominator = this.denominator * b;
		Rational result = new Rational(newNumerator, newDenominator);
		
		return result;
	}
	
	//乘法运算
	public Rational muti(Rational r)
	{
		int a = r.getNumerator();
		int b = r.getDenominator();
		int newNumerator = this.numerator * a;
		int newDenominator = this.denominator * b;
		Rational result = new Rational(newNumerator, newDenominator);

		return result;
		
	}

	//除发运算
	public Rational div(Rational r)
	{
		int a = r.getNumerator();
		int b = r.getDenominator();
		int newNumerator = this.numerator * b;
		int newDenominator = this.denominator * a;
		Rational result = new Rational(newNumerator, newDenominator);		

		return result;
	}
}

=======
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
