protected enum Direction
{
UP, DOWN, LEFT, RIGHT
}

protected Direction dir;

public void move(double xa, double ya) // xa = how the x position changes on the x-axis, ya = how the y position changes on the y-axis
// look in subclass to see how, into xa &amp; ya, we actually plug in -1 (left or down), 0 (no change in position), 1 (right or up)
// btw, we only handle moving if there&#39;s no collision

if(xa != 0 &amp;&amp; ya != 0)

