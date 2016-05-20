package javatest.game.entities;

import javatest.game.World;
import javatest.game.types.IAtractable;
import javatest.game.types.IDestroyable;
import javatest.game.types.IRepulsable;

/**
 * Can destroy a player.
 * @author Viliam Simko
 */
public class ProximityMine extends Entity implements IRepulsable, IAtractable {

	public ProximityMine(World world, int x, int y) {
		super(world);
		setEntityAnimation("glint.png");
		setPosition(x,y);
	}
	
	@Override
	public void onUpdatePosition() {
		x += vx;
		y += vy;
		for (Entity entity : world.getEntities()) {
			if( ! (entity instanceof IDestroyable) ) continue;
			double fx = x - entity.x;
			double fy = y - entity.y;
			double r2 = fx*fx + fy*fy;
			if(r2 < 1000) {
				((IDestroyable) entity).onDestroy(this);
			}
		}
	}

}

