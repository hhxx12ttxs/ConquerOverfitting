package sc2tv.redsq.model.level.tile;

import java.util.List;

import sc2tv.redsq.gfx.Screen;
import sc2tv.redsq.math.BB;
import sc2tv.redsq.math.BBOwner;
import sc2tv.redsq.model.Entity;
import sc2tv.redsq.model.level.Level;

public class Tile implements BBOwner{
	public static final int WIDTH = 16;
	public static final int HEIGHT = 16;
	
	public Level level;
	public int x, y;
	
	public void init(Level level, int x, int y){
		this.level = level;
		this.x = x;
		this.y = y;
	}
	
	public void draw(Screen screen){
		
	}
	
	public void addClipBBs(List<BB> list, Entity e){
		if(canPass(e)) return;
		list.add(new BB(this, x * Tile.WIDTH, y * Tile.HEIGHT, (x + 1) * Tile.WIDTH, (y + 1) * Tile.HEIGHT));
	}
	
	public boolean canPass(Entity e){
		return true;
	}

	public void handleCollision(Entity e, double xa, double ya) {
		System.out.println("collide");
	}
}

