public static double distance(Line l, Point p) {
Point a = l.getStart();
Point b = l.getEnd();

return distancePointToLine(a, b, p);
}

public static double distancePointToLine(Point a, Point b, Point p) {

