package sc2tv.redsq.model;

import java.util.Random;

import sc2tv.redsq.Art;
import sc2tv.redsq.gfx.Screen;

public class TestMob extends Mob{
	//boolean dead = false;
	public double xSlide;
	public double ySlide;
	public TestMob(double x, double y) {
		super(x, y);
		health = 5000;
	}

	public int walkTime;
	public int facing;
	Random random = new Random();
	public void tick(){
		super.tick();
		
		if(facing == 0) yd += speed;
		if(facing == 1) xd -= speed;
		if(facing == 2) yd -= speed;
		if(facing == 3) xd += speed;
		walkTime++;
		
		double speed = 0.5;
		walkTime++;
		if(walkTime / 8 % 16 != 0){
			move(xd, yd);
			facing = random.nextInt(4);
			walkTime = 0;
		}
		//if(dead) removed = true;
		xd *= 0.2;
		yd *= 0.2;
	}
	
	public void draw(Screen screen) {
		System.out.println(health);
		if(hurtTime > 0 && !dead){
			if(hurtTime > 40 - 6 && hurtTime / 2 % 2 == 0){
				screen.colourDraw(Art.instance.player[2][2], (int) pos.x, (int) pos.y, 0xA0FFFFFF); // ARGB
			} else{
				int col = 180 - health * 180 / health;
				if(hurtTime < 10) col = col * hurtTime * 10;
				screen.colourDraw(Art.instance.player[2][2], (int) pos.x, (int) pos.y, (col << 24) + 255 * 65536); // ARGB
			}
		} else  if(!dead){	
			screen.draw(Art.instance.player[2][2], (int) pos.x, (int) pos.y);
		} else{
			screen.draw(Art.instance.tiles[1][0],(int) pos.x, (int) pos.y);
		}
	}
	
	public void collide(Entity e, double xa, double ya){
		//if(e instanceof Bullet){
		//	hurt(e, 10);
		//}
	}
	
	
	
	
	
}

