package de.mns.furian.level.logic;

import de.mns.furian.level.LevelModel;
import de.mns.furian.level.object.Segment;
import de.mns.furian.level.object.flo.Bullet;
import de.mns.furian.level.object.flo.FreeLevelObject;
import de.mns.furian.level.object.oos.ObjectOnSegment;
import de.mns.furian.util.ArrayUtil;
import android.util.Log;

public class LevelHandler implements Runnable{

	public final static int INPUT_LEFT = 0x01;
	public final static int INPUT_RIGHT = 0x02;
	public final static int INPUT_UP = 0x03;
	public final static int INPUT_DOWN = 0x04;
	public final static int INPUT_FIRE1 = 0x05;
	
	
	private Thread logicThread;
	
	/** level handler runs with lps (loops per second) */
	private static final int lps = 40;
	
	/** event horizon */
	public static final int horizon = 18;

	/** maximum objects to process in horizon */
	public static final int maxHorizonObjects = 40;

	/** maximum free objects to process in horizon */
	public static final int maxFreeObjects = 40;
	
	
	public boolean running = false;
	
	/** level model to process */
	private LevelModel levelModel;
	
	/** to adjust the correct delay for the defined lps */
	private long loopTime; //last loop timestamp
	private int loopTimeCount; //counts to next analysis
	public int curDelayMillis; //millis of sleep
	public int curLps;  //current loops per second (should be around lps)
	
		
	//------- runtime values for visualizer ------
	public int levelLength;
	public double globalLevelSpeed;
	public double cSegmentLevelSpeed;
	
	public int cPlayerPos;  // x - axis position of player
	public double cPlayerJumpProgress; // current progress of jump (0.0 -> -1.0 || 0.0 -> 1.0 )
	public double cPlayerJumpValAdder;	// value to be added for next evaluation to progress jump

	public int cSegment;	// current segment on z - axis.
	public double cSegmentProgress;  // current progress of segment (to 0.0 -> 1.0 )
	public double cZValAdder;	// value to be added on next evaluation to progress
	
	
	public ObjectOnSegment[] horizonObject;  //  array contains objects to process in horizon width
	public FreeLevelObject[] freeObject;  //  array contains free objects to process in horizon width
	
	public Segment cSegmentObject;
	
	
	public LevelHandler(LevelModel model){
		
		this.levelModel = model;
		
		this.curDelayMillis = 1000 / lps;
		this.loopTimeCount = 0;
		this.loopTime = System.currentTimeMillis();
		
		this.analyse();		
		this.initValues();
	}
	
	private void analyse() {
		
		this.levelLength = this.getLevelModel().segments.length;
		this.globalLevelSpeed = this.getLevelModel().globalLevelSpeed;
		
		
	}
	
	private void initValues() {
		
		//current level speed is 1.0 (is defined by current segment)
		this.cSegmentLevelSpeed = 1.0;
		
		//one segment per second.
		this.cZValAdder = this.globalLevelSpeed * this.cSegmentLevelSpeed / lps;
		this.cSegment = 0;
		this.cSegmentProgress = 0.0;	
		
		//jump progress speed: (0.4sec)
		this.cPlayerJumpValAdder = this.globalLevelSpeed / lps * 2.5;
		
		this.horizonObject = new ObjectOnSegment[maxHorizonObjects];
		this.freeObject = new FreeLevelObject[maxFreeObjects];
		
		
		this.cPlayerPos = 22;
		
	}
	
	
	
	//calculation for value progression:
	private void evaluateZProgression(){
		
		this.cZValAdder = this.globalLevelSpeed * this.cSegmentLevelSpeed / lps;
		
		this.cSegmentProgress += cZValAdder;
		
		if(this.cSegmentProgress > 0.99999999){
			
			//set State to next Segment
			this.cSegmentProgress = cSegmentProgress - 1.0;
			this.cSegment ++;
			this.checkAddRemoveObjects(false);

			if(cSegment >= levelLength){
				
				//games finished ;)...
				this.running = false;
			}
		}
	}
	
	private Segment tmp_segment;
	
