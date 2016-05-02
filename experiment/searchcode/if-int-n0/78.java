package spaceinvaders;

import codeanticode.glgraphics.GLModel;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class Invader {

	PApplet pApplet;

	float x, y, originX, originY;
	int dir = 1;
	float speed, speedIncrement;
	int yStep, delayBetweenBullets = 1000;
	int distanceAllowed = 60;
	boolean alive = true;
	int id = -1;
	int myTime = 0;
	int frameInterval = 200;
	boolean frameOne = true;

	PImage invadersFrameOne, invadersFrameTwo;

	int lastBulletTime = -10000;
	private float bulletSpeed = -45;

	// The indices that connect the 8 vertices
	// in a single cube, in the form of 12 triangles.
	int cubeIndices[] = { 0, 4, 5, 0, 5, 1, 1, 5, 6, 1, 6, 2, 2, 6, 7, 2, 7, 3,
			3, 7, 4, 3, 4, 0, 4, 7, 6, 4, 6, 5, 3, 0, 1, 3, 1, 2 };

	int[][] invader1 = { { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
			{ 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
			{ 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 },
			{ 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0 },
			{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
			{ 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },
			{ 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0 },
			{ 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1 }, };

	PGraphics graphics;

	GLModel cubes;
	GLModel xcubes;
	int cubeSize = 10;

	public Invader(PApplet pApplet, PGraphics graphics, int _x, int _y,
			int _id, float _s, float _si, int _ys, int unitSize) {
		this.graphics = graphics;
		this.pApplet = pApplet;
		x = originX = _x;
		y = originY = _y;
		id = _id;
		speed = _s;
		speedIncrement = _si;
		yStep = _ys;
		cubeSize = unitSize;
		createIndexedCubes(invader1);
	}

	public void createIndexedCubes(int[][] bitmap) {

		int count = 0;
		for (int i = 0; i < bitmap.length; i++)
			for (int j = 0; j < bitmap[i].length; j++)
				if (bitmap[i][j] != 0)
					count++;

		xcubes = new GLModel(pApplet, 8 * count, PApplet.TRIANGLES,
				GLModel.STATIC);

		xcubes.beginUpdateVertices();
		int tempCount = 0;
		for (int i = 0; i < bitmap.length; i++)
			for (int j = 0; j < bitmap[i].length; j++)
				if (bitmap[i][j] != 0) {
					int n0 = 8 * tempCount;
					tempCount++;
					float x0 = (x * cubeSize / 2) + (cubeSize * 2 + 1) * i;
					float y0 = (y * cubeSize) + (cubeSize * 2 + 1) * j;
					float z0 = 0;
					xcubes.updateVertex(n0 + 0, x0 - cubeSize, y0 - cubeSize,
							z0 - cubeSize);
					xcubes.updateVertex(n0 + 1, x0 + cubeSize, y0 - cubeSize,
							z0 - cubeSize);
					xcubes.updateVertex(n0 + 2, x0 + cubeSize, y0 + cubeSize,
							z0 - cubeSize);
					xcubes.updateVertex(n0 + 3, x0 - cubeSize, y0 + cubeSize,
							z0 - cubeSize);
					xcubes.updateVertex(n0 + 4, x0 - cubeSize, y0 - cubeSize,
							z0 + cubeSize);
					xcubes.updateVertex(n0 + 5, x0 + cubeSize, y0 - cubeSize,
							z0 + cubeSize);
					xcubes.updateVertex(n0 + 6, x0 + cubeSize, y0 + cubeSize,
							z0 + cubeSize);
					xcubes.updateVertex(n0 + 7, x0 - cubeSize, y0 + cubeSize,
							z0 + cubeSize);
				}
		xcubes.endUpdateVertices();

		xcubes.initColors();
		xcubes.setColors(255, 0, 0, 100);

		// Creating vertex indices for all the cubes in the model.
		// Since each cube is identical, the indices are the same,
		// with the exception of the shifting to take into account
		// the position of the cube inside the model.
		int indices[] = new int[36 * count];
		for (int i = 0; i < count; i++) {
			int n0 = 36 * i;
			int m0 = 8 * i;
			for (int j = 0; j < 36; j++) {
				indices[n0 + j] = m0 + cubeIndices[j];
			}
		}

		xcubes.initIndices(36 * count);
		xcubes.updateIndices(indices);
	}

	public void update() {
		if (alive) {
			move();
		}
	}

	public void move() {
		x += speed * dir;
		// if(id==20){
		// println(x+"__"+originX);
		// println((abs(originX-x)>distanceAllowed));
		// println("x="+x+"  s+d:"+speed*dir);
		// }

		// Si llegamos al punto que hay que cambiar de direcci\u00f3n...
		if (PApplet.abs(x - originX) > distanceAllowed) {
			dir = -dir;
			y += yStep;
			speed += speedIncrement;
		}
	}

	public boolean checkCollision(float xx, float yy) {
		// teniendo en cuenta que los invaders son im\u00e1genes de
		// 24x16
		if (PApplet.abs(x - xx) < 12 && PApplet.abs(y - yy) < 8) {
			kill();
			return true;
		}
		return false;
	}

	public Bullet shoot(float invaderXTranslate, float invaderZTranslate) {
		Bullet bullet = null;
		if (alive)
			if (pApplet.millis() - lastBulletTime > delayBetweenBullets) {
				bullet = new Bullet(pApplet, graphics, x * invaderXTranslate, y
						* invaderZTranslate, bulletSpeed);
				lastBulletTime = pApplet.millis();
			}
		return bullet;
	}

	// GETs y SETs
	public void setX(int _x) {
		x = _x;
	}

	public void setY(int _y) {
		y = _y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void kill() {
		alive = false;
	}

	public boolean isAlive() {
		if (alive)
			return true;
		else
			return false;
	}
}

