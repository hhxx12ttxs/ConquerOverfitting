package lengkeng.group.SceneManager;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;

import android.graphics.Color;

public class MenuGame extends ManageableScene implements IManageableScene{
	public static BitmapTextureAtlas bg_bitmapTextureAtlas;
	public static TextureRegion bg_textureRegion; // luu khi load anh
	public static Sprite bg_sprite; // sprite lam anh nen			
	
	private BitmapTextureAtlas mFontTexture; // font vao bbo nho	
	private Font mfont; // luu lai font
	
	private Text StartText;
	private Text ExitText;
	
	public MenuGame(BaseGameActivity context) {
		super();
	}
	
	/**
	 * Load the scene and any assets we need.
	 */
	@Override
	public void loadResources(BaseGameActivity context) {
		
		this.isLoaded = true;
		this.mScene.setTouchAreaBindingEnabled(true);		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game script/menu game/");				
		
		bg_bitmapTextureAtlas= new BitmapTextureAtlas(1024,512,TextureOptions.DEFAULT); // luu anh vao bo nho
		bg_bitmapTextureAtlas.clearTextureAtlasSources();		
		bg_textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bg_bitmapTextureAtlas,context,"menu_background.png",0,0);
		SceneManager.loadTexture( bg_bitmapTextureAtlas );
			
		bg_sprite = new Sprite(0,0,bg_textureRegion);
		mScene.setBackground(new SpriteBackground(bg_sprite));
		
		mFontTexture = new BitmapTextureAtlas(256,256,TextureOptions.BILINEAR_PREMULTIPLYALPHA);		        
		FontFactory.setAssetBasePath("font/");
        mfont = FontFactory.createFromAsset(mFontTexture, context, "BRADHITC.TTF", 70, true, Color.rgb(85, 91, 87));
        
    	//load
        SceneManager.loadTexture(mFontTexture);
    	SceneManager.loadFont(mfont);
    	
		 this.StartText = new Text(380, 180, mfont, "Menu",HorizontalAlign.CENTER){
	    	
	    	// chuyen sang chon level
	    	@Override
	    	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,final float pTouchAreaLocalY){
	    			if(pSceneTouchEvent.getAction()== TouchEvent.ACTION_DOWN){
	    				SceneManager.last_Menu_id = SceneManager.Menu_id;
	    				SceneManager.Menu_id = SceneManager.CHOOSE_LEVEL;
	    				SceneManager.load();
	    				SceneManager.setScene(SceneManager.run());	
	    			}
	    			return true;
	    		}
	    	};	    
	   
	    this.ExitText = new Text(420, 330, mfont, "Exit",HorizontalAlign.CENTER){
	    	
	    	// Thoat khoi game
	    	@Override
	    	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,final float pTouchAreaLocalY){
	    			if(pSceneTouchEvent.getAction()== TouchEvent.ACTION_DOWN){	    				
	    				SceneManager.getBaseGameActivity().finish();
	    			}
	    			return true;
	    		}
	    	};	    		    	    
        mScene.attachChild(StartText);
        mScene.attachChild(ExitText);
                
        mScene.registerTouchArea(StartText);
        mScene.registerTouchArea(ExitText);
	}

	@Override
	public Scene run() {
		return this.mScene;
	}

	@Override
	public void unloadResources(BaseGameActivity context) {
		// TODO Auto-generated method stub
		mScene.detachChildren();
		mScene.detachSelf();	
//		this.SheetMenuBitmapTextureAtlas.clearTextureAtlasSources();
//		bg_bitmapTextureAtlas.clearTextureAtlasSources();		
		SceneManager.unloadTexture(bg_bitmapTextureAtlas);
		SceneManager.unloadTexture(mFontTexture);
	}
}

