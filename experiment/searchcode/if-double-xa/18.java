public void move(double xa, double ya)  {
if (xa != 0 &amp;&amp; ya != 0) {
move(xa, 0);
move(0, ya);
return;
}

if (xa > 0) dir = Direction.RIGHT;
if (xa < 0) dir = Direction.LEFT;
if (ya > 0) dir = Direction.DOWN;

