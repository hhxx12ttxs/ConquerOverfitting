public class Point implements Comparable<Point> {
double x, y;

Point(double x, double y) {
this.x = x;
this.y = y;
}

@Override
public int compareTo(Point o) {
if (y != o.y)
return Double.compare(y, o.y);

