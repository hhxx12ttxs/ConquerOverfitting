package sc2tv.redsq.model;

import java.util.Random;

import sc2tv.redsq.Art;
import sc2tv.redsq.Keys;
import sc2tv.redsq.gfx.Screen;

public class Player extends Mob{
	public Keys keys;
	double xd, yd;
	private int facing = 0;
	public double xAim = 1;
	public double yAim = 1;
	public double dir = 0;
	int walkTime = 0;
	public int shootDelay = 0;
	public int takeDelay = 0;
	public boolean wasShooting;
	public int time = 0;
	
	
	public Player(Keys keys,int x, int y){
		super(x,y);
		this.keys = keys;
	}
	
	Random random = new Random();
	public void tick(){
		double xa = 0;
		double ya = 0;
		int posDir = 0;
		
		if(keys.up.isDown) {
			ya--;
		}
		if(keys.down.isDown) {
			ya++;
		}
		if(keys.left.isDown) {
			xa--;
		}
		if(keys.right.isDown) {
			xa++;
		}
		if(!keys.pew.isDown && xa * xa + ya * ya != 0){
			xAim *= 0.7;
			yAim *= 0.7;
			xAim += xa;
			yAim += ya;
			//xa = Math.cos(dir);
			//ya = Math.sin(dir);
			//double xab = Math.cos(dir);
			//double yab = Math.sin(dir);
			//level.addEntity(new Bullet(this, xab, yab));
			facing = (int) ((Math.atan2(-xAim, yAim) * 4 / (Math.PI * 2) + 8.5)) &7;
		}
		
		if(xa != 0 || ya != 0){
			int facing2 = (int) ((Math.atan2(-xa, ya) * 8 / (Math.PI * 2) + 8.5)) &7; 
			int diff = facing - facing2;
			if(diff >= 4) diff -= 8;
			if (diff < -4) diff += 8;
			if(diff > 2 || diff < -4){
				walkTime--;
			}else walkTime++;
			double dd = Math.sqrt(xa * xa + ya * ya);
			double speed = getSpeed() / dd;
			xa *= speed;
			ya *= speed;
			
			xd += xa;
			yd += ya;
		}
		
		move(xa, ya);
		
		if(keys.pew.isDown && carrying == null){
			wasShooting = true;
			if(takeDelay > 0) takeDelay--;
			
			if(shootDelay-- <= 0){
				double dir = (int) (Math.atan2(yAim, xAim));
				xa = Math.cos(dir);
				ya = Math.sin(dir);
				xd -= xa;
				yd -= ya;
				level.addEntity(new Bullet(this, xa, ya));
				shootDelay = 5;
				//play sound
			}
		} else {
			wasShooting = false;
			takeDelay = 15;
			shootDelay = 0;
		}
		//move(xd, yd);
		//if(random.nextInt(2) == 1) level.addEntity(new Bullet(this, xa, ya));
	}
	
	public void draw(Screen screen){
		int frame = (walkTime / 4 % 4);
		
		screen.draw(Art.instance.player[frame][facing],(int) pos.x,(int) pos.y);
	}
	
	public double getSpeed(){
		return speed;
	}
	@Override
	public boolean shouldBlock(Entity e){
		return true;
	}
	
}

