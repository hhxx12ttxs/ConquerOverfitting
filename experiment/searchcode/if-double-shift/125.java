import model.*;


public final class MyStrategy implements Strategy {
	
	static final double MIN_SHOT_ANGLE = Math.PI / 36;
	static final double MIN_BONUS_ANGLE = Math.PI / 4;
	static final int TICK_BEFORE_SHOT = 10;
	static final int CLOSE_DISTANCE = 400;
	static final int LOW_HEALTH = 30;
	static final int SPEED = 17;
	static String LAST_AIM = "noname";
	

    @Override
    public void move(Tank self, World world, Move move) {
    	Tank [] tanks = world.getTanks();
    	Bonus [] bonuses = world.getBonuses();
    	

    	double min_angle_to_enemy = 1E20;
    	double min_dist_to_enemy = 1E20;
    	double min_dist_to_bonus = 1E20;
        int selected_bonus = bonuses.length;
        int selected_to_shoot = tanks.length;
        int selected_to_ram = tanks.length;
        boolean priority_to_medikit = false;
        
        if (LAST_AIM.equals("noname")) {
        	move.setLeftTrackPower(-1.0);
        	move.setRightTrackPower(-1.0);
        	double max_dist = 0;
        	for (int i = 0; i < tanks.length; ++i) {
        		double curr_dist = self.getDistanceTo(tanks[i]);
        		if (curr_dist > max_dist) {
        			max_dist = curr_dist;
        			selected_to_shoot = i;
        		}
        	}
        	LAST_AIM = tanks[selected_to_shoot].getPlayerName();
        	move.setFireType(FireType.PREMIUM_PREFERRED);
        }
        
        if (self.getCrewHealth() < LOW_HEALTH) {
        	priority_to_medikit = true;
        }
        
      //----------------------------------------TANKS-------------------------------------------------
        
        for(int i = 0; i < tanks.length; ++i) {             
            Tank tank = tanks[i];
            
            if (!tank.isTeammate() && tank.getCrewHealth() != 0) {
            	if (tank.getCrewHealth() < LOW_HEALTH && self.getDistanceTo(tank) < CLOSE_DISTANCE) {
            		selected_to_shoot = i;
            		break;
            	}
                double angle_to_enemy = Math.abs(self.getTurretAngleTo(tank)); 
                if (angle_to_enemy < min_angle_to_enemy) {          
                    min_angle_to_enemy = angle_to_enemy;
                    selected_to_shoot = i;
                }
                double dist_to_enemy = self.getDistanceTo(tank);
                if (dist_to_enemy < min_dist_to_enemy) {
                	min_dist_to_enemy = dist_to_enemy;
                	selected_to_ram = i;
                }
            }
        }
        
        if (min_dist_to_enemy < CLOSE_DISTANCE && self.getDistanceTo(tanks[selected_to_shoot]) > CLOSE_DISTANCE) {
        	selected_to_shoot = selected_to_ram;
        }
        
        for (int i = 0; i < tanks.length; ++i) {
        	Tank tank = tanks[i];
        	if (tank.getPlayerName().equals(LAST_AIM) && tank.getCrewHealth() > 0) {
        		if (self.getDistanceTo(tank) < CLOSE_DISTANCE) {
        			selected_to_shoot = i;
        		}
        		break;
        	}
        }
        


        if (selected_to_shoot != tanks.length) {
        	Tank tank = tanks[selected_to_shoot];
            double angle_to_enemy = self.getTurretAngleTo(tank); 
            double shift = 0;
            
            /*if (self.getDistanceTo(tank) > CLOSE_DISTANCE) { //some failed magic
            	double d = 0;
            	double x1, y1, x2, y2, x, y;
            	x1 = self.getX();	y1 = self.getY();
            	x2 = tank.getX();	y2 = tank.getY();
            	x = tank.getX() + tank.getSpeedX();	y = tank.getY() + tank.getSpeedY();
            	d = Math.abs(((y2 - y1)*x + (x1 - x2)*y + y1*x2 - y2*x1))/Math.sqrt((y2 - y1)*(y2 - y1) + (x1 - x2)*(x1 - x2));
            	shift = self.getDistanceTo(tank)/SPEED*Math.atan(d/self.getDistanceTo(tank));
            }*/
            
            if (angle_to_enemy - shift > MIN_SHOT_ANGLE) {         
                move.setTurretTurn(1.0);
                
            } else if (angle_to_enemy - shift < -MIN_SHOT_ANGLE) {  
                move.setTurretTurn(-1.0);
                
            } else {
                move.setFireType(FireType.PREMIUM_PREFERRED);
                LAST_AIM = tank.getPlayerName();
            }
        }
        
        if (selected_to_ram != tanks.length && self.getDistanceTo(tanks[selected_to_ram]) < CLOSE_DISTANCE
        		&& tanks[selected_to_ram].getCrewHealth() < LOW_HEALTH) {
        	double angle_to_bonus = self.getAngleTo(tanks[selected_to_ram]); 
        	if (angle_to_bonus > MIN_BONUS_ANGLE && angle_to_bonus < Math.PI-MIN_BONUS_ANGLE) {
         	   if (move.getTurretTurn() == 1.0) {
         		   move.setLeftTrackPower(1.0);         
         		   move.setRightTrackPower(-1.0);
         		   
         	   } else {
         		   move.setLeftTrackPower(-1.0);      
         		   move.setRightTrackPower(1.0);
         	   }
         	  
            } else if (angle_to_bonus < -MIN_BONUS_ANGLE && angle_to_bonus > -Math.PI+MIN_BONUS_ANGLE) {
         	   if (move.getTurretTurn() == -1.0) {
         		   move.setLeftTrackPower(-1.0);         
         		   move.setRightTrackPower(1.0);
         		   
         	   } else {
         		   move.setLeftTrackPower(1.0);      
         		   move.setRightTrackPower(-1.0);
         	   }
         	  
            } else {
         	   if (angle_to_bonus > -MIN_BONUS_ANGLE && angle_to_bonus < MIN_BONUS_ANGLE) {
         		   move.setLeftTrackPower(1.0);         
         		   move.setRightTrackPower(1.0); 
         		   
         	   } else {
         		   move.setLeftTrackPower(-1.0);         
         		   move.setRightTrackPower(-1.0); 
         	   }   
            }
            return;
        }
        
        
      //----------------------------------------BONUSES-------------------------------------------------
        
        if (priority_to_medikit) {
        	for(int i = 0; i < bonuses.length; ++i) {
        		Bonus bonus = bonuses[i];
        		if (bonus.getType().equals(BonusType.MEDIKIT)) {
        			double dist_to_bonus = self.getDistanceTo(bonus);
        			if (dist_to_bonus < min_dist_to_bonus) {
        				min_dist_to_bonus = dist_to_bonus;
        				selected_bonus = i;
        			}
        		}
        	}
    	}
       
        if (selected_bonus == bonuses.length || self.getDistanceTo(bonuses[selected_bonus]) > CLOSE_DISTANCE 
        		|| !priority_to_medikit) {
        	for(int i = 0; i < bonuses.length; ++i) {
        		Bonus bonus = bonuses[i];
        		double dist_to_bonus = self.getDistanceTo(bonus);
        		if (dist_to_bonus < min_dist_to_bonus) {            
        			min_dist_to_bonus = dist_to_bonus;
        			selected_bonus = i;
        		}
        	}
        }

        if (selected_bonus != bonuses.length) {
           double angle_to_bonus = self.getAngleTo(bonuses[selected_bonus]); 
           if (angle_to_bonus > MIN_BONUS_ANGLE && angle_to_bonus < Math.PI-MIN_BONUS_ANGLE) {
        	   if (move.getTurretTurn() == 1.0) {
        		   move.setLeftTrackPower(1.0);         
        		   move.setRightTrackPower(-1.0);
        		   
        	   } else {
        		   move.setLeftTrackPower(-1.0);      
        		   move.setRightTrackPower(1.0);
        	   }
        	  
           } else if (angle_to_bonus < -MIN_BONUS_ANGLE && angle_to_bonus > -Math.PI+MIN_BONUS_ANGLE) {
        	   if (move.getTurretTurn() == -1.0) {
        		   move.setLeftTrackPower(-1.0);         
        		   move.setRightTrackPower(1.0);
        		   
        	   } else {
        		   move.setLeftTrackPower(1.0);      
        		   move.setRightTrackPower(-1.0);
        	   }
        	  
           } else {
        	   if (self.getRemainingReloadingTime() < TICK_BEFORE_SHOT) {
        		   move.setLeftTrackPower(0.0);         
        		   move.setRightTrackPower(0.0); 
        		 
        	   } else if (angle_to_bonus > -MIN_BONUS_ANGLE && angle_to_bonus < MIN_BONUS_ANGLE) {
        		   move.setLeftTrackPower(1.0);         
        		   move.setRightTrackPower(1.0); 
        		   
        	   } else {
        		   move.setLeftTrackPower(-1.0);         
        		   move.setRightTrackPower(-1.0); 
        	   }   
           }
        }
    }

    @Override
    public TankType selectTank(int tankIndex, int teamSize) {
        return TankType.MEDIUM;
    }
}

