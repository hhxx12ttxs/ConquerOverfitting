package gamelib.command;

import gamelib.canvas.GameCanvas;
import gamelib.direction.HorizontalDirection;
import gamelib.direction.VerticalDirection;
import gamelib.level.TileMap;
import gamelib.shape.GameShape;

import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

/**
 *
 * @author Angus
 */
public class TopDownCommander extends Commander{

    /*
     * hash maps used for parsing character input, to directional enum data.
     */
    public static final LinkedHashMap<Character, VerticalDirection> VERTDIR_CHAR_LINKED_HASH_MAP
            = new LinkedHashMap<>();
    public static final LinkedHashMap<Character, HorizontalDirection> HORIDIR_CHAR_LINKED_HASH_MAP
            = new LinkedHashMap<>();

    static {
        VERTDIR_CHAR_LINKED_HASH_MAP.put('w', VerticalDirection.UP);
        VERTDIR_CHAR_LINKED_HASH_MAP.put('s', VerticalDirection.DOWN);
        VERTDIR_CHAR_LINKED_HASH_MAP.put(' ', VerticalDirection.NONE);
        HORIDIR_CHAR_LINKED_HASH_MAP.put('a', HorizontalDirection.LEFT);
        HORIDIR_CHAR_LINKED_HASH_MAP.put('d', HorizontalDirection.RIGHT);
        HORIDIR_CHAR_LINKED_HASH_MAP.put(' ', HorizontalDirection.NONE);
    }

    public double previousDirection = 0;
    public double currentDirection = 0;

    public GameCanvas canvas;

