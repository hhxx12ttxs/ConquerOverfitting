package de.mns.furian.level.object.flo;

import android.util.Log;



public class Bullet extends FreeLevelObject {

	public static final int TYPE_ENEMY_BULLET = 0x01;
	public static final int TYPE_PLAYER_BULLET = 0x02;
	
	public int type = TYPE_ENEMY_BULLET;

	public double z_speed = 0.1;
	
	
	public void init() {
		

	}


	public void remove() {


	}


	public void trigger() {

		//bullet will fly to user (-z, -z, -z, ...)
		
		switch(type){
		
		case TYPE_ENEMY_BULLET:		//flys to player
		
			this.segment_z_progress -= z_speed;
		
			if(this.segment_z_progress < 0.0){
				this.segment_z--;
				this.segment_z_progress += 1.0d;
			}
		
			break;
			
		case TYPE_PLAYER_BULLET:	//flys to enemy
			
			this.segment_z_progress += z_speed;
			
			if(this.segment_z_progress > 0.999999){
				this.segment_z++;
				this.segment_z_progress -= 1.0d;
			}
		
			break;
		}
		
	}




	

}

