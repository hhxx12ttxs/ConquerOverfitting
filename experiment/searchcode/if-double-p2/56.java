public static DoubleOrientedPoint minus(DoubleOrientedPoint p1, DoubleOrientedPoint p2) {
double x = p1.x - p2.x;
double y = p1.y - p2.y;
double theta = Utils.theta(p1.theta - p2.theta);
return new DoubleOrientedPoint(x, y, theta);

