this.pointB = pointB;
}

public Point getPoint(double t) {
if (t < 0 || t > 1) throw new IllegalArgumentException(&quot;Illegal value &quot; + t + &quot; for &#39;t&#39; in getPoint.  Value must be: 0>=t<=1&quot;);
public Double getSlope() {
double yDiff = pointB.y - pointA.y;
double xDiff = pointB.x - pointA.x;

if (xDiff != 0) {

