package ca.viaware.rpg.entity;

import java.util.Random;

import org.newdawn.slick.opengl.Texture;

import ca.viaware.rpg.game.Globals;
import ca.viaware.rpg.map.Tile;
import ca.viaware.rpg.utilities.TexturedQuad;

public class Bullet extends AbstractMoveableEntity {
	private Texture texture;
	private boolean removed = false;
	TexturedQuad bulletQuad;
	private int minDamage, maxDamage;
	private double oldX, newX, newY, oldY, xSpeed, ySpeed, xOffset, yOffset, currentSpeed;
	private targetType target;

	public Bullet(Texture t, double oldX, double newX, double oldY, double newY, double bulletSpeed, int mind, int maxd, targetType target) {
		super(oldX, oldY, 50, 50);
		width = 32;
		height = 32;
		minDamage = mind;
		maxDamage = maxd;
		texture = t;

		xOffset = Globals.gameMap.getXOffset();

		yOffset = Globals.gameMap.getYOffset();

		bulletQuad = new TexturedQuad((int) width, (int) height, x, y, texture);

		setDestination(oldX, oldY, newX, newY, bulletSpeed / 1000, target);
	}

	public void setDestination(double startX, double startY, double destX, double destY, double speed, targetType target) {
		currentSpeed = speed;
		this.oldX = startX;
		this.newX = destX;
		this.oldY = startY;
		this.newY = destY;
		double ySpeed = 0;
		double xSpeed = 0;
		x = oldX;
		y = oldY;
		x = x - xOffset;// this is for movement of player
		y = y - yOffset;
		newX = newX + xOffset;// this is for movement of player
		newY = newY + yOffset;

		double angle = 0;
		double triangleBase = oldX - newX;
		double triangleHeight = oldY - newY;
		angle = Math.tanh(triangleHeight / triangleBase) * 100;

		bulletQuad.rotate(angle * -1 / 2);

		// Maths to make bullet go in direction thing
		xSpeed = (float) (newX - oldX);
		ySpeed = (float) (newY - oldY);

		double factor = (double) (((xSpeed * xSpeed) + (ySpeed * ySpeed)));

		factor = Math.sqrt(factor);
		factor = speed / factor;

		xSpeed = xSpeed * factor;
		ySpeed = ySpeed * factor;

		this.ySpeed = ySpeed;
		this.xSpeed = xSpeed;

		this.target = target;

	}

	public double getCurrentSpeed() {
		return currentSpeed;
	}

	public void setDamage(int minDmg, int maxDmg) {
		minDamage = minDmg;
		maxDamage = maxDmg;
	}

	public void update(int delta) {

		xOffset = Globals.gameMap.getXOffset();
		yOffset = Globals.gameMap.getYOffset();

		x = x + xSpeed * delta;
		y = y + ySpeed * delta;

		// Removes if really far away
		if (x > (Globals.dispWidth * 5)) {
			removed = true;
		}
		if (y > (Globals.dispWidth * 5)) {
			removed = true;
		}
		if (x < (Globals.dispWidth * -5)) {
			removed = true;
		}
		if (y < (Globals.dispWidth * -5)) {
			removed = true;
		}
		y = y + yOffset;// this is for movement of player
		x = x + xOffset;

		// Removes if collides with object
		if (Globals.playerEntity.intersects(this) && target == targetType.PLAYER) {
			contact(null);
		} else if (target == targetType.ENEMIES) {
			for (Enemy enemy : Globals.enemies) {
				if (enemy.intersects(this)) {
					contact(enemy);
				}
			}
		} else {
		}
			System.out.println("INTERSECT");
			for (Tile[] tile1 : Globals.gameMap.mapTiles) {
				for (Tile tile : tile1) {
					if (tile.intersects(this)) {
						
						if (tile.hasCollision()) {
							removed = true;
						}
					}
				}
			}

		

		bulletQuad.setlocation(x, y);
	}

	public void reset() {
		y = y - yOffset;// this is for movement of player
		x = x - xOffset;

	}

	private void contact(Enemy e) {
		Random r = new Random();
		int damage = r.nextInt(maxDamage - minDamage);
		damage += minDamage;

		if (target == targetType.PLAYER) {
			Globals.playerEntity.takedamage(damage);
		} else if (target == targetType.ENEMIES) {
			e.takedamage(damage);
		}

		removed = true;
	}

	public boolean getremoved() {
		return removed;
	}

	public void render() {
		bulletQuad.update();

	}

	@Override
	public void draw() {

	}

	public static enum targetType {
		ENEMIES, FRIENDLIES, PLAYER, ALLNPCS, ALL;
	}

}

