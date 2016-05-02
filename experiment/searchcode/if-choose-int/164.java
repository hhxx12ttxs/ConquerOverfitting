package lengkeng.group.Student;

import android.annotation.SuppressLint;
import java.util.LinkedList;

import lengkeng.group.Grid.Grid;
import lengkeng.group.LevelManager.LevelManager;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseLinear;

/**
 * @author DRAGON
 * @version 1.0
 * @created 15-Thg7-2012 4:07:28 CH
 */
public class Student extends AnimatedSprite{

	private boolean finishStep = true;	
	private LinkedList<Footsteps> Path = new LinkedList<Footsteps>();
	private ReuseMoveModifier reuseMoveModifier;
	private float Velocity = 500;
	private int Direction ;
	private boolean runEnable =true;
	private boolean touchEnable = true;
	private FootstepsPool footstepsPool ;
	
	private IEntityModifierListener modifierListener = new IEntityModifierListener(){	
		@Override
		public void onModifierFinished(IModifier<IEntity> pModifier,
				IEntity pItem) {
			// TODO on finish Step ( finish Modifier )		
			finishStep = true;
			switch(Direction){
			case 0:  // up
				stopAnimation(12);			
				break;
			case 1: // right 
				stopAnimation(8);
				break;
			case 2: // down
				stopAnimation(0);
				break;
			case 3: //  left
				stopAnimation(4);
				break;
			}						
		}

		@Override
		public void onModifierStarted(IModifier<IEntity> pModifier,
				IEntity pItem) {
			// TODO Start Step ( start modifier )
			switch(Direction){
			case 0:  // up
				animate(new long[]{100,100,100,100}, 12, 15, true);
				break;
			case 1: // right 
				animate(new long[]{100,100,100,100}, 8, 11, true);
				break;
			case 2: // down
				animate(new long[]{100,100,100,100}, 0, 3, true);
				break;
			case 3: //  left
				animate(new long[]{100,100,100,100}, 4, 7, true);
				break;
			}
			finishStep = false;			
		}		
	};
	

	public void finalize() throws Throwable {

	}
	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param pTiledTextureRegion
	 */
	public Student(float pX, float pY, TiledTextureRegion pTiledTextureRegion){
		super(pX, pY, pTiledTextureRegion);
		reuseMoveModifier = new ReuseMoveModifier(2, pX, pY, pX, pY, modifierListener, EaseLinear.getInstance());
	      this.registerEntityModifier(reuseMoveModifier);
	}

	/**
	 * add a footsteps to Path
	 * @param footsteps
	 */
	public void addFootsteps(Footsteps footsteps){
		Path.addLast(footsteps);
	}
	
	/**
	 * 
	 * @ return the last footsteps in Student's Path
	 * but not remove
	 */
	public Footsteps getFootsteps(){
		return Path.getLast();
	}
	
	/**
	 * 
	 * @ return the last footsteps in Student's Path and remove footsteps from Path
	 */
	public Footsteps pollFootsteps(){
		return Path.poll();
	}
	
	/**
	 * recycle all footsteps in Student's Path
	 */
	public void recyclePath(){
		while(!Path.isEmpty()){
			Footsteps footsteps = this.pollFootsteps();
			footstepsPool.recyclePoolItem(footsteps);
		}
	}
	
	/**
	 * 
	 * @ return Student's Path is empty ?
	 */
	public boolean PathisEmpty(){
		return Path.isEmpty();
	}
	
	/**
	 * move Student if Student's Path is not empty
	 */
	public void move(){
		if (runEnable){
			if(!Path.isEmpty() && finishStep){
				Footsteps footsteps= pollFootsteps();
				float x = footsteps.getX();
				float y = footsteps.getY();				
		    	Grid.CountFootsteps[Grid.getRow(y)] [Grid.getCol(x)] -=1;
		    		
				this.Direction = footsteps.getDirection();
				reuseMoveModifier.restart(getDuration(x,y), this.getX(), x, this.getY(), y);
				footstepsPool.recyclePoolItem(footsteps);
			}
		}		
	}
	
