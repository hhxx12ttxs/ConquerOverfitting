protected static Random random = new Random();

public double xa, ya;
public double x, y;
protected double bounce = 0.05;
public void init (Level level) {
this.level = level;
}

public void tryMove (double xa, double ya) {
onGround = false;
if (level.isFree(this, x + xa, y, w, h, xa, 0)) {

