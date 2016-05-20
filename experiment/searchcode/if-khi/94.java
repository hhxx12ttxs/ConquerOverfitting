package lengkeng.group.GeneralClass;

import java.util.LinkedList;

import lengkeng.group.LevelManager.LevelManager;

import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.pool.GenericPool;

public class SeriesOfAniamatedItemPool extends GenericPool<AnimatedItem> {
	public float posX;
	public float posY;
	public LinkedList<AnimatedItem> arrAniamatedItem;
	public boolean inScene;		
	private TiledTextureRegion[] mTextureRegion;
	protected int count;
	
	public SeriesOfAniamatedItemPool (TiledTextureRegion[] ttr) {
		posX = 0;
		posY = 0;
		arrAniamatedItem = new LinkedList<AnimatedItem>(); 
		inScene = true;	
		mTextureRegion = ttr;
	}
	
	@Override
	/**
	 * tao 1 object gan vao mScene ( khi ko co object nao trong Pool )
	 */
	protected AnimatedItem onAllocatePoolItem() {
		inScene = true;		
		int id = count%mTextureRegion.length;
		count++;
		AnimatedItem animatedItem = new AnimatedItem(posX, posY, mTextureRegion[id].deepCopy());
		LevelManager.getScene().attachChild(animatedItem);
		animatedItem.setAttachToScene(true);		
		arrAniamatedItem.add(animatedItem);
		return animatedItem;
	}
	
	@Override
	/**
	 * reset lai object nhu ban dau
	 */
	public void onHandleObtainItem(final AnimatedItem animatedItem) { 
		animatedItem.reset();
	}
	
	@Override 
	/**
	 * recycle 1 object cu the
	 */
	public void onHandleRecycleItem(final AnimatedItem animatedItem){
		animatedItem.removeMe();			
	}
	
	public void recycleAll() {
		try {
			if (inScene)
//			for (AnimatedItem animatedItem : arrAniamatedItem) {
//				if(animatedItem!=null){
//					this.recyclePoolItem(animatedItem);
//					this.arrAniamatedItem.remove(animatedItem);
//				}
				while(!this.arrAniamatedItem.isEmpty()){
					AnimatedItem animatedItem = this.arrAniamatedItem.poll();
					if(animatedItem!=null){
						this.recyclePoolItem(animatedItem);
						this.arrAniamatedItem.remove(animatedItem);
					}
			}
		} catch (Exception e) {
			Debug.e(e);
		}
	}	
}

