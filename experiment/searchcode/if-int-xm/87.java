package com.alycarter.gravityGame.states.level;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.alycarter.crabClawEngine.Game;
import com.alycarter.crabClawEngine.state.State;
import com.alycarter.gravityGame.states.level.entity.Entity;
import com.alycarter.gravityGame.states.level.entity.Planet;
import com.alycarter.gravityGame.states.level.entity.Ship;

public class Level implements State{
	private Game game;
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	public Integer selectedShip= null;
	
	public static double unitResolution =128;
	public Point2D.Double cameraLocation = new Point2D.Double(4, 3);
	
	public Player player;
	public boolean host = false;
	
	public Level(Game game) {
		this.game=game;
		player=new Player(this, game);
		if(host){
			entities.add(new Planet(new Point2D.Double(3, 3), 1));
			entities.add(new Planet(new Point2D.Double(8, 3), 1));
			entities.add(new Ship(1,new Point2D.Double(2, 3), new Point2D.Double(0, 1), 0.2));
			entities.add(new Ship(2,new Point2D.Double(7, 3), new Point2D.Double(0, 1), 0.2));
		}
	}
	
	public void sendCommand(int id, int command){
		for(int i=0;i<entities.size();i++){
			if(entities.get(i).id==id){
				entities.get(i).giveCommand(command);
			}
		}
	}

	@Override
	public void update() {
		player.update();
		double xm=0;
		double ym=0;
		if(game.getControls().isPressed(KeyEvent.VK_RIGHT)){
			xm+=1;
		}
		if(game.getControls().isPressed(KeyEvent.VK_LEFT)){
			xm-=1;
		}
		if(game.getControls().isPressed(KeyEvent.VK_DOWN)){
			ym+=1;
		}
		if(game.getControls().isPressed(KeyEvent.VK_UP)){
			ym-=1;
		}
		cameraLocation.x+=Entity.angleAsVector(Entity.vectorAsAngle(new Point2D.Double(xm,ym))).x*game.getDeltaTime()*Math.abs(xm);
		cameraLocation.y+=Entity.angleAsVector(Entity.vectorAsAngle(new Point2D.Double(xm,ym))).y*game.getDeltaTime()*Math.abs(ym);
		if(game.getControls().leftMouseClicked()){
			selectedShip=null;
			double x = (game.getControls().mouseLocation.x+getOffset().x)/Level.unitResolution;
			double y = (game.getControls().mouseLocation.y+getOffset().y)/Level.unitResolution;
			for(int i=0;i< entities.size();i++){
				if(entities.get(i).entityType.equals(Entity.SHIP)&&entities.get(i).team==player.team){
					double dx = x-entities.get(i).location.x;
					double dy = y-entities.get(i).location.y;
					if(Math.abs(entities.get(i).width/2)>Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2))){
						selectedShip=entities.get(i).id;
					}
				}
			}
		}
		if(game.getControls().isPressed(KeyEvent.VK_MINUS)){
			unitResolution-=50*game.getDeltaTime();
		}
		if(game.getControls().isPressed(KeyEvent.VK_EQUALS)){
			unitResolution+=50*game.getDeltaTime();
		}
		for(int i=0;i<entities.size();i++){
			entities.get(i).update(this);
		}
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g.setColor(Color.white);
		g.fillRect(0, 0, game.getResolutionWidth(), game.getResolutionHeight());
		for(int i=0;i<entities.size();i++){
			entities.get(i).render(g2, this, getOffset());
		}
		g.setColor(Color.BLACK);
		if(host){
			g.drawString("host", 0, 50);
		}
	}
	
	public Point getOffset(){
		Point offset =new Point((int)(cameraLocation.x *unitResolution), (int) (cameraLocation.y *unitResolution));
		offset.x-=game.getResolutionWidth()/2;
		offset.y-=game.getResolutionHeight()/2;
		return offset;
	}
	
	public double getWorldDeltaTime(){
		return game.getDeltaTime()/5;
	}

}

