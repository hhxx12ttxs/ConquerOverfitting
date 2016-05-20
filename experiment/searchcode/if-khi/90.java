package lengkeng.group.Level_2;

import lengkeng.group.GeneralClass.AnimatedItem;
import lengkeng.group.Grid.Grid;
import lengkeng.group.LevelManager.LevelManager;
import lengkeng.group.Timer.Timer;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.AnimatedSprite.IAnimationListener;

public class GameLoopUpdateHandler implements IUpdateHandler{	
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		// TODO Auto-generated method stub
		Level_2_Market_Scene.mStudent.move();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	private void checkCollidesWithBook(){
		
	}
	
	private void checkCollidesWithClock(){
		for (AnimatedItem clock : Level_2_Market_Scene.ClockPool.arrAniamatedItem) {
			if ( (clock.collidesWith(Level_2_Market_Scene.mStudent)&& (clock.isAttachToScene))){
				addExplosion(clock.getX(), clock.getY());	
				Level_2_Market_Scene.ClockPool.recyclePoolItem(clock);		
				Level_2_Market_Scene.timer.incSecond(10); // tang diem
			}
		}
	}
	
	private void checkCollidesWithShoes(){
		for (AnimatedItem shoes : Level_2_Market_Scene.ShoesPool.arrAniamatedItem) {
			if ( (shoes.collidesWith(Level_2_Market_Scene.mStudent)&& (shoes.isAttachToScene))){
				addExplosion(shoes.getX(), shoes.getY());	
				Level_2_Market_Scene.ShoesPool.recyclePoolItem(shoes);				
				Level_2_Market_Scene.mStudent.setVelocity(Level_2_Market_Scene.mStudent.getVelocity() + 150); // tang toc do
			}
		}
	}
	
	private void addExplosion(final float x, final float y){
		
	}

	public static void adddialog(final float x, final float y){ // them vao hieu ung trai tim khi nguoi va cham vao chuong ngai vat di dong
		
	}
	
}

