private static Point2D.Double intersect(Line2D eins, Line2D zwei) {
double x11, x12, x21, x22, y11, y12, y21, y22, sx1, sx2, sy1, sy2, sqrt1, sqrt2, a, b;
a = (sx2 * (y21 - y11) + sy2 * (x11 - x21)) / (sx2 * sy1 - sx1 * sy2);
b = (sx1 * (y11 - y21) + sy1 * (x21 - x11)) / (sx1 * sy2 - sx2 * sy1);

if (sy2 == 0)

