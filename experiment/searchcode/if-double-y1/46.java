
public class CCurve {

public static void cCurve(double x0, double y0, double x1, double y1, int level) {
if (level == 0) {
StdDraw.line(x0, y0, x1, y1);
} else {
double dx = x1 - x0;

