public class Brownian {
public static void curve(double x0, double y0,
double x1, double y1,
double var, double s)
{
if (x1 - x0 < .01) {
StdDraw.line(x0, y0, x1, y1);

