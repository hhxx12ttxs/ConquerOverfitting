public void move(double xa, double ya, Mob mob) {
if ((xa > 0 &amp;&amp; xa < 1) || (ya > 0 &amp;&amp; ya < 1) || (xa < 0 &amp;&amp; xa > -1) || (ya < 0 &amp;&amp; ya > -1)) {
move(xa, ya);
}

public void move(double xa, double ya) {
if (xa != 0 &amp;&amp; ya != 0) {
move(xa, 0);
move(0, ya);

