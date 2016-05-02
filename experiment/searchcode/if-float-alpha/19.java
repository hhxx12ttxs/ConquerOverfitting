package de.ggj14.wap.frontend;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import de.ggj14.wap.WapGame;

/**
 * shows particles to define the minion faction
 * 
 * @author grimmer
 * 
 */
public class Aura {

	/**
	 * 
	 * @author grimmer
	 * 
	 */
	private class AuraParticle {
		float x; // current x
		float y; // current y
		float sx; // start x
		float sy; // start y
		float lifeTime; // range: 0 to 1
		float alpha;
		Color color;

		public AuraParticle(float sx, float sy, float tx, float ty, Color c,
				float alpha) {
			this.x = sx;
			this.y = sy;
			this.sx = sx;
			this.sy = sy;
			this.lifeTime = 0;
			this.color = c;
			this.alpha = alpha;
		}
	}

	Random rnd;
	List<AuraParticle> particles; // holds the particles
	int currentSpawnCd; // cool down for spawning
	final int spawnCd = 40;
	final float startRadius = 20.0f;
	final float targetRadius = 30.0f;
	final float absLifeTime = 1000.0f;
	float tx; // target x
	float ty; // target y

	public Aura() {
		particles = new LinkedList<AuraParticle>();
		rnd = new Random(System.currentTimeMillis());
	}

	public void addParticle(int x, int y, Color c) {
		float alpha = rnd.nextFloat() * 6.283f;
		float startX = x + (float) (Math.cos(alpha) * startRadius);
		float startY = y + (float) (Math.sin(alpha) * startRadius);
		AuraParticle p = new AuraParticle(startX, startY, x, y, c, alpha);
		particles.add(p);
	}

	public boolean maySpawn() {
		return currentSpawnCd < 0;
	}

	public void resetSpawnCd() {
		currentSpawnCd = spawnCd;
	}

	public void update(int delta, float tx, float ty) {
		if (currentSpawnCd >= 0) {
			currentSpawnCd -= delta;
		}

		this.tx = tx;
		this.ty = ty;

		Iterator<AuraParticle> iter = particles.iterator();
		while (iter.hasNext()) {
			AuraParticle ap = iter.next();
			ap.lifeTime += ((float) delta / absLifeTime);
			ap.sx = tx + (float) (Math.cos(ap.alpha) * startRadius);
			ap.sy = ty + (float) (Math.sin(ap.alpha) * startRadius);
			float ttx = tx + (float) (Math.cos(ap.alpha) * targetRadius);
			float tty = ty + (float) (Math.sin(ap.alpha) * targetRadius);
			ap.x = ap.sx * (1.0f - ap.lifeTime) + ttx * (ap.lifeTime);
			ap.y = ap.sy * (1.0f - ap.lifeTime) + tty * (ap.lifeTime);
			if (ap.lifeTime > 1.0f) {
				iter.remove();
			}
		}
	}

	public void render(Graphics g) {
		g.setDrawMode(Graphics.MODE_SCREEN);
		for (AuraParticle ap : particles) {
			g.setColor(ap.color);
			g.fillRect(WapGame.getXOffset() + ap.x,
					WapGame.getYOffset() + ap.y, 5.0f, 5.0f);
		}
		g.setDrawMode(Graphics.MODE_NORMAL);
	}
}