	private void checkAddRemoveObjects(boolean full) {

		if (full) {
			for (int z = cSegment, ze = cSegment + horizon; z < ze
					&& z < levelLength; z++) {
				for (int x = 0; x < 36; x++) {
					tmp_segment = levelModel.segments[z][x];
					if (tmp_segment != null && tmp_segment.objects != null) {
						for (int o = 0; o < tmp_segment.objects.length; o++) {
							if (!ArrayUtil.addToArray(this.horizonObject,
									tmp_segment.objects[o])) {
								Log.e("LevelHandler", "HorizonObjects out of bounds. Need more slots");
							} else {
								// new horizon object in game. init!
								tmp_segment.objects[o].preInit(this, tmp_segment);
							}
						}
					}
				}
			}
		} else {
			int curz = cSegment + horizon;
			if (curz < levelLength) {
				for (int x = 0; x < 36; x++) {
					tmp_segment = levelModel.segments[curz][x];
					if (tmp_segment != null && tmp_segment.objects != null) {
						for (int o = 0; o < tmp_segment.objects.length; o++) {
							if (!ArrayUtil.addToArray(this.horizonObject,
									tmp_segment.objects[o])) {
								Log.e("LevelHandler", "HorizonObjects out of bounds. Need more slots");
							} else {
								// new horizon object in game. init!
								tmp_segment.objects[o].preInit(this, tmp_segment);
							}
						}
					}
				}
			}
			if (cSegment > 0) {
				// remove objects behind currenz z - segment:
				//1. the (ObjectOnSegment) horizon objects
				curz = cSegment - 1;
				for (int x = 0; x < 36; x++) {
					tmp_segment = levelModel.segments[curz][x];
					if (tmp_segment != null && tmp_segment.objects != null) {
						for (int o = 0; o < tmp_segment.objects.length; o++) {
							if (!ArrayUtil.removeFromArray(this.horizonObject,
									tmp_segment.objects[o])) {
								Log.e("LevelHandler", "Remove of HorizonObject failed!");
							} else {
								// horizon object out of game!:
								tmp_segment.objects[o].remove();
							}
						}
					}
				}
			}
		}
	}
	
	public void addFreeObject(ObjectOnSegment oos, FreeLevelObject flo){
		
		if (!ArrayUtil.addToArray(this.freeObject, flo)) {
			Log.e("LevelHandler", "FreeObjects out of bounds. Need more slots");
		} else {
			// new free object in game. init!
			flo.preInit(this, oos);
		}
	}
	
	public void removeFreeObject(FreeLevelObject flo){
		
		if (!ArrayUtil.removeFromArray(this.freeObject, flo)) {
			Log.e("LevelHandler", "Remove of FreeObject failed!");
		} else {
			flo.remove();
		}
	}
	
	private void checkCurrentSegment(){
		
		Segment nToSet = levelModel.segments[cSegment][cPlayerPos];
		
		if(cSegmentObject != nToSet){
			
			if(cPlayerJumpProgress == 0.0 && nToSet!=null){
				//if player is on floor:
				analysisSegment(nToSet);
			}
		}
		
	}
	
	private void checkJumpProgress() {

		if(cPlayerJumpProgress != 0.0){
			
			if(cPlayerJumpProgress < 0){
				
				if(cPlayerJumpProgress <= -0.99999999){
					cPlayerJumpProgress = 0.0;
					if(cPlayerPos - 1 < 0){
						cPlayerPos = 35;
					}else{
						cPlayerPos --;
					}
				}else{
					cPlayerJumpProgress -= cPlayerJumpValAdder;
				}
				
			}else{
				
				if(cPlayerJumpProgress >= 0.99999999){
					cPlayerJumpProgress = 0.0;
					if(cPlayerPos + 1 > 35){
						cPlayerPos = 0;
					}else{
						cPlayerPos ++;
					}
				}else{
					cPlayerJumpProgress += cPlayerJumpValAdder;
				}
			}
		}
	}
	

	private void processHorizonObjects() {

		for(int i = 0; i < horizonObject.length; i++){
			
			if(horizonObject[i] != null){
				
				horizonObject[i].trigger();
			}
		}
	}

	private FreeLevelObject tmp_freeLevelObject;
	
