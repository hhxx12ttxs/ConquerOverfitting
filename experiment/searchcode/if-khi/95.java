package lengkeng.group.Level_1;

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
		if(Level_1_Class_Scene.Touchable){
			checkCollidesWithBook();
			checkCollidesWithClock();		
			checkCollidesWithShoes();

			if (Level_1_Class_Scene.teacher.checkCollidesWithStudent(Level_1_Class_Scene.mStudent))
				Level_1_Class_Scene.teacher.studentCollidesWithMobileBlock(Level_1_Class_Scene.mStudent, Level_1_Class_Scene.dialog_Pool);
		
			Level_1_Class_Scene.teacher.move();
			Level_1_Class_Scene.mStudent.move();
		
			Level_1_Class_Scene.scoreBook.updateScore();
			Level_1_Class_Scene.timer.updateTimer();
		
			if(Timer.isTimeOut()){
				Level_1_Class_Scene.Touchable = false;								
				LevelManager.finishLevel.setResult(Level_1_Class_Scene.scoreBook.getResult(), Level_1_Class_Scene.scoreBook.getTextScore(), Level_1_Class_Scene.scoreBook.iscompletely() );												
				
				LevelManager.saveScore(Level_1_Class_Scene.scoreBook.getScore(),Level_1_Class_Scene.scoreBook.getScoreRequirements() );
				LevelManager.finishLevel();
				LevelManager.getScene().clearUpdateHandlers();				
			}
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	private void checkCollidesWithBook(){
		for (AnimatedItem book : Level_1_Class_Scene.bookPool.arrAniamatedItem) {
			if ( (book.collidesWith(Level_1_Class_Scene.mStudent)&& (book.isAttachToScene))){
				addExplosion(book.getX(), book.getY());		
				Level_1_Class_Scene.bookPool.recyclePoolItem(book);		
				Level_1_Class_Scene.scoreBook.incScore(1); // tang diem								
			}
		}
	}
	
	private void checkCollidesWithClock(){
		for (AnimatedItem clock : Level_1_Class_Scene.ClockPool.arrAniamatedItem) {
			if ( (clock.collidesWith(Level_1_Class_Scene.mStudent)&& (clock.isAttachToScene))){
				addExplosion(clock.getX(), clock.getY());	
				Level_1_Class_Scene.ClockPool.recyclePoolItem(clock);		
				Level_1_Class_Scene.timer.incSecond(10); // tang diem
			}
		}
	}
	
	private void checkCollidesWithShoes(){
		for (AnimatedItem shoes : Level_1_Class_Scene.ShoesPool.arrAniamatedItem) {
			if ( (shoes.collidesWith(Level_1_Class_Scene.mStudent)&& (shoes.isAttachToScene))){
				addExplosion(shoes.getX(), shoes.getY());	
				Level_1_Class_Scene.ShoesPool.recyclePoolItem(shoes);				
				Level_1_Class_Scene.mStudent.setVelocity(Level_1_Class_Scene.mStudent.getVelocity() + 150); // tang toc do
			}
		}
	}
	
	private void addExplosion(final float x, final float y){
		final AnimatedItem explosion = Level_1_Class_Scene.explosion_3Pool.obtainPoolItem();
		explosion.setPosition(x, y);		
		
		// x : cot ----------------------
		// y : hang ---------------------
		Grid.setItem(Grid.getRow(y), Grid.getCol(x), false);
		
		if (!explosion.isAttachToScene()) {
			LevelManager.getScene().attachChild(explosion);
			explosion.setAttachToScene(true);
		}
		
		explosion.animate(20, false, new IAnimationListener () {
		    @Override
		    public void onAnimationEnd(final AnimatedSprite pAnimatedSprite) {
				LevelManager.getEngine().runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						Level_1_Class_Scene.explosion_3Pool.recyclePoolItem(explosion);
					}
				});
			}
		});
	}

	public static void adddialog(final float x, final float y){ // them vao hieu ung trai tim khi nguoi va cham vao chuong ngai vat di dong
		final AnimatedItem dialog = Level_1_Class_Scene.dialog_Pool.obtainPoolItem();
		dialog.setPosition(x, y);
		// x : cot ----------------------
		// y : hang ---------------------
		if (dialog.isAttachToScene() == false) {
			LevelManager.getScene().attachChild(dialog);
			dialog.setAttachToScene(true);
		}
		dialog.animate(100, 4, new IAnimationListener () {
		    @Override
		    public void onAnimationEnd(final AnimatedSprite pAnimatedSprite) {
				LevelManager.getEngine().runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						Level_1_Class_Scene.dialog_Pool.recyclePoolItem(dialog);
					}
				});
			}
		});
	}
	
}

