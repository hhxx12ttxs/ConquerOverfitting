protected Direction dir;

public void move(double xa, double ya) {
if(xa != 0 &amp;&amp; ya != 0) {
if (collision(xa , 0)) {
move(xa, 0);
}
return;
}

if (xa > 0) dir = Direction.RIGHT;
if (xa < 0) dir = Direction.LEFT;

