public final double xmin, xmax, ymin, ymax;

public Rect(final double xmin, final double xmax, final double ymin, final double ymax) {
if (xmax < xmin || ymax < ymin) throw new IllegalArgumentException();
public double distanceSquared(final double x, final double y) {
if (xmin <= x &amp;&amp; x <= xmax &amp;&amp; ymin <= y &amp;&amp; y <= ymax) return 0;

