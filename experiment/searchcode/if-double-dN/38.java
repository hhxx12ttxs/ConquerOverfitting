/*******************************************************************************
 * Copyright (c) 2001, 2010 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - Code cleanup & optimizations
 *     - Bugfix: checkBulletCollision() now uses a workaround for the Java 5 bug
 *       #6457965 with Line2D.intersectsLine via intersect(Line2D.Double line)
 *     - Integration of robocode.Rules
 *     - Replaced width and height with radius
 *     - Added constructor for the BulletRecord to support the replay feature
 *     - Fixed synchonization issues on member fields and methods
 *     - Some private methods were declared public, and have therefore been
 *       redeclared as private
 *     - Replaced getting the number of explosion frames from image manager with
 *       integer constant
 *     - Removed hitTime and resetHitTime(), which is handled thru frame instead
 *     - Added getExplosionLength() to get the exact number of explosion frames
 *       for this class and sub classes
 *     - The update() method is now removing the bullet from the battle field,
 *       when the bullet reaches the inactive state (i.e. is finished)
 *     - Bugfix: Changed the delta coordinates of a bullet explosion on a robot,
 *       so that it will be on the true bullet line for all bullet events
 *     - The coordinates of the bullet when it hits, and the coordinates for the
 *       explosion rendering on a robot has been split. So now the bullet is
 *       painted using the new getPaintX() and getPaintY() methods
 *     Luis Crespo
 *     - Added states
 *     Robert D. Maupin
 *     - Replaced old collection types like Vector and Hashtable with
 *       synchronized List and HashMap
 *     Titus Chen
 *     - Bugfix: Added Battle parameter to the constructor that takes a
 *       BulletRecord as parameter due to a NullPointerException that was raised
 *       as the battleField variable was not intialized
 *     Pavel Savara
 *     - disconnected from Bullet, now we rather send BulletStatus to proxy side
 *******************************************************************************/
package net.sf.robocode.battle.peer;


import net.sf.robocode.peer.BulletStatus;
import robocode.*;
import robocode.control.snapshot.BulletState;

import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.List;


/**
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Luis Crespo (contributor)
 * @author Robert D. Maupin (contributor)
 * @author Titus Chen (constributor)
 */
public class BulletPeer implements ProjectilePeer {

	private static final int EXPLOSION_LENGTH = 17;

	private static final int RADIUS = 3;

	protected final RobotPeer owner;

	private final BattleRules battleRules;
	private final int bulletId;

	protected RobotPeer victim;

	protected BulletState state;

	private double heading;

	protected double x;
	protected double y;

	private double lastX;
	private double lastY;

	protected double power;

	private double deltaX;
	private double deltaY;

	private final Line2D.Double boundingLine = new Line2D.Double();

	protected int frame = -1;

	private final int color;

	protected int explosionImageIndex;

	public BulletPeer(RobotPeer owner, BattleRules battleRules, int bulletId) {
		super();
		this.owner = owner;
		this.battleRules = battleRules;
		this.bulletId = bulletId;
		state = BulletState.FIRED;
		color = owner.getBulletColor(); // Store current bullet color set on robot
	}

	public void checkCollision(List<? extends ProjectilePeer> projectiles) {
		for (ProjectilePeer b : projectiles) {
			if (b != null && b != this && b.isActive() && intersect(b.getBoundingLine())) {
				state = BulletState.HIT_BULLET;
				b.setState(BulletState.HIT_BULLET);
				b.setFrame(0);
				frame = 0;
				x = lastX;
				y = lastY;
				b.setX(b.getLastX());
				b.setY(b.getLastY());

				Bullet thisBullet = createBullet();
				Bullet otherBullet = null;
				if (b instanceof BulletPeer) {
					otherBullet = ((BulletPeer) b).createBullet();

					owner.addEvent(new BulletHitBulletEvent(thisBullet, otherBullet));
					b.getOwner().addEvent(new BulletHitBulletEvent(otherBullet, thisBullet));
				} // When introducing new projectiles, add those here!
				
				break;
			}
		}
	}

	public double getLastX() {
		return lastX;
	}

