/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.kirauks.pixelrunner.scene;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kirauks.pixelrunner.GameActivity;
import net.kirauks.pixelrunner.R;
import net.kirauks.pixelrunner.game.Player;
import net.kirauks.pixelrunner.manager.ResourcesManager;
import net.kirauks.pixelrunner.manager.SceneManager;
import net.kirauks.pixelrunner.manager.db.SuccessDatabase;
import net.kirauks.pixelrunner.manager.db.SuccessDatabase.Success;
import net.kirauks.pixelrunner.scene.base.BaseListMenuScene;
import net.kirauks.pixelrunner.scene.base.element.ListElement;
import net.kirauks.pixelrunner.scene.base.element.XmAudioListElement;
import net.kirauks.pixelrunner.scene.base.utils.comparator.AlphanumComparator;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.adt.color.Color;

/**
 *
 * @author Karl
 */
public class BonusJukeboxScene extends BaseListMenuScene{
    private final AnimatedSprite playerDance;
    private final AnimatedSprite nyan;
    private Text nowPlaying;
    private Text playing;
    private Sprite top;
    private Sprite bottom;
    private final Rectangle listBackground;
    private final Rectangle listBordersLeft;
    private final Rectangle listBordersRight;
    
    public BonusJukeboxScene(){
        super();
        try {
            List<String> fileNames = Arrays.asList(this.activity.getAssets().list("mfx"));
            Collections.sort(fileNames, new AlphanumComparator());
            for(final String name : fileNames){    
                ListElement file = new XmAudioListElement(name.substring(0, name.length() - 3), name, this.vbom);
                this.addListElement(file);
            }
        } catch (IOException ex) {
            Logger.getLogger(BonusChoiceScene.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.getListWrapper().setX(390);
        
        this.listBackground = new Rectangle(550, GameActivity.CAMERA_HEIGHT/2, 350, GameActivity.CAMERA_HEIGHT, this.vbom);
        this.listBackground.setZIndex(this.getListWrapper().getZIndex() - 1);
        this.listBackground.setColor(Color.BLACK);
        this.listBackground.setAlpha(0.6f);
        this.attachChild(this.listBackground);
        this.listBordersLeft = new Rectangle(373, GameActivity.CAMERA_HEIGHT/2, 4, GameActivity.CAMERA_HEIGHT, this.vbom);
        this.listBordersLeft.setZIndex(this.listBackground.getZIndex() - 1);
        this.listBordersLeft.setColor(new Color(0.4f, 0.4f, 0.4f));
        this.attachChild(this.listBordersLeft);
        this.listBordersRight = new Rectangle(727, GameActivity.CAMERA_HEIGHT/2, 4, GameActivity.CAMERA_HEIGHT, this.vbom);
        this.listBordersRight.setZIndex(this.listBackground.getZIndex() - 1);
        this.listBordersRight.setColor(new Color(0.4f, 0.4f, 0.4f));
        this.attachChild(this.listBordersRight);
        
        this.sortChildren();
        
        this.playerDance = new AnimatedSprite(190, 280, this.resourcesManager.player, this.vbom);
        this.playerDance.animate(Player.PLAYER_ANIMATE_DANCE, Player.PLAYER_ANIMATE_DANCE_FRAMES);
        this.playerDance.setCullingEnabled(true);
        this.playerDance.setScale(4);
        this.attachChild(this.playerDance);
        
        this.nowPlaying = new Text(190, 110, this.resourcesManager.fontPixel_34, this.activity.getString(R.string.jukebox_playing), this.vbom);
        this.nowPlaying.setVisible(false);
        this.attachChild(this.nowPlaying);
        this.playing = new Text(190, 80, this.resourcesManager.fontPixel_60, "0123456789", this.vbom);
        this.playing.setVisible(false);
        this.attachChild(this.playing);
        
        this.top = new Sprite(765, 450, ResourcesManager.getInstance().lvlLeft, this.vbom){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y){
                if (pSceneTouchEvent.isActionUp() && this.isVisible()){
                    BonusJukeboxScene.this.activity.vibrate(30);
                    BonusJukeboxScene.this.impulseUp();
                }
                return false;
            };
        };
        this.top.setScale(6f);
        this.top.setRotation(90f);
        this.registerTouchArea(this.top);
        this.attachChild(this.top);
        this.bottom = new Sprite(765, 30, ResourcesManager.getInstance().lvlRight, this.vbom){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y){
                if (pSceneTouchEvent.isActionUp() && this.isVisible()){
                    BonusJukeboxScene.this.activity.vibrate(30);
                    BonusJukeboxScene.this.impulseDown();
                }
                return false;
            };
        };
        this.bottom.setScale(6f);
        this.bottom.setRotation(90f);
        this.registerTouchArea(this.bottom);
        this.attachChild(this.bottom);
        this.onListMove(0, 0, 1);
        
        //Nyan ester egg
        this.nyan = new AnimatedSprite(190, 280, this.resourcesManager.nyan, this.vbom);
        this.nyan.animate(new long[]{80, 80, 80, 80, 80}, new int[]{0, 1, 2, 3, 4});
        this.nyan.setCullingEnabled(true);
        this.nyan.setScale(4);
        this.nyan.setVisible(false);
        this.attachChild(this.nyan);
    }
    
