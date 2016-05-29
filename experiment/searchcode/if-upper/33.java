if ((tmp.x > 0 &amp;&amp; tmp.y > 0) || (tmp.x < 0 &amp;&amp; tmp.y < 0)) {
if (tmp.x > 0) {
upperLeft = p1;
width = p2.x - p1.x;
height = p1.y - p2.y;
}
} else {
if (tmp.x > 0) {
upperLeft = new Point(p2.x - p1.x, p1.y - p2.y);

