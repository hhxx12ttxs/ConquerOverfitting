public static double u(double r, double b1, double gamma) {
double u = 0.0;

if (r < b1) {
u = -r;
}
else {
u = (r - (1.0 + gamma)*b1) / gamma;
public static double v(double r, double b2, double b1, double gamma) {
double v = 0.0;

if (r < b2) {
v = -r + b2;
} else if (r > b1 * (1.0 + gamma)) {