    private void enableNyan(){
        this.nyan.setVisible(true);
        this.playerDance.setVisible(false);
    }
    private void disableNyan(){
        this.nyan.setVisible(false);
        this.playerDance.setVisible(true);
    }

    @Override
    protected void onListMove(float newPos, float minPos, float maxPos) {
        if(newPos >= maxPos){
            this.bottom.setVisible(false);
        }
        else{
            this.bottom.setVisible(true);
        }
        if(newPos <= minPos){
            this.top.setVisible(false);
        }
        else{
            this.top.setVisible(true);
        }
    }

    @Override
    public void onPause() {
        /* Override auto audio pause to continue playback on phone lock */
    }

    @Override
    public void onResume() {
        /* Override auto audio pause to continue playback on phone lock */
    }
    
    @Override
    public void onBackKeyPressed() {
        this.audioManager.stop();
        this.audioManager.play("mfx/", "menu.xm");
        SceneManager.getInstance().createBonusChoiceScene();
        SceneManager.getInstance().disposeBonusJukeboxScene();
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_BONUS_JUKEBOX;
    }

    @Override
    public void onElementAction(ListElement element) {
        this.activity.vibrate(30);
        this.audioManager.stop();
        this.nowPlaying.setVisible(true);
        this.playing.setVisible(true);
        this.playing.setText(element.getName());
        if(element.getName().equals("nyan")){
            this.enableNyan();
            new SuccessDatabase(this.activity).unlockSuccess(Success.NYAN);
        }
        else{
            this.disableNyan();
        }
        this.audioManager.play("mfx/", ((XmAudioListElement)element).getXmFileName());
            new SuccessDatabase(this.activity).unlockSuccess(Success.JUKEBOX);
    }
    
    @Override
    public void disposeScene() {
        super.disposeScene();
        this.playerDance.detachSelf();
        this.playerDance.dispose();
        this.nyan.detachSelf();
        this.nyan.dispose();
        this.nowPlaying.detachSelf();
        this.nowPlaying.dispose();
        this.playing.detachSelf();
        this.playing.dispose();
        this.listBackground.detachSelf();
        this.listBackground.dispose();
        this.listBordersLeft.detachSelf();
        this.listBordersLeft.dispose();
        this.listBordersRight.detachSelf();
        this.listBordersRight.dispose();
        this.top.detachSelf();
        this.top.dispose();
        this.bottom.detachSelf();
        this.bottom.dispose();
        this.detachSelf();
        this.dispose();
    }
}
