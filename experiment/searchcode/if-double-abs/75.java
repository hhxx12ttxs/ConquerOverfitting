public static double hypot(double a, double b) {
double r;
if (Math.abs(a) > Math.abs(b)) {
r = b/a;
r = Math.abs(a)*Math.sqrt(1+r*r);
} else if (b != 0) {

