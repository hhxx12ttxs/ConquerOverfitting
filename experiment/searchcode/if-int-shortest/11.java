package game;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;


import processing.core.PApplet;
import processing.core.PShape;

import util.Updateable;

/**
 * Kuvaa pelimaailmaa
 * @author Juho Salmio
 *
 */
public class WorldModel implements Updateable {

	public PlayerModel player;
	private PApplet parent;
	private PShape path;
	private final static int GAME_OVER_DISTANCE = 15;
	private final static int WARNING_DISTANCE_FROM_PATH = 30;
	private GameState gameState;
	private boolean movementPossible;
	private Level currentLevel;
	private GameTimer timer;
	private Set<RouteSelect>routesAccomplished;

	public WorldModel(PApplet parent){
		this.parent = parent;
		this.path = parent.loadShape("Otaniemi1.svg");
		this.gameState = GameState.INMENU;
		routesAccomplished = new HashSet<RouteSelect>();

	}

	public void setPlayer(PlayerModel player){
		this.player = player;
	}

	public void setGameState(GameState state){
		this.gameState = state;
	}
	
	public void setGameTimer(GameTimer timer){
		this.timer = timer;
	}
	
	public GameState getGameState(){
		return this.gameState;
	}
	
	
	public void setCurrentLevel(RouteSelect route){
		this.currentLevel = new Level(route);
		Point startPoint = currentLevel.getStartPoint();
		this.player.setLocation(startPoint.x, startPoint.y);
		//k&#x160;ynnistet&#x160;&#x160;n timer
		if(timer != null){
			timer.setLimitTime(currentLevel.getTimeLimit());
			timer.startTimer();
		}
	}
	
	public Level getCurrentLevel(){
		return currentLevel;
	}
	/**
	 * Laskee lyhyimm&#x160;n et&#x160;isyyden pelialuetta rajaaviin vektoreihin
	 * @param p
	 * @return
	 */
	
	public boolean allLevelsAccomplished(){
		return routesAccomplished.size() == RouteSelect.values().length;
	}
	private int getShortestDistanceToPath(Point p){
		int shortest = 100000;
		for(int j = 0; j < this.path.getChildCount(); j++){
			PShape shape = this.path.getChild(j);
			for(int i = 0; i< shape.getVertexCount()-1; i++){
				Point2D.Float startPoint = new Point2D.Float(shape.getVertexX(i), shape.getVertexY(i));
				Point2D.Float endPoint = new Point2D.Float(shape.getVertexX(i+1), shape.getVertexY(i+1));
				Line2D line = new Line2D.Float(startPoint, endPoint);
				//parent.line((float)line.getP1().getX(), (float)line.getP1().getY(), (float)line.getP2().getX(),(float) line.getP2().getY());
				double distance = line.ptSegDistSq(p.x, p.y);
				if(distance < shortest){
					shortest = (int) distance;
				}
				
				
			}
		}
		return (int)Math.sqrt(shortest);
	}


	public Point getPlayerLocation(){
		return new Point(player.getLocationX(), player.getLocationY());
	}
	
	public void setMovementPossible(boolean possible){
		this.movementPossible = possible;
	}
	
	
	
	public void update(){
		
		
		if(timer != null){
			if(!timer.timeLeft()){
				setGameState(GameState.GAMEOVER);
			}
		}
		
		Point goal = currentLevel.getEndPoint();
		
		int distanceToGoalSquared = (int)(Math.pow(goal.x - player.getLocationX(), 2) + 
									Math.pow(goal.y - player.getLocationY(), 2));
		
		//maalintunnistus
		if(distanceToGoalSquared < 800 ){
			gameState = GameState.VICTORY;
			routesAccomplished.add(currentLevel.getRoute());
		}
		
		int distanceToPath = getShortestDistanceToPath(new Point(player.getLocationX(), player.getLocationY()));
		
		//reunojentunnistus
		if(distanceToPath < GAME_OVER_DISTANCE){
			gameState = GameState.GAMEOVER;
		}
		else if(distanceToPath < WARNING_DISTANCE_FROM_PATH){
			player.setIsInWarningDistance(true);
		}
		else{
			player.setIsInWarningDistance(false);
		}
		
		//ei liikuta kun zoomaus k&#x160;ynniss&#x160;.
		if(movementPossible){
			player.update();
		}
		
		


	}


}

