package sc2tv.redsq.model;

import sc2tv.redsq.Art;
import sc2tv.redsq.gfx.Screen;

public class Bullet extends Entity{
	public double xa, ya;
	public Mob owner;
	int life;
	boolean hit = false;
	
	public Bullet(Mob owner, double xa, double ya){
		this.owner = owner;
		pos.set(owner.pos.x + xa * 4, owner.pos.y + ya * 4);
		this.xa = xa * 4;
		this.ya = ya * 4;
		life = 100;
		
	}
	
	public void tick(){
		if(--life <= 0){
			remove();
			return;
		}
		if(hit && !removed){
			remove();
		}
		move(xa, ya);
	}
	
	public void draw(Screen screen){
		screen.draw(Art.instance.sprites[0][2],(int) pos.x,(int) pos.y);
	}
	
	public void collide(Entity e, double xa, double ya){
		System.out.println("Collided with " + e.toString());
		if(e instanceof Mob){
			((Mob) e).hurt(this, 10);
			hit = true;
		}
	}
	
	public boolean shouldBlock(Entity e){
		if(e instanceof Player) return false;
		return true;
	}
	
}