	/**
	 * Touch Screen to move Student
	 * @param x
	 * @param y
	 * @return 
	 */
	public boolean onSceneTouchEvent(final int x, final int y){
		if(!this.touchEnable) return false;
		int x2 = x;
		int y2 = y;
		Footsteps before_step = null;
		
        x2 = Grid.resetX(x2);
        y2 = Grid.resetY(y2);
        		
        if(!PathisEmpty()){
    	        before_step = getFootsteps();
    	        if(Grid.checkAlignment(before_step.getX(), before_step.getY(), x2, y2)){ 	        	    	        			        			        		
    		        	addFootsteps(x2, y2, 1);
    	        	}
    	        }// end of !mStudent.PathisEmpty()
    	        else
    	        	if (Grid.checkAlignment(this.getX(), this.getY(), x2, y2)){
    	        		addFootsteps(x2, y2, 2);
    	        }// end of checkAlignment with Student
        
        
        //  Find way Automation
        if(!this.PathisEmpty()){
        	before_step = this.getFootsteps();
	        int x1 = (int) before_step.getX();
	        int y1 = (int) before_step.getY();
	        	        
	        if( Grid.checkAlignment(x1, y1, x1, y2) && Grid.checkAlignment(x1, y2, x2, y2) && (Grid.CountFootsteps[Grid.getRow(y2)] [Grid.getCol(x1)] <3)){
	        	addFootsteps(x1, y2, 1);
	        	addFootsteps(x2, y2, 1);
	        }else 
	        	if( Grid.checkAlignment(x1, y1, x2, y1) && Grid.checkAlignment(x2, y1, x2, y2) &&  (Grid.CountFootsteps[Grid.getRow(y1)] [Grid.getCol(x2)] <3)){
	        		addFootsteps(x2, y1, 1);
		        	addFootsteps(x2, y2, 1);
	        	}	        
	    }// end of !mStudent.PathisEmpty()
	     else{
	    	 int x1 = (int) this.getX();
		     int y1 = (int) this.getY();
		     if( Grid.checkAlignment(x1, y1, x1, y2) && Grid.checkAlignment(x1, y2, x2, y2) && (Grid.CountFootsteps[Grid.getRow(y2)] [Grid.getCol(x1)] <3)){
		        	addFootsteps(x1, y2, 2);
		        	addFootsteps(x2, y2, 1);
		        }else 
		        	if( Grid.checkAlignment(x1, y1, x2, y1) && Grid.checkAlignment(x2, y1, x2, y2) && (Grid.CountFootsteps[Grid.getRow(y1)] [Grid.getCol(x2)] <3)){
		        		addFootsteps(x2, y1, 2);
			        	addFootsteps(x2, y2, 1);
		        	}	    
	     }// end of checkAlignment with Student
		return true;	        	
	} // end of move action		
	
	/**
	 * add a Footsteps(X,Y) to Path
	 * @param X
	 * @param Y
	 */
	public void addFootsteps(final int x, final int y, int choose){		
		int x2 = Grid.standardizeX(x);
    	int y2 = Grid.standardizeY(y);
    	if(Grid.CountFootsteps[Grid.getRow(y)] [Grid.getCol(x)] <3){
    		Grid.CountFootsteps[Grid.getRow(y)] [Grid.getCol(x)] +=1;
    		
    		Footsteps footsteps = this.footstepsPool.obtainPoolItem();
    		footsteps.setPosition(x2, y2);			        
    		if(!footsteps.isAttachToScene){
    			LevelManager.getScene().attachChild(footsteps);
    			footsteps.setAttachToScene(true);
    		}
        
    		switch(choose){
    		case 1:
    			Footsteps before_step = getFootsteps();
    			footsteps.setDirection(Grid.getDirection(before_step.getX(), before_step.getY(), x2, y2));
    			footsteps.animate(100,false);
    			addFootsteps(footsteps);
    			break;
	        case 2:
	        	footsteps.setDirection(Grid.getDirection(this.getX(), this.getY(), x2, y2));
		        footsteps.animate(100,false);
				addFootsteps(footsteps);
				break;
	        }
    	}
	}
	
	/**
	 * remove Student from Scene
	 */
	public void removeMe(){
		this.footstepsPool.recycleAll();
		this.setIgnoreUpdate(true);
		this.setVisible(false);
		this.reset();
		this.detachSelf();
		BufferObjectManager.getActiveInstance().unloadBufferObject(this.getVertexBuffer());				
	}

	/**
	 * register Entity Listener (reuseMoveModifier)
	 */
	public void registerListener(){
		this.registerEntityModifier(reuseMoveModifier);
	}
	
	/**
	 * unregister Entity Listener (reuseMoveModifier)
	 */
	public void unregisterListener(){
		this.unregisterEntityModifier(reuseMoveModifier);
	}
	
	/**
	 * set Student's Velocity
	 * @param velocity
	 */
	public void setVelocity(float velocity){
		Velocity = velocity; 
	}
	
	/**
	 * @return Student's Velocity
	 * @param velocity
	 */
	public float getVelocity(){
		return Velocity;
	}
	
	/**
	 * set Student's Direction
	 * @param direction
	 */
	public void setDirection(int direction){
		this.Direction = direction;
	}
	
	/**
	 * @return Student's Direction
	 */
	public int getDirection(){
		return this.Direction;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return Duration from Student's position to (x, y)
	 */
	@SuppressLint("FloatMath")
	private float getDuration(final float x, final float y){
		float d = (float) Math.sqrt((this.getX() - x)*(this.getX() - x) + (this.getY() - y)*(this.getY() - y)) / this.Velocity;
		return (float) (d+0.001);
	}

	/**
	 * 
	 * @return Student's runEnable
	 */
	public boolean getRunEnable(){
		return this.runEnable;
	}
	
	/**
	 * set Student's runEnable = value
	 * @param value
	 */
	public void setRunEnable(boolean value){
		runEnable = value;
	}
	
	/**
	 * 
	 * @return Student's TouchEnable
	 */
	public boolean getTouchEnable(){
		return this.touchEnable;
	}
	
	/**
	 * set Student's TouchEnable = value
	 * @param value
	 */
	public void setTouchEnable(boolean value){
		touchEnable = value;
	}

	/**
	 * set Student's finishStep = value
	 * @param value
	 */
	public void setfinishStep(boolean value){
		finishStep = value; 
	}
	
	/**
	 * 
	 * @return Student's finishStep
	 */
	public boolean getfinishStep(){
		return finishStep;
	}

	/**
	 * set Student's FootStepsPool's Texture
	 */
	public void setFootStepsPoolTexture(TiledTextureRegion FootstepsTextureRegion){
		this.footstepsPool = new FootstepsPool(FootstepsTextureRegion);
	}
}//end Student
