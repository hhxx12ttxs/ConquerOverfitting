// date create 
// author Nguyen Thanh Linh
package lengkeng.group.mobileBlock;

import lengkeng.group.GeneralClass.AnimatedItemPool;
import lengkeng.group.GeneralClass.AnimatedItem;
import lengkeng.group.LevelManager.LevelManager;
import lengkeng.group.Level_1.Level_1_Class_Scene;
import lengkeng.group.Student.ReuseMoveModifier;
import lengkeng.group.Student.Student;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.modifier.ease.EaseLinear;

public class AI extends MobileBlock{
	private float TIME_STUN = 1f;
	private float TIME_RELEASE = 2f;
	private int loopCountanimationItem = 3;
	
	private boolean finishedCheckCollidesWithBlock = false;
	
	public AI(float pX, float pY, TiledTextureRegion pTiledTextureRegion){
		
		super(pX, pY, pTiledTextureRegion);
		reuseMoveModifier = new ReuseMoveModifier(2, pX, pY, pX, pY, modifierListener, EaseLinear.getInstance());
	      this.registerEntityModifier(reuseMoveModifier);	
	}
	public boolean checkCollidesWithStudent(Student mStudent){
		float studentX = mStudent.getX();
		float studentY = mStudent.getY();
		float AIX = getX();
		float AIY = getY();

		if ( (this.collidesWith(Level_1_Class_Scene.mStudent)) && !finishedCheckCollidesWithBlock){
			if(studentX == AIX){
				if (this.Direction == 2 && (studentY > AIY))
					return true;
				if (this.Direction == 0 && (studentY < AIY))
					return true;
				return false;
			} if (studentY == AIY){
				if (this.Direction == 1 && (studentX > AIX))
					return true;
				if (this.Direction == 3 && (studentX < AIX))
					return true;
				return false;							
			} if ((studentX == AIX) && (studentY == AIY))
				return true;
			return false;				
		}
		return false;
	}
	private void changeDirectionWhenMeetStudent(){
		switch(Direction){
		case 0: Direction = 2; break;
		case 1: Direction = 3; break;
		case 2: Direction = 0; break;
		case 3: Direction = 1; break;
		default: break;
		}
	}
	private void changAnimateOfStudent(Student mStudent){
		switch(Direction){
		case 0: // AI's up 
//			mStudent.animate(new long[]{100}, new int[]{0}, 0); 			
			mStudent.stopAnimation(0);
			break;
		case 1: //  AI's right
			mStudent.stopAnimation(4);
			break;
		case 2:  // AI's down
			mStudent.stopAnimation(12);
			break;
		case 3: // AI's left
			mStudent.stopAnimation(8);
			break;
		default: break;
			
		}		
	}
	public void studentCollidesWithMobileBlock(final Student mStudent, final  AnimatedItemPool animationItemPool){	
		float AIX = getX();
		float AIY = getY();
		mStudent.unregisterListener();
		mStudent.recyclePath();
		mStudent.setTouchEnable(false);
		
		this.unregisterListener();
		finishedCheckCollidesWithBlock = true; 
		changAnimateOfStudent(mStudent);
		changeDirectionWhenMeetStudent();
		
		// can chinh toa do cho dung' ung' voi tung` nhan vat, tung hieu ung'
		addanimationItem(AIX, AIY-60, animationItemPool, mStudent);

		TimerHandler Timer2;
		
		float mEffectSpawnDelay2 = TIME_RELEASE;
		Timer2 = new TimerHandler(mEffectSpawnDelay2,false, new ITimerCallback(){
			@Override
			public void onTimePassed(TimerHandler pTimerHandler){
				mStudent.setTouchEnable(true);
	            finishedCheckCollidesWithBlock = false;
	            // nhung Timer chi dung 1 lan, roi lan sau lai tao new TimerHandler thi can unregister sau khi ket thuc
	            LevelManager.getEngine().unregisterUpdateHandler( pTimerHandler );

			}
		});
		LevelManager.getEngine().registerUpdateHandler(Timer2);
	}
	
	public void addanimationItem(final float x, final float y,final  AnimatedItemPool animationItemPool,final Student mStudent){ // them vao hieu ung trai tim khi nguoi va cham vao chuong ngai vat di dong
		final AnimatedItem animationItem = animationItemPool.obtainPoolItem();		
		animationItem.setPosition(x, y);
		// x : cot ----------------------
		// y : hang ---------------------
		if (animationItem.isAttachToScene() == false) {
			LevelManager.getScene().attachChild(animationItem);
			animationItem.setAttachToScene(true);
		}
		animationItem.animate((long) (TIME_STUN*1000/loopCountanimationItem/animationItem.getTextureRegion().getTileCount()), loopCountanimationItem, new IAnimationListener () {
		    @Override
		    public void onAnimationEnd(final AnimatedSprite pAnimatedSprite) {
				LevelManager.getEngine().runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						animationItemPool.recyclePoolItem(animationItem);
						mStudent.registerListener();
			            mStudent.setTouchEnable(true);
			            registerListener(); 
					}
				});
			}
		});
	}
	
	public float getTIME_STUN(){
		return TIME_STUN;
	}
	public void setTIME_STUN(float value){
		TIME_STUN = value;
	}
	
	public float getTIME_REALEASE(){
		return TIME_RELEASE;
	}
	public void setTIME_REALEASE(float value){
		TIME_RELEASE = value;
	}
}

