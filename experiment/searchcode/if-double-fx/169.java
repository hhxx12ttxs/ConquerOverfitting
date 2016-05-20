package javatest.game.entities;

import javatest.game.World;
import javatest.game.types.IPlayer;

/**
 * Represents a single goal of our game.
 * @author Viliam Simko
 */
public class GoalEntity extends Entity {

	private int goalNum;
	synchronized final public int getGoalNum() {
		return goalNum;
	}
	
	synchronized private void setGoalNum(int goalNum) {
		this.goalNum = goalNum;
	}
	
	public GoalEntity(World world, int x, int y, int goalNum) {
		super(world);
		setEntityAnimation("goal" + goalNum + ".png");
		setGoalNum(goalNum);
		setPosition(x, y);
	}
	
	@Override
	public void onAnimStarted() {
		emitPositiveGlow();
	}
	
	@Override
	public void onUpdatePosition() {
		for (Entity entity : world.getEntities()) {
			// affect players only
			if(! (entity instanceof IPlayer) ) continue;
			
			double fx = x - entity.x;
			double fy = y - entity.y;
			double r2 = fx*fx + fy*fy;

			if( r2 < 1000 ) {
				((IPlayer) entity).goalReached(this);
			}
		}
	}
	
	public void emitPositiveGlow() {
		world.addEntity(new Glow(world, (int)x, (int)y));
	}
	
	public void emitNegativeGlow() {
		world.addEntity(new NegativeGlow(world, (int)x, (int)y));
	}
}

