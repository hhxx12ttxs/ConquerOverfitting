int width = Math.abs(p1.x - p2.x);
int height = Math.abs(p1.y - p2.y);

if ((p2.y <= p1.y) &amp;&amp; (p2.x <= p1.x)) {
g.drawRect(p2.x, p2.y, width, height);
} else if ((p2.y <= p1.y) &amp;&amp; (p1.x <= p2.x)) {

