* Determine the area of a polygon. Points must be sorted counter-clockwise.
*/
public static double area(List<Point2D.Double> points) {
double area = 0.0;
Point2D.Double previous = null;
for (Point2D.Double point : points) {
if (previous != null)

