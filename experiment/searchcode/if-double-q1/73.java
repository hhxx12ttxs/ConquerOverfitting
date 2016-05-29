private static boolean isIntersectedLines(Point p1, Point p2, Point q1, Point q2) {
if (p2.y - p1.y != 0 &amp;&amp; q2.y - q1.y != 0) {
double slope1 = (p2.x - p1.x) / (p2.y - p1.y);
double slope2 = (q2.x - q1.x) / (q2.y - q1.y);
return Math.abs(slope2 - slope1) < E;

