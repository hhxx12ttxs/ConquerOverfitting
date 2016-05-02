package team3;

import java.util.ArrayList;

import hockey.api.GoalKeeper;
import hockey.api.IObject;
import hockey.api.IPlayer;
import hockey.api.Position;
import hockey.api.Util;

public class Goalie extends GoalKeeper {
    // Middle of our own goalcage, on the goal line
    protected static final Position GOAL_POSITION = new Position(-2600, 0);
    
    int number;
    String name;
    Team team;
    
    public Goalie (String name, int number, Team team) {
        this.number = number;
        this.name = name;
        this.team = team;
    }
    
    // Number of the goalie.
    public int getNumber() { return number; }

    // Name of the goalie.
    public String getName() { return name; }

    // Left handed goalie
    public boolean isLeftHanded() { return true; }

    // Initiate
    public void init() { }

    boolean isPenalty;
    
    // Face off
    
    public void faceOff() {
        isPenalty = false;
    }

    // Called when the goalie is about to receive a penalty shot
    public void penaltyShot() {
        isPenalty = true;
    }
    
    // Intelligence of goalie.
    public void step() {
        
        if (!isPenalty) {
            team.update();
        }
        
        double x = GOAL_POSITION.getX();
        double y = GOAL_POSITION.getY();
        
        IObject puck = getPuck ();
        
        double radius = 150;
        setMessage("" + (int) Util.datan2(getPuck(),new Position(-2600,90)) + " "+ getPuck().getHeading() + " " + (int)Util.datan2(getPuck(),new Position(-2600,-90)));
        double angle = Util.datan2(puck, GOAL_POSITION);
//        if(Util.dist(getPuck(),this) < 2300 && getPuck().getHeading() > Util.datan2(getPuck(),new Position(-2600,90)) && getPuck().getHeading() < Util.datan2(getPuck(),new Position(-2600,-90))){
//        	setMessage("Intercepting");
//        	if(getPuck().getHeading() - Util.datan2(getPuck(), this) > 0){
//        		turn(getPuck().getHeading()+90, MAX_TURN_SPEED);
//        		skate(MAX_SPEED);
//        	} else if(Util.datan2(getPuck(), this) - getPuck().getHeading() > 0){
//        		turn(getPuck().getHeading()-90, MAX_TURN_SPEED);
//        		skate(MAX_SPEED);
//        	}
//        	
//        } else {
    	if(getPuck().getHolder() == null && Util.dist(getPuck(),this) < 2300){
    		//setMessage("Puck is missing");
    	} else {
    		//setMessage("" + angle);
    	}
        
        x = x + Util.clamp(0, Util.cosd(angle)*radius, 2000);
        angle += 360;
        angle %= 360;
        
        //if (angle > 0) {
            //y = y + Util.sind(angle + (80-angle)*(angle/90))*radius;
        //} else {
            y = y + (1-(1-Util.sind(angle))*(1-Util.sind(angle)))*radius;
            
//      }
        
        double speed = MAX_GLIDE;
        double dist = Util.dist2(this, new Position((int)x,(int)y));
        
        double slowdownDist = 200;
        speed *= dist / (slowdownDist*slowdownDist);
        speed = Util.clamp(0,speed,MAX_GLIDE);
        
        //setMessage (""+speed);
        
        skate((int)x, (int)y, (int)speed);
        
        
        turn(getPuck(), MAX_TURN_SPEED);

        
        if (hasPuck()) {
        	double min = 100;
        	int target = -1;
            for (int i=1;i<6;i++) {
                //if (getPlayer(i).getX() < getX() + 40 || team.getPlayerInternal(i).getRole() != Role.ATTACKER) continue;
            	
                if(shotRating(this, getPlayer(i)) < min && getPlayer(i).getX() > -2600) {
                	min = shotRating(this, getPlayer(i));
                	target = i;
                }
            }

            //Passa spelare
            if(target != -1){
            	shoot(getPlayer(target),MAX_SHOT_SPEED);
            }
            turn (0,MAX_TURN_SPEED);
        }
    }
    
    
    public boolean isValidShot (Goalie from, IObject to, int ignorePlayer) {
        
        int heading = Util.collisionHeading(to, from, MAX_SHOT_SPEED/10);
        
        
        IObject originObj = from.getStick();
        double dist = Util.dist(originObj, to);
        
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
}

