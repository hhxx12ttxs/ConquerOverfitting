public static void dragon(double x0, double y0, double x1, double y1, int level) {
if (level == 0) {
StdDraw.line(x0, y0, x1, y1);
} else {
double dx = x1 - x0;
double dy = y1 - y0;
double xm = (x0 + x1) / 2;
double ym = (y0 + y1) / 2;
double xNew = xm - dy/2;

