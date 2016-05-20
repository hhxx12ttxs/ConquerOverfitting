package team3;

import hockey.api.IObject;
import hockey.api.IPlayer;
import hockey.api.Position;
import hockey.api.Util;

public class LussePlayer extends BasePlayer {
	
	private Role lastRole;
	
	public LussePlayer(Team team, String name){
		super(team,name);
	}

	
    Role role = Role.ATTACKER;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
    	this.lastRole = this.role;
        this.role = role;
    }
    
    public int getNumber() { return 15; }

    // Name of forward
    public String getName() { return name; }

    // Intelligence of forward
    public void step() {
    	time++;
        
        if (isPenalty) {
            update();
        }
    }
    
    int time;
    
    public void update(){
        
        setAimOnStick(true);
        
        if (isPenalty) {
            
            if (hasPuck()) {
                boolean goodShootArea = pointInCone (GOAL_POSITION, new Position(-GOAL_POSITION.getX(),0), 50, this, 40);

                if (goodShootArea) {
                    //setMessage ("GOOD SHOOT AREA");
                } else {
                    //setMessage ("NO GOOD");
                }
                
                
                Position tp = new Position (getX()+400,0 + (int)(Util.sind(time*4)*300 + 100));
                skate(tp,MAX_SPEED);
                
                //if (true) return;

                if (goodShootArea) {
                    IObject stick = getStick();

                    Position goalA1 = new Position(GOAL_POSITION.getX(),GOAL_POSITION.getY()-90);
                    Position goalA2 = new Position(GOAL_POSITION.getX(),GOAL_POSITION.getY()+90);

                    IObject goalKeeper = getPlayer(6);
                    IObject goalKeeper1 = new Position (goalKeeper.getX(), goalKeeper.getY()-40);
                    IObject goalKeeper2 = new Position (goalKeeper.getX(), goalKeeper.getY()+40);

                    double goalAngle1 = Util.datan2(goalA1,stick);
                    double goalAngle2 = Util.datan2(goalA2,stick);
                    double goalKeeperAngle1 = Util.datan2(goalKeeper1,stick);
                    double goalKeeperAngle2 = Util.datan2(goalKeeper2,stick);

                    double gap1 = goalKeeperAngle1 - goalAngle1;
                    double gap2 = goalAngle2 - goalKeeperAngle2;

                    Position largestGap = new Position(0,0);
                    if (gap1 > gap2) {
                        largestGap = new Position((goalA1.getX()*1+goalKeeper1.getX())/2, (goalA1.getY()*1+goalKeeper1.getY())/2);
                    } else {
                        largestGap = new Position((goalA2.getX()*1+goalKeeper2.getX())/2, (goalA2.getY()*1+goalKeeper2.getY())/2);
                    }

                    setMessage ((int)gap1 + " " + (int)gap2);
                    //setMessage ((int)goalAngle1 + " " + (int)goalAngle2 + " " + (int)goalKeeperAngle1 + " " + (int)goalKeeperAngle2);

                    if ((gap1 > 3 || gap2 > 3) && Util.dist(stick, GOAL_POSITION) < 3000) {
                        shoot (largestGap, MAX_SHOT_SPEED);
                        setMessage ("SHOOOOOT!!");
                    } else {

                    }



                    //skate (largestGap,MAX_SPEED);
                } else {
                    setMessage ("NO GOOD SHOOT");


                    skate (new Position (1700,(int)(Math.signum(getY())*800)),MAX_SPEED);
                }
            } else {
                skate (getPuck(),MAX_SPEED/4);
            }
            
            
            
            return;
        }
        
        
    	//skate(500);
    	setMessage("" + team.attack);
    		
    	if(getRole() == Role.DEFENDER){
    		
            Position defPos = new Position (-2600 + 600,0);
            
            LussePlayer lowestPlayer = this;
            LussePlayer highestPlayer = this;
            for (int i=1;i<=PLAYER_OUT_COUNT;i++) {
                int y = getPlayer(i).getY();
                if (y < lowestPlayer.getY() && team.getPlayerInternal(i).role == Role.DEFENDER) {
                    lowestPlayer = team.getPlayerInternal(i);
                }
                if (y > lowestPlayer.getY() && team.getPlayerInternal(i).role == Role.DEFENDER) {
                    highestPlayer = team.getPlayerInternal(i);
                }
            }
            if(getPuck().getX() > 0){
	            if (lowestPlayer == this) {
	                defPos = new Position (-2600 + 600,-350);
	            } else
	            if (highestPlayer == this) {
	                defPos = new Position (-2600 + 600,350);
	            } else{
	                setMessage("3 DEFENDERS!!");
	            }
            } else {
            	LussePlayer closestDefender = this;
            	for(LussePlayer defender : team.getPlayersWithRole(Role.DEFENDER)){
            		if(Util.dist(defender, getPuck()) < Util.dist(closestDefender,getPuck())){
            			closestDefender = defender;
            		}
            	}
            	if(closestDefender == this){
            		// Get closest attacker
            		double closestAttackerDist = 100000;
            		for(LussePlayer attacker : team.getPlayersWithRole(Role.ATTACKER)){
            			if(Util.dist(attacker,getPuck()) < closestAttackerDist){
            				closestAttackerDist = Util.dist(attacker,getPuck()); 
            			}
            		}
            		
            		if(closestAttackerDist < 300){
            			// Attacker is taking care of chasing
            			// Block between goal and puck
                		defPos = new Position((getPuck().getX() + OWN_GOAL.getX())/2,(getPuck().getY() + OWN_GOAL.getY())/2);
            		} else {
            			// Chase puck
            			this.setAimOnStick(false);
            			defPos = new Position(getPuck().getX(),getPuck().getY());
            		}
            	} else {
            		// Block between goal and puck
            		this.setAimOnStick(false);
            		defPos = new Position((getPuck().getX() + OWN_GOAL.getX())/2,(getPuck().getY() + OWN_GOAL.getY())/2);
            	}
            }
            
            skate (defPos,MAX_SPEED/2);
    	} else if (getRole() == Role.ATTACKER) {
    		this.setAimOnStick(true);
            
            if (hasPuck() && getX() < 0) {
                
                for (int i=1;i<=PLAYER_OUT_COUNT;i++) {
                    if (getPlayer(i).getIndex() == getIndex()) {
                        continue;
                    }
                    if (getPlayer(i).getX() < getX() + 40 || team.getPlayerInternal(i).getRole() != Role.ATTACKER) continue;
                    
                    if(isValidShot (this,getPlayer(i),getIndex())) {
                        //Passa spelare
                        this.shoot(getPlayer(i),MAX_SHOT_SPEED/5);
                    }
                }
                
                skate (GOAL_POSITION, MAX_SPEED);
            } else if (hasPuck()) {
                
                boolean goodShootArea = pointInCone (GOAL_POSITION, new Position(-GOAL_POSITION.getX(),0), 90, this, 40);
                
                if (goodShootArea) {
                    //setMessage ("GOOD SHOOT AREA");
                } else {
                    //setMessage ("NO GOOD");
                }
                
                //if (true) return;
                
                if (goodShootArea) {
                    IObject stick = getStick();
                    
                    Position goalA1 = new Position(GOAL_POSITION.getX(),GOAL_POSITION.getY()-90);
                    Position goalA2 = new Position(GOAL_POSITION.getX(),GOAL_POSITION.getY()+90);

                    IObject goalKeeper = getPlayer(6);
                    IObject goalKeeper1 = new Position (goalKeeper.getX(), goalKeeper.getY()-40);
                    IObject goalKeeper2 = new Position (goalKeeper.getX(), goalKeeper.getY()+40);
                    
                    double goalAngle1 = Util.datan2(goalA1,stick);
                    double goalAngle2 = Util.datan2(goalA2,stick);
                    double goalKeeperAngle1 = Util.datan2(goalKeeper1,stick);
                    double goalKeeperAngle2 = Util.datan2(goalKeeper2,stick);

                    double gap1 = goalKeeperAngle1 - goalAngle1;
                    double gap2 = goalAngle2 - goalKeeperAngle2;

                    Position largestGap = new Position(0,0);
                    if (gap1 > gap2) {
                        largestGap = new Position((goalA1.getX()*2+goalKeeper1.getX())/3, (goalA1.getY()*2+goalKeeper1.getY())/3);
                    } else {
                        largestGap = new Position((goalA2.getX()*2+goalKeeper2.getX())/3, (goalA2.getY()*2+goalKeeper2.getY())/3);
                    }
                    
                    setMessage ((int)gap1 + " " + (int)gap2);
                    //setMessage ((int)goalAngle1 + " " + (int)goalAngle2 + " " + (int)goalKeeperAngle1 + " " + (int)goalKeeperAngle2);
                    
                    if ((gap1 > 5 || gap2 > 5) && Util.dist(stick, GOAL_POSITION) < 2300) {
                        shoot (largestGap, MAX_SHOT_SPEED);
                        setMessage ("SHOOOOOT!!");
                    } else {
                        
                    }
                    
                    
                    
                    skateAvoid (largestGap,MAX_SPEED);
                } else {
                    setMessage ("NO GOOD SHOOT");
                    
                    
                    skateAvoid (new Position (1700,(int)(Math.signum(getY())*800)),MAX_SPEED);
                }
                
                //ArrayList<IPlayer> inTri = playersInTri (new Position(getX(),getY()),goalA1,goalA2);
                
                for (int i=1;i<=PLAYER_OUT_COUNT;i++) {
                    if (getPlayer(i).getIndex() == getIndex() || team.getPlayerInternal(i).getRole() != Role.ATTACKER) {
                        continue;
                    }
                    
                    if (goodShootArea) {
                        //setMessage ("GOOD SHOOT AREA");
                        if (getPlayer(i).getX() < getX() - 500) continue;
                    } else {
                        //setMessage ("NO GOOD SHOOT");
                        if (getPlayer(i).getX() > getX()) continue;
                    }
                    if(isValidShot (this,getPlayer(i),getIndex())) {
                        //Passa spelare
                        this.shoot(getPlayer(i),MAX_SHOT_SPEED/5);
                    }
                }
                
                
            } else {
                if (getPuck().isHeld() && !getPuck().getHolder().isOpponent()) {
                    
                    IPlayer p = getPuck().getHolder();
                    Position pos = new Position (p.getX() - 50, p.getY() + (int)(Math.signum(-p.getY()) * 900));
                    
                    skateAvoid (pos,MAX_SPEED);
                } else {
                    double dot = Util.dangle(Util.collisionHeading(getPuck(), this, MAX_SPEED), getHeading());
                    dot = Util.clamp(0.2,Util.cosd(dot) + 0.2f, 1.0f);
                    
                    skate (getPuck(),(int)(MAX_SPEED * dot));
                }
            }
    	}
    }
}

