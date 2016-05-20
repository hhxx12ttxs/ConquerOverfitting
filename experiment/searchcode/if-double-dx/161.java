package sprites;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import util.*;

public class Ship {
	// define the shape of the ship and its flame
	final double[] origXPts = { 14, -10, -6, -10 }, origYPts = { 0, -8, 0, 8 },
			origFlameXPts = { -6, -23, -6 }, origFlameYPts = { -3, 0, 3 };
	// movement variables
	double x, y, angle, dx, dy, acceleration, velocityDecay, rotationalSpeed;

	boolean turningLeft, turningRight;
	public boolean accelerating;
	boolean active;
	
	int[] xPts, yPts, flameXPts, flameYPts; // store the current locs of points
											// used to draw ship and its flame
	int shotDelay, shotDelayLeft; // used to determine rate of fire

	Polygon borderPolygon; // represents bounds of the ship, used in collision

	public Ship(double x, double y, double angle, double acceleration,
			double velocityDecay, double rotationalSpeed, int shotDelay) {

		this.x = x;
		this.y = y;
		this.angle = angle;
		this.acceleration = acceleration;
		this.velocityDecay = velocityDecay;
		this.rotationalSpeed = rotationalSpeed;
		dx = 0; // not moving
		dy = 0;
		turningLeft = false; // not turning
		turningRight = false;
		accelerating = false; // not accelerating
		active = false; // start off paused
		xPts = new int[4]; // allocate space for the arrays
		yPts = new int[4];
		flameXPts = new int[3];
		flameYPts = new int[3];
		this.shotDelay = shotDelay; // # of frames between shots
		shotDelayLeft = 0; // able to fire
		borderPolygon = new Polygon(xPts, yPts, 4); // update our polygon
	}

	/*
	 * calculates where the polygons should be, then draws them.
	 */
	public void draw(Graphics g) {
		// System.out.println("drawing");
		// rotate the points, translate them to the ship's location (by adding x
		// and y), then round them by adding .5 and casting them as integers
		if (accelerating && active) { // draw flame if accelerating
			for (int i = 0; i < 3; i++) {
				flameXPts[i] = (int) (origFlameXPts[i] * Math.cos(angle)
						- origFlameYPts[i] * Math.sin(angle) + x + .5);
				flameYPts[i] = (int) (origFlameXPts[i] * Math.sin(angle)
						+ origFlameYPts[i] * Math.cos(angle) + y + .5);
			}
			g.setColor(FunBox.randomColor_RedToYellow()); // set color of flame
			g.fillPolygon(flameXPts, flameYPts, 3);
		}

		// calculate the polygon for the ship, then draw it
		for (int i = 0; i < 4; i++) {
			xPts[i] = (int) (origXPts[i] * Math.cos(angle) - // rotate
					origYPts[i] * Math.sin(angle) + x + .5); // translate and
																// round
			yPts[i] = (int) (origXPts[i] * Math.sin(angle) + // rotate
					origYPts[i] * Math.cos(angle) + y + .5); // translate and
																// round
		}

		borderPolygon = new Polygon(xPts, yPts, 4); // update our polygon
		
		if (active) {
			g.setColor(FunBox.randomPastelColor());
		} else {
			g.setColor(Color.DARK_GRAY);
		}

		g.fillPolygon(borderPolygon);

	}

	/*
	 * Decrements shotDelayLeft, adds the acceleration value to the speed of the
	 * ship if it is accelerating, moves the ship according to its velocity,
	 * decays the ships velocity, and wraps the locatin of the ship arund to the
	 * opposite side of the screen if it goes out of bounds.
	 */
	public void move(int fieldWidth, int fieldHeight) {
		if (active) {
			if (shotDelayLeft > 0)
				shotDelayLeft--; // tick down shot delay
			// this is backwards from typical polar coordinates because positive
			// y

			// is downward, because of this, adding to the angle is rotating
			// clockwise
			if (turningLeft)
				angle -= rotationalSpeed;
			if (turningRight)
				angle += rotationalSpeed;

			if (angle > (2 * Math.PI)) // Keep angle within interval of (0, 2PI)
				angle -= 2 * Math.PI;
			else if (angle < 0)
				angle += 2 * Math.PI;

			// adds accelerating to velocity in direction that the ship is
			// pointing
			// calculates the components of acceleration and adds them to
			// velocity
			if (accelerating) {
				dx += acceleration * Math.cos(angle);
				dy += acceleration * Math.sin(angle);
			}

			x += dx; // move the ship by adding velocity (change) to position
			y += dy;

			dx *= velocityDecay;
			dy *= velocityDecay;

			// wrap the ship around to the oppisite side of the field when it
			// goes
			// out of the field's bounds.
			if (x < 0)
				x += fieldWidth;
			else if (x > fieldWidth)
				x -= fieldWidth;

			if (y < 0)
				y += fieldHeight;
			else if (y > fieldHeight)
				y -= fieldHeight;
		}

	}

	public Missle fire() {
		// set delay till next missle can be fired
		shotDelayLeft = shotDelay;
		
		// TODO: time (40) can be adjusted
		return new Missle(xPts[2], yPts[2], angle, dx, dy, 40);
	}

	public void setTurningLeft(boolean turningLeft) {
		this.turningLeft = turningLeft; // start or stop turning the ship
	}

	public void setTurningRight(boolean turningRight) {
		this.turningRight = turningRight;
	}

	public void setAccelerating(boolean accelerating) {
		this.accelerating = accelerating;
	}

	public double getX() {
		return x; // return the ship's x location
	}

	public double getY() {
		return y;
	}

	public void setActive(boolean active) {
		this.active = active; // used when the game is paused or unpaused
	}

	public boolean isActive() {
		return active;
	}

	public boolean canShoot() {
		if (shotDelayLeft > 0)
			return false;
		else
			return true;
	}

}

