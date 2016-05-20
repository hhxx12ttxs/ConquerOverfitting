package de.mns.furian.level.object.oos;


import de.mns.furian.level.object.flo.Bullet;
import android.util.Log;

public class Monster extends ObjectOnSegment {

	public double aggressiveness = 0d;
	
	
	
	
	public void trigger(){
		
		
		if(((int)(Math.random()*80)) == 1){
			this.levelHandler.addFreeObject(this, new Bullet());
		}
	}
	
	
	
	
	public void init(){
		
		this.aggressiveness = Double.parseDouble((String)this.properties.getProperty("aggressiveness", "0.2"));

		
	}
	
	
	public void remove(){
		
//		Log.i("Monster", " Ive been removed!!!");
		
	}
	
	
}

