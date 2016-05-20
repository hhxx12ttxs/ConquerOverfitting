package lengkeng.group.SceneManager;

import lengkeng.group.GeneralClass.StaticItem;
import lengkeng.group.LevelManager.LevelManager;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

public class LevelSelector extends ManageableScene implements IManageableScene{
	public static BitmapTextureAtlas bg_bitmapTextureAtlas;
	public static TextureRegion bg_textureRegion; // luu khi load anh
	public static Sprite bg_sprite; // sprite lam anh nen	
	
	private BitmapTextureAtlas SheetMenuBitmapTextureAtlas; // luu anh vao bo nho
	private TextureRegion Level_1_TextureRegion; // start
	private TextureRegion Level_2_TextureRegion; // exit
	
	private StaticItem Level_1;
	private StaticItem Level_2;
	
	public LevelSelector(BaseGameActivity context) {
		super();
		// TODO Auto-generated constructor stub
	}	

	@Override
	public void loadResources(BaseGameActivity context) {	

		this.isLoaded = true;
		mScene.setTouchAreaBindingEnabled(true);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		// background
		bg_bitmapTextureAtlas= new BitmapTextureAtlas(1024,512,TextureOptions.DEFAULT); // luu anh vao bo nho
		bg_bitmapTextureAtlas.clearTextureAtlasSources();		
		bg_textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bg_bitmapTextureAtlas,context,"game script/level selector/MenuChoice_background.png",0,0);
		SceneManager.loadTexture( bg_bitmapTextureAtlas );				
		
		bg_sprite = new Sprite(0,0,bg_textureRegion);
		mScene.setBackground(new SpriteBackground(bg_sprite));
		
		// menu 
		this.SheetMenuBitmapTextureAtlas = new  BitmapTextureAtlas(256, 128, TextureOptions.DEFAULT);
		this.Level_1_TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.SheetMenuBitmapTextureAtlas,
				context, "game script/level selector/Level_1.png", 0, 0 );
		this.Level_2_TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.SheetMenuBitmapTextureAtlas,
				context, "game script/level selector/Level_2.png", 0, 40 );		
		
		SceneManager.loadTexture( this.SheetMenuBitmapTextureAtlas );
		
		Level_1 = new StaticItem( context.getEngine().getCamera().getWidth()/2 - Level_1_TextureRegion.getWidth()/2, 100, this.Level_1_TextureRegion ){
	    	
	    	// chuyen sang chon level
	    	@Override
	    	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,final float pTouchAreaLocalY){
	    			if(pSceneTouchEvent.getAction()== TouchEvent.ACTION_DOWN){
	    				SceneManager.last_Menu_id = SceneManager.Menu_id;
	    				SceneManager.Menu_id = SceneManager.GAME_PLAY;
	    				LevelManager.Level = LevelManager.LEVEL_1_CLASS;
	    				LevelManager.Level_id = LevelManager.SPLASHSCENE;
//	    				SceneManager.load();
//	    				SceneManager.setScene(SceneManager.run());
	    				LevelManager.load();
	    				LevelManager.setScene(LevelManager.run());	
	    			}
	    			return true;
	    		}
	    	};
	    	
	    	Level_2 = new StaticItem( context.getEngine().getCamera().getWidth()/2 - Level_1_TextureRegion.getWidth()/2, 200, this.Level_2_TextureRegion ){	    	
	    	// Thoat khoi game
	    	@Override
	    	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,final float pTouchAreaLocalY){
	    			if(pSceneTouchEvent.getAction()== TouchEvent.ACTION_DOWN){	
	    				SceneManager.last_Menu_id = SceneManager.Menu_id;
	    				SceneManager.Menu_id = SceneManager.GAME_PLAY;
	    				LevelManager.Level = LevelManager.LEVEL_2_MARKET;	 
	    				LevelManager.Level_id = LevelManager.SPLASHSCENE;
	    				SceneManager.load();
	    				SceneManager.setScene(SceneManager.run());	
	    			}
	    			return true;
	    		}
	    	};
		mScene.attachChild( Level_1 );
		mScene.attachChild( Level_2 );
		
		mScene.registerTouchArea(Level_1);
		mScene.registerTouchArea(Level_2);
	}

	@Override
	public Scene run() {
		return this.mScene;
	}

	@Override
	public void unloadResources(BaseGameActivity context) {
		// TODO Auto-generated method stub
		Level_1.removeMe();
		Level_2.removeMe();		
		mScene.detachChildren();
		mScene.detachSelf();
		this.SheetMenuBitmapTextureAtlas.clearTextureAtlasSources();
		bg_bitmapTextureAtlas.clearTextureAtlasSources();
		SceneManager.unloadTexture(SheetMenuBitmapTextureAtlas);		
		SceneManager.unloadTexture(bg_bitmapTextureAtlas);
	}
}

