<<<<<<< HEAD
=======

>>>>>>> 0b8f0822f1b4e9813ecd083ada35c3ed92a6f861
package com.mojang.metagun.entity;

import com.mojang.metagun.Art;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Spark extends Entity {
<<<<<<< HEAD
    private int life;

    public Spark(double x, double y, double xa, double ya) {
        this.x = x;
        this.y = y;
        this.w = 1;
        this.h = 1;
        bounce = 0.2;
        this.xa = (xa + (random.nextDouble() - random.nextDouble()) * 0.2);
        this.ya = (ya + (random.nextDouble() - random.nextDouble()) * 0.2);

        life = random.nextInt(20) + 10;
    }

    public void tick() {
        if (life-- <= 0) remove();
        onGround = false;
        tryMove(xa, ya);

        xa *= 0.999;
        ya *= 0.999;
        ya += Level.GRAVITY*0.15;
    }

    protected void hitWall(double xa, double ya) {
        this.xa *= 0.4;
        this.ya *= 0.4;
    }

    public void render(Screen g, Camera camera) {
        int xp = (int) x;
        int yp = (int) y;
        g.draw(Art.guys[9][1], xp, yp);
    }
=======
	private int life;

	public Spark (double x, double y, double xa, double ya) {
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 1;
		bounce = 0.2;
		this.xa = xa + (random.nextDouble() - random.nextDouble()) * 0.2;
		this.ya = ya + (random.nextDouble() - random.nextDouble()) * 0.2;

		life = random.nextInt(20) + 10;
	}

	@Override
	public void tick () {
		if (life-- <= 0) remove();
		onGround = false;
		tryMove(xa, ya);

		xa *= 0.999;
		ya *= 0.999;
		ya += Level.GRAVITY * 0.15;
	}

	@Override
	protected void hitWall (double xa, double ya) {
		this.xa *= 0.4;
		this.ya *= 0.4;
	}

	@Override
	public void render (Screen g, Camera camera) {
		int xp = (int)x;
		int yp = (int)y;
		g.draw(Art.guys[9][1], xp, yp);
	}
>>>>>>> 0b8f0822f1b4e9813ecd083ada35c3ed92a6f861
}

