public static double intersectionArea(Point a, Point b, double r0, double r1) {

double c = a.distance(b);
if(c >= r0+r1) {
return 0; // no intersection
}
if(c < r0 &amp;&amp; c + r1 < r0) {

