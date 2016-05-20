package javatest.game.entities;

import java.util.Random;

import javatest.game.World;
import javatest.game.types.IRepulsable;

/**
 * Repulses IRepulsable entities.
 * @author Viliam Simko
 */
public class BigRepulsor extends Entity {

	private static int STRENGTH = 10;
	private static int SHAKE = 2;
	private static int RADIUS = 80;
	private static int RADIUS2 = RADIUS * RADIUS;
	
	private Random rnd = new Random();
	
	public BigRepulsor(World world, int x, int y) {
		super(world);
		setEntityAnimation("orb.png");
		setPosition(x, y);
	}
	
	public void onUpdatePosition() {
		
		for (Entity entity : world.getEntities()) {
			// update only repulsable objects
			if(! (entity instanceof IRepulsable) ) continue;
			
			double fx = x - entity.x;
			double fy = y - entity.y;
			double r2 = fx*fx + fy*fy;
			if(r2 > RADIUS2) continue;
			
			double fabs =  -STRENGTH / r2;
			entity.vx += fx*fabs;
			entity.vy += fy*fabs;
		}
	}
	
	@Override
	public void onUpdateGraphics() {
		anim.draw(world.gfx, (int) x - rnd.nextInt(SHAKE), (int) y - rnd.nextInt(SHAKE));
	}
}

