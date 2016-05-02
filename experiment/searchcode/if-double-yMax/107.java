package spade;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Any interactive moving thing in the game, e.g. player, enemy, powerup.
 *
 * @author Patrick
 */
public class Entity {
    double xabs = 0; // absolute position. only used internally.
    double yabs = 0;
    int xpos = 0; // position rounded to the nearest pixel.
    int ypos = 0;
    double xspeed = 0; // used for move()
    double yspeed = 0;
    int width = 16; // used only for collisions (rectangles)
    int height = 16;
    int spriteXo = 0; // offset of which to draw sprite
    int spriteYo = 0;
    BufferedImage sprite; // image to draw
    int spriteXFlip = 1;
    int spriteYFlip = 1;
    Game game; // used to access stuff in our game object
    boolean blockprotected = false; // are we allowed to dig tiles on top of the Entity
    boolean harmful = false;
    boolean meltsTime = false; // is it not affected by the time freeze powerup
    
    public Entity() {}
    
    public Entity(int xpos, int ypos, Game game) {
        this.game = game;
        this.xpos = xpos;
        this.ypos = ypos;
        xabs = xpos;
        yabs = ypos;
    }
    
    public void tick() {}
    
    protected void move() {
        // move "pixel by pixel"
        // nice movement algorithm that allows entities to slide into
        // one-block gaps as long as they have 0.5 speed in that direction.
        double xstep = 0;
        double ystep = 0;
        int x,y,xa,xb;
        int xdir = (int)(xspeed/Math.abs(xspeed));
        int ydir = (int)(yspeed/Math.abs(yspeed));
        double xmax = Math.abs(xspeed);
        double ymax = Math.abs(yspeed);
        for (int n=0;n<Math.abs(xspeed)+Math.abs(yspeed);n++) {
            // move in x direction
            if (xstep < xmax) {
                if (xmax-xstep>=1) {
                    xabs+=xdir;
                }
                else xabs += xdir*(xmax-xstep);
                xpos = (int)xabs;
                xstep++;
                x = (int)(xpos/16);
                y = (int)(ypos/16);
                xa = (int)((xdir+1)/2); // 1 if right, 0 if left.
                // if collision, move backwards.
                if ((  ypos == y*16 && game.level[x+xa][y].isSolid())  ||  (ypos != y*16 && (game.level[x+xa][y].isSolid() || game.level[x+xa][y+1].isSolid()))  ) {
                    xpos = 16*(x+1-xa);
                    xabs = xpos+(xa-0.5);
                    //xspeed = 0; // commented because then you can't fall and walk into horizontal gaps.
                    xstep--;
                }
            }
            // move in y direction
            if (ystep < ymax) {
                if (ymax-ystep>=1) {
                    yabs+=ydir;
                }
                else yabs += ydir*(ymax-ystep);
                ypos = (int)yabs;
                ystep++;
                x = (int)(xpos/16);
                y = (int)(ypos/16);
                int ya = (int)((ydir+1)/2); // 1 if down, 0 if up.
                // if collision, move backwards.
                if (  (xpos == x*16 && game.level[x][y+ya].isSolid())  ||  (xpos != x*16 && (game.level[x][y+ya].isSolid() || game.level[x+1][y+ya].isSolid()))  ) {
                    ypos = 16*(y+1-ya);
                    yabs = ypos+(ya-0.5);
                    yspeed = 0; // hitting head on ceiling or standing on floor.
                    ystep--;
                }
            }
        }
    }
    
    protected boolean isOnscreen() {
        int xmin = -game.Xtrans-width;
        int xmax = -game.Xtrans+game.w;
        return !(xpos<xmin || xpos>xmax);
    }
    
    public int getXpos() {
        return xpos;
    }
    
    public int getYpos() {
        return ypos;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Rectangle getRect() {
        return new Rectangle(xpos,ypos,width,height);
    }
    
    public int getXoffset() {
        return spriteXo;
    }
    
    public int getYoffset() {
        return spriteYo;
    }
    
    public BufferedImage getSprite() {
        return sprite;
    }
    
    public int spriteXFlip() {
        return spriteXFlip;
    }
    
    public int spriteYFlip() {
        return spriteYFlip;
    }
    
    public boolean isBlockProtected() {
        return blockprotected;
    }
    
    public boolean isHarmful() {
        return harmful;
    }
    
    public void remove() {
        game.entities.remove(this);
    }
        
}

