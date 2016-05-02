package spade;

/** Enemy that paces back and forth.
 * @author Patrick
 */
public class PaceEnemy extends Entity {
    
    double walkSpeed = 0.4;
    double yaccel = 0.15;
    
    int walkcount = 0;
    
    public PaceEnemy(int xpos, int ypos, Game game) {
        super(xpos,ypos,game);
        blockprotected = true;
        harmful = true;
        sprite = Art.paceenemy.get("idle");
        xspeed = walkSpeed;
    }
    
    public void tick() {
        if (yspeed == 0) yspeed = 0.5;
        else yspeed += yaccel;
        int oldx = (int)(xpos/8); // I think we're going to assume you're going slower than 8 px/tick :P
        move();
        
        int x = (int)(xpos/16);
        int y = (int)(ypos/16);
        int xdir = (int)(xspeed/Math.abs(xspeed));
        int xa = (int)((xdir+1)/2); // 1 if right, 0 if left.
        int xb; // 0 if right, 1 if left.
        if (xa==1) xb = 0;
        else xb = 1;
        
        // if hits wall (alligned with x grid and solid in front of it) - turn around
        if (xpos == x*16 && game.level[x+2*xa-1][y].isSolid()) {
            xspeed *= -1;
            spriteXFlip*=-1;
        }
        // we have changed 8-px chunk
        else if (oldx!=(int)(xpos/8)) {
            // if we are now over halfway off a ledge. below old was solid, and now it's not.
            if (!game.level[(xpos+8*xb-8*xa)/16+xa][y+1].isSolid() && game.level[x+xb][y+1].isSolid()) {
                //xpos = (oldx+xa)*8;
                xspeed*=-1;
                spriteXFlip*=-1;
            }
        }
        
        walkcount++;
        if (walkcount==1) sprite = Art.paceenemy.get("walk1");
        else if (walkcount==10) sprite = Art.paceenemy.get("walk2");
        else if (walkcount==20) walkcount=0;
        
    }
    
}