	public double getLastY() {
		return lastY;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	private Bullet createBullet() {
		return new Bullet(heading, x, y, power, owner == null ? null : owner.getName(),
				victim == null ? null : victim.getName(), isActive(), bulletId);
	}

	private BulletStatus createStatus() {
		return new BulletStatus(bulletId, x, y, victim == null ? null : victim.getName(), isActive());
	}

	// Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6457965
	private boolean intersect(Line2D.Double line) {
		double x1 = line.x1, x2 = line.x2, x3 = boundingLine.x1, x4 = boundingLine.x2;
		double y1 = line.y1, y2 = line.y2, y3 = boundingLine.y1, y4 = boundingLine.y2;

		double dx13 = (x1 - x3), dx21 = (x2 - x1), dx43 = (x4 - x3);
		double dy13 = (y1 - y3), dy21 = (y2 - y1), dy43 = (y4 - y3);

		double dn = dy43 * dx21 - dx43 * dy21;

		double ua = (dx43 * dy13 - dy43 * dx13) / dn;
		double ub = (dx21 * dy13 - dy21 * dx13) / dn;

		return (ua >= 0 && ua <= 1) && (ub >= 0 && ub <= 1);
	}

	private void checkRobotCollision(List<RobotPeer> robots) {
		for (RobotPeer otherRobot : robots) {
			if (!(otherRobot == null || otherRobot == owner || otherRobot.isDead())
					&& otherRobot.getBoundingBox().intersectsLine(boundingLine)) {
				double damage = Rules.getBulletDamage(power);

				double score = damage;

				if (score > otherRobot.getEnergy()) {
					score = otherRobot.getEnergy();
				}
				otherRobot.updateEnergy(-damage);

				boolean teamFire = (owner.getTeamPeer() != null && owner.getTeamPeer() == otherRobot.getTeamPeer());

				if (!teamFire) {
					owner.getRobotStatistics().scoreBulletDamage(otherRobot.getName(), score);
				}

				if (otherRobot.getEnergy() <= 0) {
					if (otherRobot.isAlive()) {
						otherRobot.kill();
						if (!teamFire) {
							final double bonus = owner.getRobotStatistics().scoreBulletKill(otherRobot.getName());

							if (bonus > 0) {
								owner.println(
										"SYSTEM: Bonus for killing "
												+ (owner.getNameForEvent(otherRobot) + ": " + (int) (bonus + .5)));
							}
						}
					}
				}
				owner.updateEnergy(Rules.getBulletHitBonus(power));

				Bullet bullet = createBullet();

				otherRobot.addEvent(
						new HitByBulletEvent(
								robocode.util.Utils.normalRelativeAngle(heading + Math.PI - otherRobot.getBodyHeading()), bullet));

				state = BulletState.HIT_VICTIM;

				owner.addEvent(new BulletHitEvent(otherRobot.getName(), otherRobot.getEnergy(), bullet));
				frame = 0;
				victim = otherRobot;

				double newX, newY;

				if (otherRobot.getBoundingBox().contains(lastX, lastY)) {
					newX = lastX;
					newY = lastY;

					setX(newX);
					setY(newY);
				} else {
					newX = x;
					newY = y;
				}

				deltaX = newX - otherRobot.getX();
				deltaY = newY - otherRobot.getY();

				break;
			}
		}
	}

	private void checkWallCollision() {
		if ((x - RADIUS <= 0) || (y - RADIUS <= 0) || (x + RADIUS >= battleRules.getBattlefieldWidth())
				|| (y + RADIUS >= battleRules.getBattlefieldHeight())) {
			state = BulletState.HIT_WALL;
			frame = 0;
			owner.addEvent(new BulletMissedEvent(createBullet()));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getId()
	 */
	public int getId() {
		return bulletId;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getFrame()
	 */
	public int getFrame() {
		return frame;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getHeading()
	 */
	public double getHeading() {
		return heading;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getOwner()
	 */
	public RobotPeer getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getPower()
	 */
	public double getPower() {
		return power;
	}

	public double getVelocity() {
		return Rules.getBulletSpeed(power);
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getVictim()
	 */
	public RobotPeer getVictim() {
		return victim;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getX()
	 */
	public double getX() {
		return x;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getY()
	 */
	public double getY() {
		return y;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getPaintX()
	 */
	public double getPaintX() {
		return (state == BulletState.HIT_VICTIM && victim != null) ? victim.getX() + deltaX : x;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getPaintY()
	 */
	public double getPaintY() {
		return (state == BulletState.HIT_VICTIM && victim != null) ? victim.getY() + deltaY : y;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#isActive()
	 */
	public boolean isActive() {
		return state.getValue() <= BulletState.MOVING.getValue();
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getState()
	 */
	public BulletState getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getColor()
	 */
	public int getColor() {
		return color;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#setHeading(double)
	 */
	public void setHeading(double newHeading) {
		heading = newHeading;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#setPower(double)
	 */
	public void setPower(double newPower) {
		power = newPower;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#setVictim(net.sf.robocode.battle.peer.RobotPeer)
	 */
	public void setVictim(RobotPeer newVictim) {
		victim = newVictim;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#setX(double)
	 */
	public void setX(double newX) {
		x = lastX = newX;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#setY(double)
	 */
	public void setY(double newY) {
		y = lastY = newY;
	}

	public void setState(BulletState newState) {
		state = newState;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#update(java.util.List, java.util.List)
	 */
	public void update(List<RobotPeer> robots, List<? extends ProjectilePeer> bullets) {
		if (isActive()) {
			frame++;
			updateMovement();
			if (bullets != null) {
				checkCollision(bullets);
			}
			if (isActive()) {
				checkRobotCollision(robots);
			}
			if (isActive()) {
				checkWallCollision();
			}
		} else if (state == BulletState.HIT_VICTIM || state == BulletState.HIT_BULLET) {
			frame++;
		}
		updateBulletState();
		owner.addBulletStatus(createStatus());
	}

	protected void updateBulletState() {
		switch (state) {
		case FIRED:
			if (frame == 1) {
				state = BulletState.MOVING;
			}
			break;

		case HIT_BULLET:
		case HIT_VICTIM:
		case EXPLODED:
			if (frame >= getExplosionLength()) {
				state = BulletState.INACTIVE;
			}
			break;

		case HIT_WALL:
			state = BulletState.INACTIVE;
			break;
		}
	}

	private void updateMovement() {
		lastX = x;
		lastY = y;

		double v = getVelocity();

		x += v * sin(heading);
		y += v * cos(heading);

		boundingLine.setLine(lastX, lastY, x, y);
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#nextFrame()
	 */
	public void nextFrame() {
		frame++;
	}

	/* (non-Javadoc)
	 * @see net.sf.robocode.battle.peer.ProjectilePeer#getExplosionImageIndex()
	 */
	public int getExplosionImageIndex() {
		return explosionImageIndex;
	}

	protected int getExplosionLength() {
		return EXPLOSION_LENGTH;
	}

	@Override
	public String toString() {
		return getOwner().getName() + " V" + getVelocity() + " *" + (int) power + " X" + (int) x + " Y" + (int) y + " H"
				+ heading + " " + state.toString();
	}

	public Double getBoundingLine() {
		return boundingLine;
	}
}
