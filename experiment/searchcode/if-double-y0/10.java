public static void drawH(double x, double y, double size) {
double x0 = x - size/2;
double x1 = x + size/2;
double y0 = y - size/2;
double y1 = y + size/2;

StdDraw.line(x0, y0, x0, y1);
StdDraw.line(x1, y0, x1, y1);