    public TopDownCommander(GameCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * throws exception because this method is not used.
     *
     * @throws CommandException
     */
    @Override @Deprecated
    public void command() throws CommandException {
        throw new CommandException(" Parameters required for command ");
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              COMMANDER METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * parses input to respectable directions then calls the command method that takes directions as parameters
     */
    public void command(GameShape actor, char[] keysPressed, boolean displace){
        command(
                actor,
                VERTDIR_CHAR_LINKED_HASH_MAP.get(keysPressed[0]),
                HORIDIR_CHAR_LINKED_HASH_MAP.get(keysPressed[1]),
                displace
        );
    }

    /**
     * takes the
     *
     * @param actor
     * @param vertDir
     * @param horiDir
     * @param displace
     */
    public void command(GameShape actor, VerticalDirection vertDir, HorizontalDirection horiDir, boolean displace) {
        /*
            align the direction that the actor is facing based on existing projectory and controls
         */
        rotation(actor, vertDir, horiDir);

        /*
            adjust the speed to the appropriate speed based on controls and existing speed
         */
        acceleration(actor, vertDir, horiDir);

        /*
            displace if the displace variable is true
         */
        if (displace){
            displace(actor);
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              DIRECTIONAL METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * align the direction that the actor is facing based on existing projectory and controls
     *
     * @param actor
     * @param vertDir
     * @param horiDir
     */
    public void rotation(GameShape actor, VerticalDirection vertDir, HorizontalDirection horiDir){
        switch (vertDir) {
            case UP:
                switch (horiDir) {
                    //UP
                    case NONE:
                        turnTowards(actor, 180);
                        break;
                    //UP & LEFT
                    case LEFT:
                        turnTowards(actor, 225);
                        break;
                    //UP & RIGHT
                    case RIGHT:
                        turnTowards(actor, 135);
                        break;
                }
                break;
            case DOWN:
                switch (horiDir) {
                    //DOWN
                    case NONE:
                        turnTowards(actor, 0);
                        break;
                    //DOWN & LEFT
                    case LEFT:
                        turnTowards(actor, 315);
                        break;
                    //DOWN & RIGHT
                    case RIGHT:
                        turnTowards(actor, 45);
                        break;
                }
                break;
            case NONE:
                switch (horiDir) {
                    //NOTHING
                    case NONE:
                        break;
                    //LEFT
                    case LEFT:
                        turnTowards(actor, 270);
                        break;
                    //RIGHT
                    case RIGHT:
                        turnTowards(actor, 90);
                        break;
                }
                break;
        }

        /*
         * desired direction is recorded by taking last current direction, then current direction is initalized.
         */
        previousDirection = currentDirection;
        currentDirection = actor.getProjectory();
    }

    /**
     * adjusts the directions in which the shape is facing towards the desired direction
     *
     * @param DESIRED_DIRECTION
     */
    protected void turnTowards(GameShape actor, final int DESIRED_DIRECTION){
        /*
         * if already facing desired direction just return as no turning is required
         */
        if (actor.getProjectory() != DESIRED_DIRECTION) {
            /*
             * if velocity is already equal to 0, and it's not moving, just move player in desired directions
             */
            if (actor.getVelocity() == 0) {
                actor.setProjectory(DESIRED_DIRECTION);
            }
            /*
             * if player is heading in the same direction it was previously check if it is it's DESIRED_DIRECTION then
             * set the set velocity to zero and face it in the other direction
             */
            else if (actor.getProjectory() == previousDirection
                    && actor.getProjectory() == oppositeDirection(DESIRED_DIRECTION)) {
                actor.setVelocity(0);
                actor.setProjectory(DESIRED_DIRECTION);
            }
            /*
             * since the control method only checks if the direction player is heading in a direction within 0 & 360,
             * weird stuff happens when you want to turn in certain directions, from certain directions. This if
             * statement is ment to counter act that.
             *
             * This is done by checking if the projectory of player isn't equal to 180 degrees, and if it's greater than
             * 180 degrees.
             *
             * If it is, then check if DESIRED_DIRECTION greater than
             */
            else if (!(actor.getProjectory() == 180) && (actor.getProjectory() > 180)
                    ? DESIRED_DIRECTION < oppositeDirection(actor.getProjectory())
                    : DESIRED_DIRECTION > oppositeDirection(actor.getProjectory())) {
                if (actor.getProjectory() > 360.00){
                    actor.setProjectory(0);
                }
                else if (actor.getProjectory() <= 0.00){
                    /*
                     * since 360 & 0 are practically on the same location of a circle, and since this statement is
                     * treating as if the shape is turning
                     */
                    actor.setProjectory(355);
                }
                else if (actor.getProjectory() > 180) {
                    actor.turn(true);
                }
                else if (actor.getProjectory() < 180) {
                    actor.turn(false);
                }
                /*
                 * if player projectory is facing 180
                 */
                else {
                    if (actor.getProjectory() < DESIRED_DIRECTION) {
                        actor.turn(true);
                    }
                    else if (actor.getProjectory() > DESIRED_DIRECTION){
                        actor.turn(false);
                    }
                }
            }
            else if (actor.getProjectory() > DESIRED_DIRECTION) {
                actor.turn(false);
            }
            else if (actor.getProjectory() < DESIRED_DIRECTION) {
                actor.turn(true);
            }
        }
    }

    /**
     * returns the opposite direction of the parameter @param direction
     */
    protected double oppositeDirection(double direction){
        return (direction + 180) % 360;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                        DISPLACEMENT & ACCELERATION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Will displace the actor if is not going to collide with any of the level layers blocks that. It also skips any
     * blocks that are out of vertical range.
     *
     * @param actor
     */
    public void displace(GameShape actor){
        /*
            displaces conditions are met if there is a loaded level check if that
         */
        if (actor.getVelocity() > 0){
            /*
                if the current level is a loaded level
             */
            if (canvas.getCurrentLevel() != null){

                boolean foundCollision = false;

                /*
                    loop through the current level's layer-hashmap's keyset
                 */
                for (String layerKey : canvas.getCurrentLevel().getLayers().keySet()){

                    /*
                        retrieve current layer's TileMap
                     */
                    TileMap tileMap = canvas.getCurrentLevel().getLayers().get(layerKey).getTileMap();

                    /*
                        if tileMap has hits, and no collision has already been found
                     */
                    if (tileMap.hasHits() && !foundCollision){

                        for (int verticalValue : tileMap.getTileLocations().keySet()){
                            int tileYValue = verticalValue * tileMap.getTileHeight();
                            int viewPortYMin = (int) ((actor.getYPoint()) - tileMap.getTileHeight());
                            int viewPortYMax = (int) (actor.getYPoint() + actor.getHeight());
                            /*
                                if tile is one tile height within the vertical range of the viewPort
                             */
                            if (tileYValue + tileMap.getY() >= viewPortYMin && tileYValue <= viewPortYMax){
                                /*
                                    go through the tileLocations horizontally
                                 */

                                //System.out.print("\n"+ verticalValue + "   ");

                                for (int horizontalValue : tileMap.getTileLocations().get(verticalValue).keySet()){
                                    int tileXValue = horizontalValue * tileMap.getTileWidth();
                                    int viewPortXMin = (int) (actor.getXPoint() - tileMap.getTileWidth());
                                    int viewPortXMax = (int) (actor.getXPoint() + actor.getWidth());
                                    /*
                                        if tile is a tiles width within the horizontal range of the viewPort
                                        it will be drawn
                                     */
                                    if (tileXValue + tileMap.getX() >= viewPortXMin && tileXValue <= viewPortXMax){
                                        /*
                                            tile num
                                         */
                                        int tileNum = tileMap
                                                .getTileLocations()
                                                .get(verticalValue)
                                                .get(horizontalValue);

                                        /*
                                            Checks if the tileNum isn't 0. 0 are empty tiles, and do not collide.
                                         */
                                        if (tileNum != 0){

                                            /*
                                                creates a rectangle that represents the properties of the tile
                                             */

                                            //TODO find out why the tile location behaviour seems to be sporadic

                                            Rectangle2D.Double tile = new Rectangle2D.Double(
                                                    tileXValue - viewPortXMin - tileMap.getTileWidth(),
                                                    tileYValue - viewPortYMin - tileMap.getTileHeight(),
                                                    tileMap.getTileWidth(),
                                                    tileMap.getTileHeight()
                                            );

                                            if (actor.getHitBox().intersects(tile)){
                                                System.out.println("COLLISION_DETECTED");
                                                System.out.println(
                                                        "ACTOR - " + actor.getXPoint() +
                                                                " " + actor.getYPoint());
                                                System.out.println(
                                                        "TILE  - " + tile.x +
                                                                " " + tile.y + "\n");
                                                //foundCollision = true;
                                            }
                                        }

                                    } // horizontal range check

                                } // end of Horizontal loop

                            } // vertical range check
                        }

                    } // checking if collision has been made, and has hits

                } // layer loop

                /*
                    displaces as it would normally if no collision was found
                 */
                if (!foundCollision){
                    actor.displace();
                }

            } else {
                /*
                    default action
                 */
                actor.displace();
            }
        } // if velocity
    } // end of displacement function

    /**
     * accelerates the player up to the velocityCap while keys are being pressed if keys are not being pressed, the
     * shape is deaccelerated while it's velocity is greater than 0.
     *
     * @param actor
     * @param vertDir
     * @param horiDir
     */
    public void acceleration(GameShape actor, VerticalDirection vertDir, HorizontalDirection horiDir){
        /*
         * accelerates the player up to the velocity while keys are being pressed if keys are not being pressed, the
         * shape is deaccelerated while it's velocity is greater than 0.
         */
        if (!(vertDir == VerticalDirection.NONE && horiDir == HorizontalDirection.NONE)
                && actor.getVelocity() < actor.getVelocityCap()) {
            actor.accelerate();
        } else if (vertDir == VerticalDirection.NONE && horiDir == HorizontalDirection.NONE
                && actor.getVelocity() > 0) {
            /*
                if ball below particular speed set to 0 to prevent any sliding when there are no keys being held down
             */
            if (actor.getVelocity() < 0.1) {
                actor.setVelocity(0);
            } else if (actor.getVelocity() > 0) {
                actor.accelerate(-(actor.getAcceleration() * 1.2));
            }
        }
    }




} // end of top down commander

