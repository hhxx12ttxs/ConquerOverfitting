public static void curve(double x0, double y0, double x1, double y1, double var, double s) {
if (x1 - x0 < 0.001) {
StdDraw.line(x0, y0, x1, y1);
double ym = (y0 + y1)/2;
double deltay = StdRandom.gaussian(0, Math.sqrt(var));
double deltax = StdRandom.gaussian(0, Math.sqrt(var));

