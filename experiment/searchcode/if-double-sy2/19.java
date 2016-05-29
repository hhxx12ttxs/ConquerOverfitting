public static Position getClosestPointOnSegment(double sx1, double sy1, double sx2, double sy2, double px, double py) {
double xDelta = sx2 - sx1;
double yDelta = sy2 - sy1;

if ((xDelta == 0.0) &amp;&amp; (yDelta == 0.0)) {
throw new IllegalArgumentException(&quot;Segment start equals segment end&quot;);

