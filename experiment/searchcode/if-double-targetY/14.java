public class Shot extends Entity {

protected float dx, dy, targetX, targetY;
protected int speed = 9;
dx = this.targetX - x;
dy = this.targetY - y;
}

@Override
public void update() {
double distance = Math.sqrt((dx * dx) + (dy * dy));

