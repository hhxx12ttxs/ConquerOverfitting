double xmax = -Float.MAX_VALUE;
double ymin = Float.MAX_VALUE;
double ymax = -Float.MAX_VALUE;
double zmin = Float.MAX_VALUE;
xmax = Math.max(x, xmax);
ymin = Math.min(y, ymin);
ymax = Math.max(y, ymax);

if (xmax - xmin > 0 &amp;&amp; ymax - ymin > 0)

