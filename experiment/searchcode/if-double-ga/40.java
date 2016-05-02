package tanks;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The class which contains ball data. (Only one instance currently)
 */
public class Ball {

	private static final String CAT_LEFT_1_URI = "image/cat-left-1.png";
	private static final String CAT_LEFT_0_URI = "image/cat-left-0.png";
	private static final String CAT_RIGHT_1_URI = "image/cat-right-1.png";
	private static final String CAT_RIGHT_0_URI = "image/cat-right-0.png";

	/**
	 * A pair of images for the wagging tail cat (right)
	 */
	private static BufferedImage catImageRight0 = null, catImageRight1 = null;

	/**
	 * A pair of images for the wagging tail cat (left)
	 */
	private static BufferedImage catImageLeft0 = null, catImageLeft1 = null;

	/**
	 * The width of all cat frames in pixels
	 */
	public static final int CAT_IMAGE_WIDTH = 36;

	/**
	 * The height of all cat frames in pixels
	 */
	public static final int CAT_IMAGE_HEIGHT = 22;

	public static final double PLAYER_0_START_BALL_X = 90,
			PLAYER_0_START_BALL_Y = 460, PLAYER_1_START_BALL_X = 700,
			PLAYER_1_START_BALL_Y = 465, STANDARD_GRAVITY = 0.02,
			BALL_RADIUS = 10;

	/**
	 * The size of the ball to DRAW, in pixels, if rainbows are enabled.
	 */
	private static final double BALL_DRAW_RADIUS_RAINBOWS = 0;

	/**
	 * The radius to draw the ball, in pixels, if rainbows disabled
	 */
	private static final double BALL_DRAW_RADIUS_NORAINBOWS = BALL_RADIUS;

	private double x, y, gravAcc, xv, yv, ballRad, ballDrawRadius;

	public Ball(int playerTurn, boolean usingRainbow) {
		initialValues(playerTurn);
		ballRad = BALL_RADIUS;
		if (usingRainbow) {
			this.ballDrawRadius = BALL_DRAW_RADIUS_RAINBOWS;
		} else {
			this.ballDrawRadius = BALL_DRAW_RADIUS_NORAINBOWS;
		}

		if (catImageRight0 == null) {
			try {
				catImageRight0 = ImageIO.read(new File(CAT_RIGHT_0_URI));
				catImageRight1 = ImageIO.read(new File(CAT_RIGHT_1_URI));
				catImageLeft0 = ImageIO.read(new File(CAT_LEFT_0_URI));
				catImageLeft1 = ImageIO.read(new File(CAT_LEFT_1_URI));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setGravAcc(double ga) {
		gravAcc = ga;
	}

	public double getGravAcc() {
		return gravAcc;
	}

	public void setXV(double xv) {
		this.xv = xv;
	}

	public void setYV(double yv) {
		this.yv = yv;
	}

	public double getXV() {
		return xv;
	}

	public Angle getAngle() {
		return new Angle(Math.atan2(yv, xv), Angle.TYPE_RADIAN);
	}

	public Angle getAngleQuadrantless() {
		return new Angle(Math.atan(yv / xv), Angle.TYPE_RADIAN);
	}

	public double getYV() {
		return yv;
	}

	public int getDirection() {
		if (xv < 0) {
			return Ball.Direction.LEFT;
		} else {
			return Ball.Direction.RIGHT;
		}
	}

	public void initialValues(int i) {
		if (i == 0) {
			x = PLAYER_0_START_BALL_X;
			y = PLAYER_0_START_BALL_Y;
		} else {
			x = PLAYER_1_START_BALL_X;
			y = PLAYER_1_START_BALL_Y;
		}

		// gravity acceleration
		gravAcc = STANDARD_GRAVITY;

		// hor speed
		xv = 0;

		// ver speed
		yv = 0;

	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getRadius() {
		return (int) ballRad;
	}

	public int getDrawRadius() {
		return (int) ballDrawRadius;
	}

	public class Direction {
		public static final int LEFT = 0;
		public static final int RIGHT = 1;
	}

	/**
	 * Gets the x co-ordinate of a point near the back of the cat
	 * 
	 * @return
	 */
	public int getBackX() {
		if (xv < 0) {
			return (int) (x + (CAT_IMAGE_WIDTH / 2) - (CAT_IMAGE_HEIGHT / 2));
		} else {
			return (int) (x - (CAT_IMAGE_WIDTH / 2) + (CAT_IMAGE_HEIGHT / 2));
		}
	}

	/**
	 * Gets the y co-ordinate of a point near the back of the cat
	 * 
	 * @return
	 */
	public int getBackY() {
		return (int) (y - (CAT_IMAGE_HEIGHT / 2));
	}

	/**
	 * Draws the requested cat given the direction and frame. TODO: Make
	 * rainbows dynamically come from the back of the cat, as before
	 * 
	 * @param g
	 * @param direction
	 * @param frame
	 */
	public void drawCatFrame(Graphics g, int frame) {
		int direction = getDirection();
		if (direction == Ball.Direction.RIGHT) {
			if (frame == 0) {
				g.drawImage(catImageRight0, (int) x - CAT_IMAGE_WIDTH / 2,
						(int) y - CAT_IMAGE_HEIGHT / 2, null);
			} else {
				g.drawImage(catImageRight1, (int) x - CAT_IMAGE_WIDTH / 2,
						(int) y - CAT_IMAGE_HEIGHT / 2, null);
			}
		} else {
			if (frame == 0) {
				g.drawImage(catImageLeft0, (int) x - CAT_IMAGE_WIDTH / 2,
						(int) y - CAT_IMAGE_HEIGHT / 2, null);
			} else {
				g.drawImage(catImageLeft1, (int) x - CAT_IMAGE_WIDTH / 2,
						(int) y - CAT_IMAGE_HEIGHT / 2, null);
			}
		}
	}

	public double getSpeed() {
		return Math.sqrt(xv * xv + yv * yv);
	}
}

