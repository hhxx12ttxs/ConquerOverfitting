public static final int RIGHT = 1;
public static final int LEFT = -1;

public static final Pos NORTH_POS = new Pos(0,-1);
// bends a direction, -1 for left bend, 1 for right bend
public static int dirBend(int dir, int bend) {
int bendedDir = (dir + bend) % 4;
if(bendedDir < 0) {

