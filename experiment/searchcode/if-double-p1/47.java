private static void drawLine(Color[][] pixels, Point2D p0, Point2D p1, boolean rev) {
double m = (p1.y - p0.y) / (p1.x - p0.x);
int sx = (p0.x < p1.x ? 1 : -1);
int sy = (p0.y < p1.y ? 1 : -1);
public static void drawLine(Color[][] pixels, Point2D p0, Point2D p1) {

double m = (p1.y - p0.y) / (p1.x - p0.x);
if (Math.abs(m) <= 1.0)

