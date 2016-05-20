package javatest.game.entities;

import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;

import javatest.game.World;
import javatest.game.types.IAtractable;

/**
 * Joint attractors attract IAtractable entities.
 * @author Viliam Simko
 */
public class JointAttractor extends Entity {

	static private LinkedList<JointAttractor> allAttractors = new LinkedList<JointAttractor>();

	public JointAttractor(World world, int x, int y) {
		super(world);
		setEntityAnimation("bluestar.png");
		setPosition(x, y);

		synchronized (allAttractors) {
			allAttractors.add(this);
		}

		world.worldTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				synchronized (allAttractors) {
					for (JointAttractor attractor : allAttractors) {
						if (attractor != JointAttractor.this) {
							JointAttractor.this.world
									.addEntity(new EnergyBurst(
											JointAttractor.this.world,
											JointAttractor.this.x,
											JointAttractor.this.y, attractor,
											rnd.nextDouble()*2-1 ));
						}
					}

				}
			}
		}, 0, 300);
	}

	Random rnd = new Random();

	@Override
	public void onUpdatePosition() {
		for (Entity entity : world.getEntities()) {
			// update only repulsable objects
			if (!(entity instanceof IAtractable))
				continue;

			double fx = x - entity.x;
			double fy = y - entity.y;
			double r2 = fx * fx + fy * fy;

			double vr = entity.vx * entity.vx + entity.vy * entity.vy;
			if (vr > 16) {
				entity.vx *= 0.94;
				entity.vy *= 0.94;
			}

			double fabs = r2 * 0.000000005;
			entity.vx += fx * fabs;
			entity.vy += fy * fabs;

		}
	}
}

