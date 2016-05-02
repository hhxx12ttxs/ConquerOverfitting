package com.android.game.pacman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import com.android.game.pacman.game.GameLogic;
import com.android.game.pacman.utils.GameEnum;

public class GameObject {
	
	public RectF boundingRect = new RectF();
	public RectF destRect = new RectF();
	
	protected Vect position;
	protected Bitmap bitmap;
	protected boolean isTouched = false;
	protected int dirToChange;
	protected double speed;
	protected double angle = 0;
	protected Vect direction;
	protected Vect target = new Vect(0, 0);
	protected boolean stop;
	protected int nextDir;
	protected Rect sourceRect; // the rectangle to be drawn from the animation
	protected int spriteWidth; // the width of the sprite to calculate the cut
	protected int spriteHeight;
	protected Resources res;
	protected boolean targetReached = true;
	
	private Block[][] board;
	
	private Vect newdir = new Vect(0, 0);

	public void update() {

	}

	public GameObject(double speed, Block[][] board) {
		this.speed = speed;
		this.board = board;
	}

	public void rotate(double angle) {
		this.angle = angle;
	}

	public void draw(Canvas canvas) {
		boundingRect.set((float) position.x, (float) position.y,
				(float) position.x + spriteWidth, (float) position.y
						+ spriteHeight);
		canvas.drawBitmap(bitmap, sourceRect, boundingRect, null);
	}

	synchronized public void move(double dt) {

		if (targetReached) {
			if (dirToChange == GameEnum.UP) {
				direction.x=0;
				direction.y=-1;
				angle=270;
			}
			if (dirToChange == GameEnum.DOWN) {
				direction.x=0;
				direction.y=1;
				angle=90;
			}
			if (dirToChange == GameEnum.LEFT) {
				direction.x=-1;
				direction.y=0;
				angle=180;
			}
			if (dirToChange == GameEnum.RIGHT) {
				direction.x=1;
				direction.y=0;
				angle=0;
			}
			if (canMove(position, direction)) {
				stop = false;
				targetReached = false;
				target = position.add(direction
						.mulitply(GameLogic.BOARD_TILE_SIZE));
			} else
				stop = true;
		}
		if (!targetReached) {
			if (moveTowards(target, dt))
				targetReached = true;
		}

	}

	private boolean moveTowards(Vect goal, double dt) {
		
		if (position.equals(goal)) //jezeli obiekt osiagna cel
			return true;
		
		Vect dir = Vect.normalize(goal.subtract(position)); // kierunek ruchu

		position = position.add(dir.mulitply(speed * dt)); // nowa pozycja

		if (Math.abs(Vect.dot(dir, Vect.normalize(goal.subtract(position))) + 1) < speed* dt) //czy obiekt minol cel 
			position=goal;
		return position.equals(goal);
	}

	private boolean canMove(Vect position, Vect direction) {
		if (direction.x == 0 && direction.y == 0)
			return false;
		int nx = (int) (position.x / GameLogic.BOARD_TILE_SIZE)
				+ (int) direction.x;
		int ny = (int) (position.y / GameLogic.BOARD_TILE_SIZE)
				+ (int) direction.y;
		if (nx < 0 || ny < 0 || nx >= GameLogic.BOARD_WIDTH
				|| ny >= GameLogic.BOARD_HEIGHT)
			return false;
		return board[nx][ny].kind == GameEnum.PATH;

	}
	//sprawdzanie czy jest mozliwosc zmiany kierunku na nowy
	private boolean check(int dir) {
		newdir.x = 0;
		newdir.y = 0;

		if (dir == GameEnum.UP) {
			newdir.x = 0;
			newdir.y = -1;
		}
		if (dir == GameEnum.DOWN) {
			newdir.x = 0;
			newdir.y = 1;
		}
		if (dir == GameEnum.LEFT) {
			newdir.x = -1;
			newdir.y = 0;
		}
		if (dir == GameEnum.RIGHT) {
			newdir.x = 1;
			newdir.y = 0;
		}
		return canMove(position, newdir);
	}

	public void setDirection(int dir) {
		//jezeli obiekt jest wyrownany do plytki to zmien kierunek jezeli nie to zapamietaj ten kierunke na puxniej
		if (position.x % GameLogic.BOARD_TILE_SIZE == 0
				&& position.y % GameLogic.BOARD_TILE_SIZE == 0) {
			if (check(dir))
				dirToChange = dir;
		} else
			nextDir = dir;

	}
	//sprawdza czy obiekt moze zmienic kierunke na nowy 
	public void canChangeDir() {
		if (position.x % GameLogic.BOARD_TILE_SIZE == 0
				&& position.y % GameLogic.BOARD_TILE_SIZE == 0) {
			if (check(nextDir)) {
				dirToChange = nextDir;
				nextDir = GameEnum.STOP;
			}
		}

	}

	public void setIsTouched(boolean state) {
		isTouched = state;
	}
}

