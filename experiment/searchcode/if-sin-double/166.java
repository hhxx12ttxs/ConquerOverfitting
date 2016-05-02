package com.emitrom.lienzo.demo.client.common.widgets;

import java.util.ArrayList;

import com.emitrom.lienzo.client.core.animation.AnimationCallback;
import com.emitrom.lienzo.client.core.animation.IAnimation;
import com.emitrom.lienzo.client.core.animation.IAnimationHandle;
import com.emitrom.lienzo.client.core.animation.IndefiniteAnimation;
import com.emitrom.lienzo.client.core.shape.Circle;
import com.emitrom.lienzo.client.core.shape.Group;
import com.emitrom.lienzo.client.core.shape.IPrimitive;
import com.emitrom.lienzo.client.core.shape.Layer;
import com.emitrom.lienzo.client.core.shape.PolyLine;
import com.emitrom.lienzo.client.core.shape.Polygon;
import com.emitrom.lienzo.client.core.shape.Rectangle;
import com.emitrom.lienzo.client.core.shape.Text;
import com.emitrom.lienzo.client.core.types.Point2DArray;
import com.emitrom.lienzo.demo.client.common.WidgetLayer;
import com.emitrom.lienzo.shared.core.types.ColorName;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.RootPanel;

public class SteroidsGameWidget extends WidgetLayer {

	private static final int MAX_BULLETS = 6;

	private Ship ship;
	private ArrayList<Bullet> bullets;
	private ArrayList<Rock> rocks;

	private int score;
	private Layer scoreLayer;
	private Text scoreText;

	public SteroidsGameWidget(int width, int height) {
		super(width, height);
	}

