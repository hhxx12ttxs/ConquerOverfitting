package sc2tv.redsq.model;

import java.util.List;

import sc2tv.redsq.gfx.Screen;
import sc2tv.redsq.math.BB;
import sc2tv.redsq.math.BBOwner;
import sc2tv.redsq.math.Vec2;
import sc2tv.redsq.model.level.Level;

public abstract class Entity implements BBOwner {
	// AABB
	//
	// public double x, y;
	public Vec2 pos = new Vec2(0, 0);
	public Vec2 radius = new Vec2(10, 10);
	public Level level;
	public boolean removed = false;
	public boolean isBlocking = true;
	
	public double xd, yd;
	
	/*
	 * public Entity(int x, int y){ this.x = x; this.y = y; }
	 */

	public void setPos(double x, double y) {
		pos.set(x, y);
	}

	public void init(Level level) {
		this.level = level;
	}

	public void tick() {

	}

	public void draw(Screen screen) {
	}

	protected boolean move(double xa, double ya) {
		List<BB> bbs = level.getClipsBBs(this);
		boolean move = false;
		move |= _move(bbs, xa, 0);
		move |= _move(bbs, 0, ya);

		return move;
	}

	private boolean _move(List<BB> bbs, double xa, double ya) {
		double oxa = xa;
		double oya = ya;
		BB from = getBB();

		BB closest = null;
		for (int i = 0; i < bbs.size(); i++) {
			BB to = bbs.get(i);
			if (from.intersects(to))
				continue;

			if (ya == 0) {
				if (to.y0 >= from.y1 || to.y1 <= from.y0)
					continue;
				if (xa > 0) {
					double rdx = to.x0 - from.x1; // right direction
					if (rdx >= 0 && xa > rdx) {
						closest = to;
						xa = rdx - 0.1;
						if (xa < 0)
							xa = 0;
					}
				} else if (xa < 0) {
					double ldx = to.x1 - from.x0; // left direction
					if (ldx <= 0 && xa < ldx) {
						closest = to;
						xa = ldx + 0.1;
						if (xa > 0)
							xa = 0;
					}
				}
			}

			if (xa == 0) {
				if (to.x0 >= from.x1 || to.x1 <= from.x0)
					continue;
				if (ya > 0) {
					double rdy = to.y0 - from.y1;
					if (rdy >= 0 && ya > rdy) {
						closest = to;
						ya = rdy - 0.1;
						if (ya < 0)
							ya = 0;
					}
				} else if (ya < 0) {
					double ldy = to.y1 - from.y0; // left direction
					if (ldy <= 0 && ya < ldy) {
						closest = to;
						ya = ldy + 0.1;
						if (ya > 0)
							ya = 0;
					}
				}
			}
		}
		
		if(closest != null && closest.owner != null){
			closest.owner.handleCollision(this, oxa, oya);
		}
		if (xa != 0 || ya != 0) {
			pos.x += xa;
			pos.y += ya;
			return true;
		}

		return false;
	}

	public boolean intersects(double xx0, double yy0, double xx1, double yy1) {
		return getBB().intersects(xx0, yy0, xx1, yy1);
	}

	public BB getBB() {
		return new BB(this, pos.x - radius.x, pos.y - radius.y, pos.x
				+ radius.x, pos.y + radius.y);
	}

	public void handleCollision(Entity e, double xa, double ya) {
		if(this.blocks(e)){
			this.collide(e, xa, ya);
			e.collide(this, -xa, -ya);
		}
	}
	
	public boolean shouldBlock(Entity e){
		return true;
	}
	
	public boolean blocks(Entity e){
		return isBlocking && e.isBlocking && shouldBlock(e) && e.shouldBlock(e);
	}
	
	public void collide(Entity e, double xa, double ya){
		
	}
	
	public void remove(){
		removed = true;
	}

}

