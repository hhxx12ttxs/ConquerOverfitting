package spade;
import java.awt.image.BufferedImage;

/** Entity that plays an animation of several images then possibly destroys itself.
 * 
 * @author Patrick
 */
public class AnimEntity extends Entity {
    
    public enum Type {BLOODSPLAT, FLASH, MENUSELECTOR, PLAYERENTER, CHECKPOINTOPEN, EXPLODE2 }
    
    int framespeed;
    int tickcount = 0;
    boolean loop = false;
    BufferedImage[] frames;
    
    public AnimEntity(int xint, int yint, Type type, Game game) {
        if (type == Type.BLOODSPLAT) {
            frames = Art.bloodsplat;
            framespeed = 7;
        }
        else if (type == Type.FLASH) {
            frames = Art.flash;
            framespeed = 3;
        }
        else if (type==Type.MENUSELECTOR) {
            frames = Art.menuselector;
            framespeed = 8;
            loop = true;
        }
        else if (type==Type.PLAYERENTER) {
            frames = Art.playerenter;
            framespeed = 10;
        }
        else if (type==Type.CHECKPOINTOPEN) {
            frames = Art.checkpointopen;
            framespeed = 8;
        }
        else if (type==Type.EXPLODE2) {
            frames = Art.explode2;
            framespeed = 5;
        }
        this.xpos = xint;
        this.ypos = yint;
        sprite = frames[0];
        this.game = game;
        meltsTime = true;
    }
    
    public AnimEntity() {}
    
    public void tick() {
        tickcount++;
        if (tickcount == framespeed*frames.length) {
            if (!loop) game.entities.remove(this);
            else tickcount = 0;
        }
        else if (tickcount/framespeed == (int)(tickcount/framespeed)) sprite = frames[tickcount/framespeed];
    }
}

