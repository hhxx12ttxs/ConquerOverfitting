static Point create(Double x, Double y) {
if (null != x &amp;&amp; null != y)
return new Point(x, y);
return new Point(p1.x + p2.x, p1.y + p2.y);
}

static Point multiply(Point p, double d) {
if (p == null)

