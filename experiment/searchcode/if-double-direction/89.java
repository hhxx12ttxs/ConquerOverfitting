package com.alycarter.gravityGame.states.level.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import com.alycarter.gravityGame.states.level.Level;

public abstract class Entity implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Point2D.Double location;
	public Point2D.Double velocity;
	public double width;
	public double gravity;
	public boolean gravityEffects;
	public double direction;
	public String entityType;
	public int id;
	public static int nextID=0;
	public int team=0;
	
	public static final String SHIP = "ship";
	public static final String PLANET = "planet";
	
	public Entity(String entityType, Point2D.Double location, Point2D.Double velocity, double width, boolean hasGravity, boolean gravityEffects) {
		id=nextID;
		nextID++;
		this.location=location;
		this.velocity= velocity;
		this.gravityEffects = gravityEffects;
		direction=0;
		this.width=width;
		if(hasGravity){
			gravity = width*2;
		}else{
			gravity=0;
		}
		this.entityType=entityType;
	}
	
	public abstract void giveCommand(int command);

	public void update(Level level) {
		if(gravityEffects){
			for(int i=0;i<level.entities.size();i++){
				if(level.entities.get(i)!=this){
					Point2D.Double gravity = level.entities.get(i).getGravitationalEffect(this);
					velocity.x+=gravity.x*level.getWorldDeltaTime();
					velocity.y+=gravity.y*level.getWorldDeltaTime();
				}
			}
		}
		location.x+=velocity.x*level.getWorldDeltaTime();
		location.y+=velocity.y*level.getWorldDeltaTime();
		onUpdate(level);
	}
	
	public abstract void onUpdate(Level level);

	public Point2D.Double getGravitationalEffect(Entity e){
		double dx = location.x-e.location.x;
		double dy = location.y-e.location.y;
		double distance = Math.pow(dx,2)+Math.pow(dy,2);
		distance= Math.sqrt(distance);
		double g = gravity - distance;
		if(g<0){
			g=0;
		}
		Point2D.Double effect = angleAsVector(vectorAsAngle(new Point2D.Double(dx, dy)));
		effect.x*=g;
		effect.y*=g;
		return effect;
		
	}

	public static double vectorAsAngle(Point2D.Double d){
		return Math.toDegrees(Math.atan2(d.getX(), d.getY()));
	}

	public static Point2D.Double angleAsVector(double direction){
		double xd=Math.sin(Math.toRadians(direction));
		double yd=Math.cos(Math.toRadians(direction));
		return new Point2D.Double(xd,yd);
	}

	public void render(Graphics g, Level level,Point offset) {
		g.setColor(Color.BLACK);
		g.drawOval((int)((Level.unitResolution*(location.x-(width/2)))-offset.x ), (int)(Level.unitResolution*(location.y-(width/2)))-offset.y,
				(int)(Level.unitResolution*width), (int)(Level.unitResolution*width));
		g.drawLine((int)(Level.unitResolution*location.x)-offset.x, (int)(Level.unitResolution*location.y)-offset.y,
				(int)(Level.unitResolution*(location.x+(angleAsVector(direction).x*width/2)))-offset.x,
				(int)(Level.unitResolution*(location.y+(angleAsVector(direction).y*width/2)))-offset.y);
		onRender(g,level);
	}
	
	public static Entity simulateLocationAfterTime(Entity e,Level level, double t){
		Entity s = new Entity("",new Point2D.Double(e.location.x, e.location.y),
				new Point2D.Double(e.velocity.x, e.velocity.y),e.width,false,e.gravityEffects) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(Level level) {}
			@Override
			public void onRender(Graphics g,Level level) {}
			@Override
			public void giveCommand(int command) {}
		};
		if(s.gravityEffects){
			for(int i=0;i<level.entities.size();i++){
				Point2D.Double gravity = level.entities.get(i).getGravitationalEffect(s);
				s.velocity.x+=gravity.x*t;
				s.velocity.y+=gravity.y*t;
			}
		}
		s.location.x+=s.velocity.x*t;
		s.location.y+=s.velocity.y*t;
		return s;
		
	}
	
	public abstract void onRender(Graphics g,Level level);

}

