public static void curve(double x0, double y0, double x1, double y1, double var, double s) {
// stop if interval is sufficiently small
curve(xm, ym, x1, y1, var/s, s);
}

public static void main(String[] args) {
double H = Double.parseDouble(args[0]);

