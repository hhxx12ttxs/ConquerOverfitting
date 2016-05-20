package fi.dy.esav.JavaGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import fi.dy.esav.GameEngine.Entity;
import fi.dy.esav.GameEngine.GameEngine;
import fi.dy.esav.GameEngine.Utils;
import fi.dy.esav.JavaGame.enums.DIRECTION;

/**
 * Class for the shot bullet
 */

public class Bullet extends Entity {
	
	private double speed = 10;
	private double damage = 11;
	private DIRECTION direction = null;
	
	Color color = Color.GRAY;

	/**
	 * Disabled parameterless constructor
	 */
	private Bullet() {super(null); }
	
	/**
	 * Default constructor
	 * @param engine reference to the GameEngine
	 */
	public Bullet(GameEngine engine) {
		super(engine);
	}
	
	/**
	 * Constructor with the position and direction set
	 * @param pos position to spawn the bullet at
	 * @param direction direction the bullet will fly
	 * @param engine reference to the GameEngine
	 */
	public Bullet(Point pos, DIRECTION direction, GameEngine engine) {
		super(pos, engine);
		this.direction = direction;
	}
	
	/**
	 * Constructor with the position and direction set up
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param direction direction the bullet will fly
	 * @param engine reference to the GameEngine
	 */
	public Bullet(double x, double y, DIRECTION direction, GameEngine engine) {
		super(x, y, engine);
		this.direction = direction;
	}
	
	
	/**
	 * Method to do the logic in
	 * overrides parent class template method
	 */
	@Override
	public void act() {
		if(this.direction == DIRECTION.LEFT) {
			this.x -= this.speed;
		} else if (this.direction == DIRECTION.RIGHT) {
			this.x += this.speed;
		} else {
			try {
				throw new Exception("Direction for bullet not specified");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>) engine.getEntities().clone();
		for(Entity ent : entities) {
			if(ent instanceof EnemyEntity) {
				if(Utils.simpleHitTest(this, ent)) {
					((EnemyEntity) ent).damage(damage);
					engine.removeEntity(this);
				}
			}
		}
		if(this.x < 0 || this.x > engine.getStage().getWidth()) {
			engine.removeEntity(this);
		}
	}
	
	/**
	 * Method to do the drawing
	 * overrides parent class template method
	 * @param g Graphics instance to draw on
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect((int)x, (int)y, 10, 5);
	}
	
	/**
	 * get the speed of the bullet
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * set the speed of the bullet
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * get the direction the bullet will fly
	 * @return the direction
	 */
	public DIRECTION getDirection() {
		return direction;
	}

	/**
	 * set new direction
	 * @param direction the direction to set
	 */
	public void setDirection(DIRECTION direction) {
		this.direction = direction;
	}
	
	/**
	 * get the entity width
	 * overrides parent class template method
	 * @return entity width
	 */
	@Override
	public int getWidth() {
		return 10;
	}

	/**
	 * get the entity height
	 * overrides parent class template method
	 * @return entity height
	 */
	public int getHeight() {
		return 5;
	}

}

