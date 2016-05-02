package org.ita23.pacman.figures;

import org.ita23.pacman.Main;
import org.ita23.pacman.game.*;
import org.ita23.pacman.logic.ChunkedMap;
import org.ita23.pacman.logic.ChunkedMap.Chunk;
import org.ita23.pacman.logic.GameState;
import org.ita23.pacman.logic.Point;
import org.ita23.pacman.logic.StateListener;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The main-character of this game.
 * @author Lukas Knuth
 * @author Fabain Bottler
 * @version 1.0
 */
public class Pacman implements RenderEvent, InputEvent, CollusionEvent, MovementEvent, StateListener {

    /** The count of degrees needed to consider the moth "fully opened" */
    private final static int MOUTH_MAX = 75;
    /** The count of degrees needed to consider the moth "fully closed" */
    private final static int MOUTH_MIN = 0;
    /** The pixels-per-repaint that pacman moves */
    private final static int MOVE_PER_PAINT = 2; // TODO Slower when not eating!
    /** The speed indicating how fast the mouth moves. The higher, the faster! */
    private final static int MOUTH_SPEED = 10;
    /** The diameter of pacman's body, e.g. his hitbox */
    public final static int HITBOX = 28;
    
    private final static int ZINDEX = 1;
    
    /** The color of pacmans body */
    public static final Color BODY_COLOR = new Color(255, 255, 87);

    /** The current X-coordinate */
    private int x;
    /** The current Y-coordinate */
    private int y;
    /** The start-point for pacman (used when resetting) */
    private final Point start_point;

    /** Counts the amount of pixels moved since the last direction-change */
    private int pixel_moved_count;
    /** Whether if Pacamn has collided with a block and therefore can't move */
    private boolean has_collided;
    /** This will be {@code true}, if a live was just lost and pacman is dieing */
    private boolean isDieing;

    /** The current degrees of the mouth */
    private int mouth_degrees;
    /** Specifies if the mouth is closing or opening */
    private boolean mouth_closing;

    /** Possible directions that pacman can look */
    private enum FacingDirection{
        UP(0), DOWN(180), LEFT(90), RIGHT(270);
        
        private final int degrees;
        private FacingDirection(int degrees){
            this.degrees = degrees;
        }

        /**
         * Checks which {@code NextDirection} matches this current
         *  {@code FacingDirection} and returns it.
         * @return the matching {@code CollusionTest.NextDirection} for this
         *  {@code FacingDirection}.
         */
        private CollusionTest.NextDirection convertToNextDirection(){
            switch (this){
                case DOWN:
                    return CollusionTest.NextDirection.DOWN;
                case UP:
                    return CollusionTest.NextDirection.UP;
                case LEFT:
                    return CollusionTest.NextDirection.LEFT;
                case RIGHT:
                    return CollusionTest.NextDirection.RIGHT;
                default:
                    throw new IllegalStateException("Can't be '"+this.toString()+"'");
            }
        }
    }
    /** The current direction pacman looks */
    private FacingDirection current_direction;
    /** The direction pacman should move next possible turn */
    private FacingDirection next_direction;
    /** Weather a direction-change is possible without running into a wall */
    private boolean direction_change_possible;

    /**
     * Create a new Pacman-figure with an animated mouth.
     */
    public Pacman(Point point){
        this.start_point = point;
        reset();
        // Register self to game-state listener:
        GameState.INSTANCE.addStateListener(this);
    }
    
    public int getZIndex(){
        return ZINDEX;
    }

    @Override
    public void move() {
        // Check if direction-change is allowed:
        if (pixel_moved_count % ChunkedMap.Chunk.CHUNK_SIZE == 0){
            // Change direction if possible:
            if (direction_change_possible)
                current_direction = next_direction;
            pixel_moved_count = 0;
        }
        // Move the character:
        if (!has_collided)
            switch (current_direction){
                case UP:
                    this.y -= MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
                case RIGHT:
                    this.x += MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
                case DOWN:
                    this.y += MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
                case LEFT:
                    this.x -= MOVE_PER_PAINT;
                    pixel_moved_count += MOVE_PER_PAINT;
                    break;
            }
        // Animate the mouth:
        if (mouth_degrees < MOUTH_MAX && !mouth_closing){
            // Mouth is opening.
            if (!has_collided) // When standing, don't eat!
                mouth_degrees += MOUTH_SPEED;
        } else if (mouth_degrees > MOUTH_MIN) {
            if (!has_collided){ // When standing, don't eat!
                mouth_degrees -= MOUTH_SPEED;
                mouth_closing = true;
            }
        } else {
            // Mouth is closed. Open it again!
            mouth_closing = false;
        }
    }
    
