package com.mojang.mojam.entity.weapon;

import java.util.ArrayList;
import java.util.List;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.entity.Bullet;
import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.network.TurnSynchronizer;

public abstract class Weapon extends Entity {
	
	public double damage;
	public int speed;
	public double accuracy;
	public double pushBack;
	public Player player;
	public String name;
	public int cost;
	public int id;
	
	public Weapon() {
	}
	
	public void shoot() {
		double dir = Math.atan2(player.aimVector.y, player.aimVector.x) + (TurnSynchronizer.synchedRandom.nextFloat() - TurnSynchronizer.synchedRandom.nextFloat()) * accuracy;
        double xa, ya, xx, yy;
        xa = xx = Math.cos(dir);
    	ya = yy = Math.sin(dir);
        
    	if (xx >= 0) xx += pushBack;
    	else xx -= pushBack;
    	if (yy >= 0) yy += pushBack;
    	else yy -= pushBack;
    	
    	player.xd -= xx;
        player.yd -= yy;
        
        Entity bullet = new Bullet(player, xa, ya, damage);
        player.level.addEntity(bullet);
        player.muzzleTicks = 3;
        player.muzzleX = bullet.pos.x + 7 * xa - 8;
        player.muzzleY = bullet.pos.y + 5 * ya - 8 + 1;
        player.shootDelay = speed;
        MojamComponent.soundPlayer.playSound("/sound/Shot 1.wav", (float) pos.x, (float) pos.y);
	}
	
	public static List<Weapon> getAllWeapons(Player player) {
		List<Weapon> weapons = new ArrayList<Weapon>();
		weapons.add(new Rifle(player));
		weapons.add(new Sniper(player));
		return weapons;
	}
	
	public double getDamage() { return damage; }
	public int getSpeed() { return speed; }
	public double getAccuracy() { return accuracy; }
	public double getPushBack() { return pushBack; }
	public String getName() { return name; }
	public int getCost() { return cost; }
	public int getId() { return id; }
}

