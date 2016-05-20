package org.app.visitor;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import org.app.map.Sprite;
import org.app.map.SpriteStore;
import org.app.map.ZooMap;
import org.app.map.zooitem.CrossroadTile;
import org.app.map.zooitem.ZooTile;

/**
 *
 * @author dell
 */
public abstract class Visitor {

    private int px;
    private int py;
    private int number;
    private double money;
    private int step;
    //direction constants
    //      0
    //1           2
    //      3
    private static final int N = 0;
    private static final int E = 2;
    private static final int S = 3;
    private static final int W = 1;
    private static final int NUM_DIRS = 4;
    private static final int NUM_VISITOR_TYPE = 6;
    private int bearing;
    int prevX = -1, prevY = -1;
    private boolean atXRoad = false;
    private Point[] offset;
    private int currBearing;
    private ZooMap map;
    private Sprite sprite;
    private ArrayList<String> spriteStr = new ArrayList<>();
    private Random random;
    private String spriteString;

    public Visitor(int x, int y, ZooMap map, int number, double money) {
        px = x;
        py = y;
        this.number = number;
        this.money = money;
        offset = new Point[NUM_DIRS];
        offset[N] = new Point(0, -1);
        offset[E] = new Point(1, 0);
        offset[S] = new Point(0, 1);
        offset[W] = new Point(-1, 0);
        random = new Random();
        this.map = map;
        currBearing = E;
        step = 1;
        initSpriteString();
        setSpriteString();
        setDirection(E);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    
    
    public void setSprite(String ref) {
        sprite = SpriteStore.get().getSprite(ref);
    }
    
    private void initSpriteString(){
        spriteStr.add("sprites/visitor/man");
        spriteStr.add("sprites/visitor/girl");
        spriteStr.add("sprites/visitor/boy");
        spriteStr.add("sprites/visitor/woman");
        spriteStr.add("sprites/visitor/old");
        spriteStr.add("sprites/visitor/muf");
    }
    
    private void setSpriteString(){
        int type = random.nextInt(NUM_VISITOR_TYPE-1);
        spriteString = spriteStr.get(type);
        sprite = SpriteStore.get().getSprite(spriteStr.get(type)+"_right.png");
    }

    /**
     * move the visitor
     */
    public void move() {
        px += step * offset[currBearing].x;
        py += step * offset[currBearing].y;

        //step into a new tile
        //check for crossroad and change direction
        //check with previous tile location to make sure we're in a new tile
        if (((px % ZooTile.TILE_SIZE == 0) && (px - (px % ZooTile.TILE_SIZE)) != prevX) || ((py % ZooTile.TILE_SIZE == 0) && (py - (py % ZooTile.TILE_SIZE)) != prevY)) {
            ZooTile tile = map.getTileAt((px - (px % ZooTile.TILE_SIZE)) / ZooTile.TILE_SIZE, (py - (py % ZooTile.TILE_SIZE)) / ZooTile.TILE_SIZE);
            //System.out.println(px);
            //System.out.println(py - (py % ZooTile.TILE_SIZE));
            //System.out.println(tile);
            prevX = px - (px % ZooTile.TILE_SIZE);
            prevY = py - (py % ZooTile.TILE_SIZE);
            //is the x-axis at the middle of the horizontal path (which is the middle of the map)
            boolean atMidHMap = (px / ZooTile.TILE_SIZE) == (ZooMap.MAP_WIDTH / 2);
            //is the y-axis at the middle of each vertical path
            boolean atMidVRoad = ((py / ZooTile.TILE_SIZE) == (ZooMap.MAP_HEIGHT - 3) || (py / ZooTile.TILE_SIZE) == 2);
            //is moving vertically
            boolean isVerticalMove = (currBearing == N || currBearing == S);
            //is moving horizontal
            boolean isHorizontalMove = (currBearing == W || currBearing == E);
            //check if the new tile is at a crossroad
            //then proceed until we reach the middle tile of the crossroad (vertical or horizontal)
            if (tile instanceof CrossroadTile) {
                if (tile instanceof CrossroadTile && ((atMidHMap && isHorizontalMove) || (atMidVRoad && isVerticalMove))) {

                    if (!atXRoad) {
                        bearing();
                        setDirection(bearing);
                    }
                    atXRoad = true;
                }
            } else {
                atXRoad = false;
            }
        }
            //check for the boundary of the map
            //
            boolean isRightBound = px == ((ZooMap.MAP_WIDTH - 2) * ZooTile.TILE_SIZE);
            boolean isLeftBound = px == ZooTile.TILE_SIZE;
            boolean isTopBound = (py == ZooTile.TILE_SIZE);
            boolean isBottomBound = (py == ((ZooMap.MAP_HEIGHT - 2) * ZooTile.TILE_SIZE));
            if (isRightBound || isLeftBound || isTopBound || isBottomBound) {
                setDirection(oppositeDirection());
            }
        
    }

    /**
     * calculate a random direction based on the current direction
     */
    public void bearing() {
        do {
            bearing = random.nextInt(NUM_DIRS);
        } while (bearing == oppositeDirection());

    }

    /**
     *
     * @param bearing
     */
    private void setDirection(int bearing) {
        switch (bearing) {
            case W:
                setSprite(spriteString+"_left.png");
                currBearing = W;
                break;
            case E:
                setSprite(spriteString+"_right.png");
                currBearing = E;
                break;
            case N:
                setSprite(spriteString+"_up.png");
                currBearing = N;
                break;
            case S:
                setSprite(spriteString+"_down.png");
                currBearing = S;
                break;
        }
    }

    /**
     * base on the current direction calculate the opposite direction
     */
    public int oppositeDirection() {
        return Math.abs(currBearing - (NUM_DIRS - 1));
    }

    public void draw(Graphics g) {
        sprite.draw(g, px, py);
    }
    
    public abstract boolean spendMoney();
    //public abstract void run();
}

