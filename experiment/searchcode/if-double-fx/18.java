package javatest.game.entities;

import java.util.Random;

import javatest.game.World;
import javatest.game.types.IDestroyable;
import javatest.game.types.IPlayer;
import javatest.game.types.IRepulsable;

/**
 * Represents the player. Contains methods for handling game logic.
 * 
 * @author Viliam Simko
 */
public class PlayerEntity extends Entity implements IRepulsable, IPlayer,
		IDestroyable {

	static private final String[] flareNames = new String[] {
			"flare-white.png", "flare-yellow.png", "flare-orange.png",
			"flare-red.png", "flare-blue.png", };

	public PlayerEntity(World world, int x, int y) {
		super(world);

		currentGoal = getGoalByNumber(1);

		Random rnd = new Random();
		String imageName = flareNames[rnd.nextInt(flareNames.length)];
		setEntityAnimation(imageName);

		this.x = x;
		this.y = y;
	}

	private GoalEntity currentGoal;

	synchronized final public GoalEntity getCurrentGoal() {
		return currentGoal;
	}

	/**
	 * Helper method.
	 * 
	 * @param goalNum
	 * @return
	 */
	private GoalEntity getGoalByNumber(int goalNum) {
		for (Entity entity : world.getEntities()) {
			if (entity instanceof GoalEntity) {
				GoalEntity goal = (GoalEntity) entity;
				if (goal.getGoalNum() == goalNum)
					return goal;
			}
		}
		return null;
	}

	@Override
	synchronized public void goalReached(GoalEntity goal) {

		// reached the correct goal
		assert (currentGoal != null);
		if (currentGoal == goal) {
			currentGoal.emitPositiveGlow();

			// searching for the next goal
			currentGoal = getGoalByNumber(goal.getGoalNum() + 1);

			// TODO: no more goals = winner
			if (currentGoal == null) {
				emitWinnerAnim(x, y, getEntityRadius() * 5);
				onDestroy(this);
			}

			return;
		}

		// incorrect goal (player tried to skip the sequence)
		assert (currentGoal != null);
		if (currentGoal.getGoalNum() < goal.getGoalNum()) {

			// prevent rapid negative glowing
			if (lastNegativelyGlowed == goal)
				return;
			lastNegativelyGlowed = goal;

			goal.emitNegativeGlow();
			currentGoal = getGoalByNumber(1);
			return;
		}

		// goals with a smaller goalNum are ignored
		assert (currentGoal.getGoalNum() > goal.getGoalNum());
	}

	/**
	 * Plays some animation
	 * 
	 * @param x
	 * @param y
	 * @param entityRadius
	 */
	private void emitWinnerAnim(double x, double y, int entityRadius) {
		int numDecor = 40;
		double angle = 2 * Math.PI / numDecor;
		for (int i = 0; i < numDecor; i++) {
			double fx = Math.cos(i * angle);
			double fy = Math.sin(i * angle);
			world.addEntity(new EnergyBurst(world, x + fx * entityRadius, y
					+ fy * entityRadius, this, 1));
		}
	}

	private GoalEntity lastNegativelyGlowed;

	private boolean isDestroyed = false;

	@Override
	synchronized public boolean isDestroyed() {
		return isDestroyed;
	}

	@Override
	public void onDestroy(Entity whoDestroyed) {
		isDestroyed = true;
		world.addEntity(new NegativeGlow(world, (int) x, (int) y));
		world.deleteEntity(this);
	}

	static final private double ACCEL_PERUNIT = 0.1;
	static final private double ACCEL_MAXSPEED = 3;
	static final private double ACCEL_MAXSPEEDINV = 1 / ACCEL_MAXSPEED;

	@Override
	final public void followEntity(Entity target) {
		if (target == null || target == this)
			return;

		double fx = target.x - x;
		double fy = target.y - y;

		accelerate(fx, fy);
	}

	/**
	 * Accelerates the player in the given vector.
	 * 
	 * @param fx
	 * @param fy
	 */
	private void accelerate(double fx, double fy) {
		double fabs = Math.sqrt(fx * fx + fy * fy);

		fabs *= ACCEL_MAXSPEEDINV;
		fx = fx / fabs - vx;
		fy = fy / fabs - vy;

		fabs = Math.sqrt(fx * fx + fy * fy);
		if (fabs < 0.1 || Double.isNaN(fabs))
			return;

		fx /= fabs;
		fy /= fabs;

		vx += fx * ACCEL_PERUNIT;
		vy += fy * ACCEL_PERUNIT;

		world.addEntity(new Exhaust(world, x, y, -fx * ACCEL_MAXSPEED, -fy
				* ACCEL_MAXSPEED));
	}

	@Override
	public void evadeEntity(Entity target) {
		if (target == null || target == this)
			return;

		double fx = target.x - x;
		double fy = target.y - y;

		double evadeFX = -fx - 4 * fy;
		double evadeFY = -fy + 4 * fx;

		double targetAbs = Math.sqrt(target.vx * target.vx + target.vy
				* target.vy);
		double evadeAbs = Math.sqrt(evadeFX * evadeFX + evadeFY * evadeFY);
		double scalar = target.vx * evadeFX + target.vy * evadeFY;

		if (targetAbs > 0 && evadeAbs > 0) {
			double cos = scalar / (targetAbs * evadeAbs);
			if (cos > 0) {
				evadeFX = -fx + 4 * fy;
				evadeFY = -fy - 4 * fx;
			}
		}

		accelerate(evadeFX, evadeFY);
	}

	static final private int PROXIMITY_WARNING_RADIUS = 100;

	/**
	 * Finds the nearest entity which may collide with the player.
	 * 
	 * @return the nearest entity or null if no entities are nearby
	 */
	final public Entity getProximityWarning() {
		Entity nearestEntity = null;
		double distanceToNearest2 = PROXIMITY_WARNING_RADIUS
				* PROXIMITY_WARNING_RADIUS;

		int currentGoalNum;
		try {
			currentGoalNum = currentGoal.getGoalNum();
		} catch (NullPointerException e) {
			currentGoalNum = 0;
		}

		for (Entity entity : world.getEntities()) {
			if (entity instanceof ProximityMine || entity instanceof GoalEntity) {

				// skip safe goals
				if (entity instanceof GoalEntity
						&& ((GoalEntity) entity).getGoalNum() <= currentGoalNum)
					continue;

				double fx = entity.x - x;
				double fy = entity.y - y;
				double fabs2 = fx * fx + fy * fy;

				if (fabs2 < distanceToNearest2) {
					nearestEntity = entity;
				}
			}
		}

		return nearestEntity;
	}
}