    @Override
    public void render(Graphics g) throws IOException {
        // Draw the "ball"
    	URL url = Main.class.getResource("res/graphics/"+current_direction.toString()+".png");
        Image image = new ImageIcon(url).getImage();
    	g.drawImage(image, this.x-3, this.y-3, ChunkedMap.BACKGROUND_COLOR, null);
        // Draw the mouth:
        g.setColor(ChunkedMap.BACKGROUND_COLOR);
        // The mouth:
        g.fillArc(this.x-3, this.y-3, HITBOX, HITBOX,
                calculateMouthSpacer(mouth_degrees)+current_direction.degrees,
                mouth_degrees
        );
        // Death-animation:
        if (isDieing){
            // TODO Add the "splash" at the end of the animation!
            mouth_degrees += MOUTH_SPEED+2;
            // Check if we're at the end of the animation:
            if (mouth_degrees >= 360){
                // Reset pacman:
                reset();
            }
        }
    }

    /**
     * Calculates the space needed to "center" the mouth on the
     *  body during the "open-close" animation.
     * @param current_degrees the number of degrees the mouth is
     *  currently opened.
     * @return the calculated space used to center the mouth.
     * TODO Add pictures or something for better explanation.
     */
    public int calculateMouthSpacer(int current_degrees){
        int element_space = current_degrees + 180;
        int usable_space = 360 - element_space;
        return (usable_space / 2);
    }
    
    @Override
    public void detectCollusion(CollusionTest tester) {
        if (pixel_moved_count % ChunkedMap.Chunk.CHUNK_SIZE != 0) return;
        // Check if we went into the "jumper":
        if (tester.checkCollusion(this.x, this.y, ChunkedMap.Chunk.JUMPER)){
            if (this.x <= Chunk.CHUNK_SIZE-3){ // Went into the left jumper, so go to the right:
                this.x = Chunk.CHUNK_SIZE * 27;
            } else {
                this.x = Chunk.CHUNK_SIZE;
            }
            return;
        }
        // Check if we ran against a block (and therefore can't move):
        if (tester.checkNextCollusion(this.x, this.y,
                Chunk.BLOCK, current_direction.convertToNextDirection())
            || tester.checkNextCollusion(this.x, this.y,
                Chunk.CAGE_DOOR, current_direction.convertToNextDirection())){
            has_collided = true;
        }
        if (tester.checkNextCollusion(this.x, this.y,
                Chunk.BLOCK, next_direction.convertToNextDirection())
            || tester.checkNextCollusion(this.x, this.y,
                Chunk.CAGE_DOOR, next_direction.convertToNextDirection())){
            direction_change_possible = false;
        } else {
            direction_change_possible = true;
            has_collided = false;
        }
        // Check if we ate something:
        if (tester.checkCollusion(this.x, this.y, Chunk.POINT)){
            SoundManager.INSTANCE.loop("eat", Clip.LOOP_CONTINUOUSLY);
            GameState.INSTANCE.addScore(GameState.Food.POINT);
        } else if (tester.checkCollusion(this.x, this.y, Chunk.BALL)){
            GameState.INSTANCE.addScore(GameState.Food.BALL);
        } else if (tester.checkCollusion(this.x, this.y, Chunk.FRUIT)){
            GameState.INSTANCE.addScore(GameState.Food.BONUS);
            SoundManager.INSTANCE.play("eat_fruit");
        } else {
            SoundManager.INSTANCE.stop("eat");
        }
    }

    @Override
    public void stateChanged(States state) {
        // Stop the eating sound!
        SoundManager.INSTANCE.stop("eat");
        // Handle the specific cases:
        if (state == States.LIVE_LOST){
            // Kick off the death-animation:
            isDieing = true;
            mouth_degrees = 0;
        } else if (state == States.ROUND_WON || state == States.GAME_OVER){
            reset();
            // TODO Wait for the melody to finish...
        }
    }

    @Override
    public void keyboardInput(KeyEvent event, KeyEventType type) {
        has_collided = false;
        if (event.getKeyCode() == KeyEvent.VK_UP)
            next_direction = FacingDirection.UP;
        else if (event.getKeyCode() == KeyEvent.VK_DOWN)
            next_direction = FacingDirection.DOWN;
        else if (event.getKeyCode() == KeyEvent.VK_LEFT)
            next_direction = FacingDirection.LEFT;
        else if (event.getKeyCode() == KeyEvent.VK_RIGHT)
            next_direction = FacingDirection.RIGHT;
    }

    /**
     * This will reset pacman to hist start-position, set his default
     *  direction and mouth opening, etc.
     */
    private void reset(){
        mouth_degrees = 45;
        mouth_closing = false;
        current_direction = FacingDirection.LEFT;
        next_direction = current_direction;
        direction_change_possible = true;
        has_collided = false;
        this.x = start_point.getX();
        this.y = start_point.getY();
        isDieing = false;
        pixel_moved_count = 0;
    }

    /**
     * Get the current X-position of this {@code Pacman}-instance.
     * @return the current X-position.
     */
    int getX(){
        return this.x;
    }

    /**
     * Get the current Y-position of this {@code Pacman}-instance.
     * @return the current Y-position.
     */
    int getY(){
        return this.y;
    }

    /**
     * The direction Pacman is currently facing.
     * @return Pacman's current facing-direction.
     */
    CollusionTest.NextDirection getCurrentDirection(){
        return this.current_direction.convertToNextDirection();
    }
//    public static void main(String[] args) {
//    	String current_direction = FacingDirection.LEFT.toString();
//    	System.out.println(current_direction);
//    }
}

