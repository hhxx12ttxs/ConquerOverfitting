public abstract class Enemy extends Entity implements Cloneable{

protected double speed;
protected double nextX, nextY;
public Enemy(double health, double x, double y, double nextX, double nextY, double speed) {
super(health, x, y);

this.nextX = nextX;
this.nextY = nextY;

