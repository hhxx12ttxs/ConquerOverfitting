protected Direction dir;

public void move(double xa, double ya) {
if(xa != 0 &amp;&amp; ya != 0) {
if (collision(xa , 0)) {
if (xa < 0) dir = Direction.LEFT;
if (ya > 0) dir = Direction.DOWN;
if (ya < 0) dir = Direction.UP;

while(xa != 0) {