	private void processFreeObjects() {
		
		//1. trigger the (FreeLevelObjects) :
		for(int i = 0; i < freeObject.length; i++){
			
			if(freeObject[i] != null){
				
				freeObject[i].trigger();
			}
		}
		
		//2. remove (FreeLevelObjects) running out of horizon:
		for(int i=0; i<maxFreeObjects; i++){
			tmp_freeLevelObject = freeObject[i];
			
			if(tmp_freeLevelObject!=null && (freeObject[i].segment_z < cSegment
					|| freeObject[i].segment_z >= (cSegment+horizon))){
				
				if (!ArrayUtil.removeFromArray(this.freeObject,
						tmp_freeLevelObject)) {
					Log.e("LevelHandler", "Remove of FreeObject failed!");
				} else {
					// horizon object out of game!:
					tmp_freeLevelObject.remove();
				}
			}
		}
	}
	
	
	

	private void analysisSegment(Segment seg){
		
		this.cSegmentLevelSpeed = seg.properties.speed;
	}
	

	public int collision = 0;

	private void checkCollisions() {

		// check if free level objects collide with player...
		if (cPlayerJumpProgress == 0) {	//player is not in jump!

			for (int i = 0; i < maxFreeObjects; i++) {
				tmp_freeLevelObject = this.freeObject[i];
				if (tmp_freeLevelObject != null) {
					if (tmp_freeLevelObject.segment_x == cPlayerPos
							&& (tmp_freeLevelObject.segment_z < cSegment
							|| (tmp_freeLevelObject.segment_z == cSegment
							&& tmp_freeLevelObject.segment_z_progress < cSegmentProgress))) {

						//collides:
						collision += 1;
						
						//remove the free object!
						if (!ArrayUtil.removeFromArray(this.freeObject,
								tmp_freeLevelObject)) {
							Log.e("LevelHandler", "Remove of FreeObject failed!");
						} else {
							// horizon object out of game!:
							tmp_freeLevelObject.remove();
						}
						
					}
				}
			}
		}
	}


	public void startLevel(){
		
		if(!this.running && this.logicThread == null || !this.logicThread.isAlive()){
			
			this.running = true;
			this.logicThread = new Thread(this);
			this.logicThread.start();	
		}
		
	}

	public void run() {

		checkAddRemoveObjects(true);
		
		while(running){
			
			adjustTiming();
			
			evaluateZProgression();
			checkCurrentSegment();
			checkJumpProgress();
			
			processHorizonObjects();
			processFreeObjects();
			
			checkCollisions();
			
			try {
				Thread.sleep(curDelayMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	


	/**
	 * Adjust curDelayMillis time value every 50 ticks
	 * to match the defined loops per seconds...
	 */
	private void adjustTiming(){
		
		this.loopTimeCount++;
		if(this.loopTimeCount > 50){
			this.curLps = (int)(50000.0 / (System.currentTimeMillis() - this.loopTime));
			
			if(this.curLps != lps){
				if(this.curLps>lps)
					this.curDelayMillis++;
				else
					this.curDelayMillis--;
				
//				Log.i("LevelHandler", "adjust curDelayMillis to " + this.curDelayMillis);
			}
//			Log.i("LevelHandler", "cur Lps: " + this.curLps);
			
			this.loopTimeCount = 0;
			this.loopTime = System.currentTimeMillis();
		}
	}



	public LevelModel getLevelModel() {
		
		return this.levelModel;
	}
	
	public void handleInput(int code) {

		switch (code) {

			case INPUT_LEFT:  
				
				if(cPlayerJumpProgress == 0){
					cPlayerJumpProgress -= cPlayerJumpValAdder;
				}
				
				break;

			case INPUT_RIGHT:	
				
				if(cPlayerJumpProgress == 0){
					cPlayerJumpProgress += cPlayerJumpValAdder;
				}
				
				break;

			case INPUT_UP:	break;

			case INPUT_DOWN:  break;

			case INPUT_FIRE1:  
				
				if(cPlayerJumpProgress == 0){
					Bullet pb = new Bullet();
					pb.preInit(this, null);
					pb.type = Bullet.TYPE_PLAYER_BULLET;
					pb.segment_x = cPlayerPos;
					pb.segment_z = cSegment;
					pb.segment_z_progress = this.cSegmentProgress + 0.2;
					if(pb.segment_z_progress>1.0){
						pb.segment_z++;
						pb.segment_z_progress-=1.0d;
					}
					
					if (!ArrayUtil.addToArray(this.freeObject, pb)) {
						Log.e("LevelHandler", "FreeObjects out of bounds. Need more slots");
					}
					
				}
				
				break;
		}
	}
	
	

}

