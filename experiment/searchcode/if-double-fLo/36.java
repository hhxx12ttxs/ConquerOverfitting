package de.mns.furian.level.object.flo;

import de.mns.furian.level.logic.LevelHandler;
import de.mns.furian.level.object.oos.ObjectOnSegment;

public abstract class FreeLevelObject {

	//coordinates of free object:
	public double segment_z_progress;	// 0.0 -> 1.0  /def: 0.5
	public int segment_z;				
	public double segment_x_progress;	// 0.0 -> 1.0  /def: 0.5
	public int segment_x;
	
    public ObjectOnSegment oos;
    public LevelHandler levelHandler;
	
	
	public void preInit(LevelHandler lh, ObjectOnSegment oos){
		
		this.levelHandler = lh;
		this.oos = oos;
		
		this.segment_z_progress = 0.5d;
		this.segment_x_progress = 0.5d;
		
		if(oos!=null){
			this.segment_z = oos.segment.zPos;
			this.segment_x = oos.segment.xPos;
		}
		
		this.init();
	}
	
	/** Called when Level Handler puts object in*/
	public abstract void init();
	
	public abstract void trigger();
	
	/** Called when Level Handler removes object*/
	public abstract void remove();
	
	
	
}