	public void init() {

		Rectangle background = new Rectangle(width, height);
		background.setFillColor(ColorName.BLACK);
		add(background);
		
		// Create a separate Layer with Text to display the score
		scoreLayer = new Layer();
		scoreText = new Text("Score: 0", "Courier", 18).setStrokeColor(ColorName.WHITE);
		scoreText.setX(25).setY(25);
		scoreLayer.add(scoreText);
		getScene().add(scoreLayer);

		ship = new Ship(this);
		bullets = new ArrayList<Bullet>();
		rocks = new ArrayList<Rock>();

		// Add 6 rocks. Not too close to the ship.
		for (int i = 0; i < 6;) {
			double x = getWidth() * Math.random();
			double y = getHeight() * Math.random();

			if (ship.distance(x, y) < 200)
				continue;

			rocks.add(new Rock(this, x, y, 40));

			i++;
		}

		// Install key up/down handlers
		RootPanel.get().addDomHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				key(event, true);
			}
		}, KeyDownEvent.getType());

		RootPanel.get().addDomHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				key(event, false);
			}
		}, KeyUpEvent.getType());

		IndefiniteAnimation handle = new IndefiniteAnimation(new AnimationCallback() {

			@Override
			public void onFrame(IAnimation animation, IAnimationHandle handle) {
				// Update the bullets (if any)
				for (int i = bullets.size() - 1; i >= 0; i--) {
					Bullet bullet = bullets.get(i);
					if (!bullet.update()) {
						bullet.stopFlying(SteroidsGameWidget.this);
						bullets.remove(i);
					}
				}

				// Update the rocks
				ROCKS: for (int i = rocks.size() - 1; i >= 0; i--) {
					Rock rock = rocks.get(i);
					rock.update();

					// Check if rock hits any bullets
					for (int j = bullets.size() - 1; j >= 0; j--) {
						Bullet bullet = bullets.get(j);

						if (rock.hits(bullet.getX(), bullet.getY(), 2)) {
							// Bullet hit rock
							bullet.stopFlying(SteroidsGameWidget.this);
							bullets.remove(j);
							rocks.remove(rock);

							addScore(rock.getScore());

							rock.explode(rocks, SteroidsGameWidget.this);
							continue ROCKS;
						}
					}

					// Check if the rock hit the ship
					if (rock.hits(ship.getX(), ship.getY(), 5)) {
						// Rock hit ship. Change color for now.
						// TODO: Ship explodes...
						ship.setShipColor(nextColor());
					}
				}

				ship.update();
			}
			
			private int colorIndex = 0;
			private String[] colors = { "white", "yellow", "green", "blue" };

			private String nextColor() {
				colorIndex = (colorIndex + 1) % colors.length;
				return colors[colorIndex];
			}
			
		});
		handle.run();

	}

	private void addScore(int sc) {
		score += sc;
		scoreText.setText("Score: " + score);
		scoreLayer.draw();
	}

	protected void key(KeyCodeEvent<?> event, boolean down) {
		int code = event.getNativeKeyCode();
		switch (code) {
		case KeyCodes.KEY_LEFT: // Rotate left
			if (down)
				ship.rotate(-1);
			break;

		case KeyCodes.KEY_RIGHT: // Rotate right
			if (down)
				ship.rotate(1);
			break;

		case KeyCodes.KEY_UP: // Thrust ship
			if (down)
				ship.thrust(true);
			else
				ship.thrust(false);
			break;

		case 32: // SPACE BAR - Fire bullet
			if (down && bullets.size() < MAX_BULLETS) {
				ship.fire(this, bullets);
			}
			break;

		case KeyCodes.KEY_CTRL: // Teleport ship
			if (down) {
				double x = getWidth() * Math.random();
				double y = getHeight() * Math.random();
				ship.setLocation(x, y);
			}
			break;

		default:
			break;
		}
	}

	public static class Ship extends Falling {

		private Polygon ship;
		private PolyLine exhaust;
		private Group shape;
		private double angle; // ship points down initially
		private boolean thrust; // whether thrust is on

		private static final double ROT_ANGLE = Math.PI / 12;
		private static final double MAX_SPEED = 100;
		private static final double THRUST = 0.5;

		public Ship(Layer layer) {

			super(layer);

			shape = new Group();

			Point2DArray a = new Point2DArray().push(0, 10).push(5, -6).push(0, -2).push(-5, -6);
			ship = new Polygon(a);
			ship.setStrokeColor(ColorName.WHITE);
			shape.add(ship);

			Point2DArray p = new Point2DArray().push(2, 0).push(4, -3).push(2, -2).push(0, -5).push(-2, -2).push(-4, -3).push(-2, -0);
			exhaust = new PolyLine(p);
			exhaust.setY(-5);
			exhaust.setStrokeColor(ColorName.YELLOW);
			exhaust.setVisible(false);
			shape.add(exhaust);

			setLocation(layer.getWidth() / 2, layer.getHeight() / 2);
			tick(shape);

			layer.add(shape);

		}

		public Group getShape() {
			return shape;
		}

		public void setShipColor(String color) {
			ship.setStrokeColor(color);
		}

		public void fire(Layer layer, ArrayList<Bullet> bullets) {
			double x = getX() - Math.sin(angle) * 10;
			double y = getY() + Math.cos(angle) * 10;

			Bullet bullet = new Bullet(layer, x, y, getDx(), getDy(), angle);
			bullets.add(bullet);
		}

		public void update() {
			tick(shape);

			exhaust.setVisible(thrust);
			if (thrust) {
				boolean small = System.currentTimeMillis() % 2 == 0;
				exhaust.setScale(small ? 0.5 : 1); // flicker the flame
			}
			shape.setRotation(angle);
		}

		// dir = {-1, 0, 1}
		public void rotate(int dir) {
			angle += (dir * ROT_ANGLE);
		}

		public void thrust(boolean val) {
			thrust = val;
			if (!thrust)
				return;

			// Thrusters are on - adjust the speed
			double dx = getDx();
			double nvx = dx - Math.sin(angle) * THRUST;
			if (nvx < MAX_SPEED)
				dx = nvx;

			double dy = getDy();
			double nvy = dy + Math.cos(angle) * THRUST;
			if (nvy < MAX_SPEED)
				dy = nvy;

			setSpeed(dx, dy);
		}
	}

	public static class Rock extends Falling {
		private int size; // 40, 20, 10
		private double spin; // how fast it spins
		private int score; // number of points for this rock
		private double maxRadius; // how big the rock is
		private Polygon shape;

		public Rock(Layer layer, double x, double y, int si) {
			super(layer);

			shape = new Polygon(createPoints(size));
			shape.setStrokeColor(ColorName.WHITE);

			setLocation(x, y);

			size = si;

			double angle = Math.random() * Math.PI * 2;

			double speedFactor = 0.5;
			double dx = Math.sin(angle) * (5 - size / 10) * speedFactor;
			double dy = Math.cos(angle) * (5 - size / 10) * speedFactor;
			setSpeed(dx, dy);

			spin = (1 + Math.random()) * 0.01;
			score = (5 - size / 10) * 100;

			layer.add(shape);
		}

		public void explode(ArrayList<Rock> rocks, Layer layer) {
			layer.remove(shape);

			if (size > 10) // if it's not the smallest rock...
			{
				// Add 2 new rocks, half the size
				for (int i = 0; i < 2; i++) {
					Rock rock = new Rock(layer, getX(), getY(), size / 2);
					rocks.add(rock);
				}
			}
		}

		public int getScore() {
			return score;
		}

		public void update() {
			int ticks = tick(shape);
			shape.setRotation(shape.getRotation() + spin * ticks);
		}

		private Point2DArray createPoints(int size) {
			Point2DArray a = new Point2DArray();
			for (double angle = 0; angle < Math.PI * 2; angle += 0.25 + Math.random() * 0.5) {
				double radius = size + (size / 2 * Math.random());
				a.push(Math.sin(angle) * radius, Math.cos(angle) * radius);

				if (radius > maxRadius)
					maxRadius = radius; // track how big the rock is
			}
			maxRadius *= 0.8; // use a slightly smaller size in hit detection

			return a;
		}

		public boolean hits(double x, double y, double size) {
			return distance(x, y) <= size + maxRadius;
		}
	}

	public static class Bullet extends Falling {
		private static final double SPEED = 2;
		private static final int DISTANCE = 300; // how far the bullet flies

		private int i = 0; // how many ticks the bullet has been flying
		private Circle shape;

		public Bullet(Layer layer, double x, double y, double dx, double dy, double angle) {
			super(layer);

			shape = new Circle(1).setFillColor(ColorName.WHITE);
			shape.setX(x).setY(y);
			layer.add(shape);

			setLocation(x, y);
			setSpeed(dx - Math.sin(angle) * SPEED, dy + Math.cos(angle) * SPEED);
		}

		public void stopFlying(Layer layer) {
			layer.remove(shape);
		}

		public boolean update() {
			i += tick(shape);

			if (i > DISTANCE / SPEED) {
				return false; // stop flying
			}
			return true;
		}
	}

	// Base class for falling objects, i.e. Ship, Bullet and Rock
	public static class Falling {
		private double x;
		private double y;
		private double dx;
		private double dy;

		private int screenWidth;
		private int screenHeight;

		private long startTime;
		protected int every = 20;

		public Falling(Layer layer) {
			startTime = System.currentTimeMillis();

			init(layer);
		}

		public void init(Layer layer) {
			screenWidth = layer.getWidth();
			screenHeight = layer.getHeight();
		}

		public void setLocation(double xx, double yy) {
			x = xx;
			y = yy;
		}

		public void setSpeed(double ddx, double ddy) {
			dx = ddx;
			dy = ddy;
		}

		public int tick(IPrimitive<?> prim) {
			long time = System.currentTimeMillis();

			if (time - startTime < every) {
				return 0;
			}

			int ticks = 0;
			while (time - startTime > every) {
				x += dx;
				y += dy;

				if (x < 0)
					x = screenWidth;
				else if (x > screenWidth)
					x = 0;

				if (y < 0)
					y = screenHeight;
				else if (y > screenHeight)
					y = 0;

				startTime += every;
				ticks++;
			}

			prim.setX(x);
			prim.setY(y);

			return ticks;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getDx() {
			return dx;
		}

		public double getDy() {
			return dy;
		}

		public double distance(double x, double y) {
			double dx = x - getX();
			double dy = y - getY();
			return Math.sqrt(dx * dx + dy * dy);
		}
	}
}
