package team3;

import java.util.ArrayList;

import hockey.api.IObject;
import hockey.api.IPlayer;
import hockey.api.Position;
import hockey.api.Player;
import hockey.api.Util;

public abstract class BasePlayer extends Player {
    protected Team team;
    protected String name;
	
    public final int PLAYER_OUT_COUNT = 5;
    public final Position OWN_GOAL = new Position(-2600,0);
    
	public BasePlayer(Team team, String name){
		this.team = team;
		this.name = name;
	}
    // The middle of the opponents goal, on the goal line
    protected static final Position GOAL_POSITION = new Position(2600, 0);

    // Left handed?
    public boolean isLeftHanded() { return false; }

    
    boolean isPenalty = false;
    
    // Initiate
    public void init() {
    }

    // Face off
    public void faceOff() {
        isPenalty = false;
    }

    // Penalty shot
    public void penaltyShot() {
        isPenalty = true;
    }

    // Player intelligence goes here
    public void step() {
    }
    
    public static double triangleArea2 (double ax, double ay, double bx, double by, double cx, double cy){
        return (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
    }
    
    public static double triangleArea2 (IObject a, IObject b, IObject c){
        return triangleArea2(a.getX(),a.getY(),b.getX(),b.getY(),c.getX(),c.getY());
    }
    
    public static boolean isInTriangle (IObject a, IObject b, IObject c, IObject p){
        return triangleArea2(a,b,p) <= 0 && triangleArea2(b,c,p) <= 0 && triangleArea2(c,a,p) <= 0;
    }
    
    public static boolean pointInCone (IObject origin, IObject to, double angle, IObject obj, double objRadius) {
        double a = Util.datan2(to, origin);
        double a2 = Util.datan2(obj,origin);
        
        double maxDist = Util.dist2(origin, to);
        
        if (Util.dangle(a, a2) >= angle) {
            double dist = Util.dist(origin, obj);
            
            if (dist+objRadius > maxDist) return true;
            
            IObject right = new Position (origin.getX() + (int)(Util.cosd(a-angle)*dist),origin.getY() + (int)(Util.sind(a-angle)*dist));
            IObject left = new Position (origin.getX() + (int)(Util.cosd(a+angle)*dist),origin.getY() + (int)(Util.sind(a+angle)*dist));
            
            double d2 = Math.min(Util.dist(obj,right), Util.dist(obj,left));
            if (d2 < objRadius) return false;
            
            
        } else {
            
            return true;
        }
        
        return Util.dangle(a, a2) < angle;
    }
    
    public ArrayList<IPlayer> playersInTri(IObject a, IObject b, IObject c) {
    	ArrayList<IPlayer> conePlayers = new ArrayList<IPlayer>();
    	for(int i = 0;i<12;i++){
    		int radius = 70;
    		if(i == 0 || i == 6){
    			radius = 80;
    		}
    		if(isInTriangle (a,b,c,getPlayer(i))){
    			conePlayers.add(getPlayer(i));
    		}
    	}
    	return conePlayers;
    }
    
    public ArrayList<IPlayer> playersInCone(IObject origin, IObject to, double angle){
    	ArrayList<IPlayer> conePlayers = new ArrayList<IPlayer>();
    	for(int i = 0;i<12;i++){
    		int radius = 70;
    		if(i == 0 || i == 6){
    			radius = 80;
    		}
    		if(pointInCone(origin, to, angle, getPlayer(i), radius)){
    			conePlayers.add(getPlayer(i));
    		}
    	}
    	return conePlayers;
    }
   
    
    public void skateAvoid (IObject target, int speed) {
        
        //skate (target,speed);
        //if (true) return;
        
        int heading = Util.collisionHeading(target, this, speed);
        
        double fx = 0;
        double fy = 0;
        
        double force = 250;
        for (int i=7;i<11;i++) {
            double dx = getPlayer(i).getX()-getX();
            double dy = getPlayer(i).getY()-getY();
            
            if (Util.dangle(Util.datan2(getPlayer(i), this),getHeading()) > 90) continue;
            
            double dist = Util.dist(getPlayer(i), this);
            if (dist < 400) {
                dx /= dist*dist;
                dy /= dist*dist;
                
                fx -= dx;
                fy -= dy;
            }
        }
        
        fx *= force;
        fy *= force;
        
        double dirx = Util.cosd(heading) + fx;
        double diry = Util.sind(heading) + fy;
        
        heading = (int)Util.datan2(diry,dirx);
        
        skate(speed);
        turn(heading,MAX_TURN_SPEED);
    }
    public boolean isValidShot (LussePlayer from, IObject to, int ignorePlayer) {
        
        int heading = Util.collisionHeading(to, from, MAX_SHOT_SPEED/5);
        
        
        IObject originObj = from.getStick();
        double dist = Util.dist(originObj, to);
        
        if (dist < 200) return false;
        
        IObject targetObj = new Position (originObj.getX() + (int)(Util.cosd(heading)*dist),
                originObj.getY() + (int)(Util.sind(heading)*dist));
        
        for (int i=0;i<12;i++) {
            IPlayer p = getPlayer(i);
            if (p.getIndex() == ignorePlayer || p.getIndex() == from.getIndex()) continue;
            
            if (BasePlayer.pointInCone (originObj, targetObj, 10,p, 70)) {
                return false;
            }
        }
        return true;
    }
    
    public double shotRating(IPlayer p, IObject target){
    	double rating = 1;
    	for(IPlayer other : playersInCone(p,target,25)){
    		rating += 0.1;
    		if(p.isOpponent() != other.isOpponent()){
    			rating += 0.5;
    			if(Util.dist(target,other)<= 150){
        			rating += 0.3;
        		}
    		}
    		
    	}
    	return rating;
    }
}

