package slime;

import java.awt.Color;
import java.awt.Graphics2D;

import net.sourceforge.binge.collision.Collidable;
import net.sourceforge.binge.collision.SpriteCollisionDetector;
import net.sourceforge.binge.core.Controller;
import net.sourceforge.binge.view.painter.PainterG2D;

public class Ball implements PainterG2D, Controller, Collidable {
	private float bigness = 0.025f;
	private float x = 0;
	private float y = 0;
	@SuppressWarnings("unused")
	private float prevY;
	@SuppressWarnings("unused")
	private float prevX;
	private float vX = 0;
	private float vY = 0;
	private float aY = -0.001f;

	private Color color = Color.YELLOW;

	public float getBigness() {
		return bigness;
	}

	public void setBigness(float bigness) {
		this.bigness = bigness;
	}

	public float getVX() {
		return vX;
	}

	public void setVX(float vx) {
		vX = vx;
	}

	public float getVY() {
		return vY;
	}

	public void setVY(float vy) {
		vY = vy;
	}

	public float getX() {
		return x;
	}

	public void setX(float newx) {
		prevX = this.x;
		this.x = newx;
	}

	public float getY() {
		return y;
	}

	public void setY(float newy) {
		prevY = this.y;
		this.y = newy;
	}

	public void paint(Graphics2D g) {
		g.setColor(color);

		int x = Slime.floatToPixelX(this.x - this.bigness / 2);
		int y = Slime.floatToPixelY(this.y - this.bigness / 2);
		int w = Slime.floatToPixelX(bigness);
		int h = Slime.floatToPixelY(bigness);

		g.fillOval(x, y, w, h);
	}

	public void update(long elapsedTime) {
		float timeFactor = elapsedTime;
		timeFactor /= 1000;

		this.vY += this.aY;

		setX(x + vX * timeFactor);
		setY(y - vY * timeFactor);
	}

	@SuppressWarnings("unused")
	private void flipVX() {
		this.vX = -this.vX;
	}

	@SuppressWarnings("unused")
	private void flipVY() {
		this.vY = -this.vY;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@SuppressWarnings("unused")
	private double distance(float x1, float y1, float x2, float y2) {
		return Math.sqrt(doubleIt(x2 - x1) + doubleIt(y2 - y1));
	}

	private float doubleIt(float toDouble) {
		return toDouble * toDouble;
	}

	@SuppressWarnings("unused")
	private void reload() {
		setX(0.3f);
		setY(0.2f);
		this.vY = 0;
		this.setColor(Color.YELLOW);
	}

	public int getCollidableHeight() {
		return Slime.floatToPixelY(bigness);
	}

	public int getCollidableWidth() {
		return Slime.floatToPixelX(bigness);
	}

	public int getNextX() {
		return Slime.floatToPixelY(x);
	}

	public int getNextY() {
		return Slime.floatToPixelY(y);
	}

	public void handleCollision(Object information) {
		if (information instanceof SpriteCollisionDetector) {
			flipVY();
		}
	}
}

