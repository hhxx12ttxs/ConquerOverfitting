private double solution1(double n, double l, double u) {
double m = (1 + u) / 2;
double delta = m * m - n;
while (Math.abs(delta) > PRECISE) {
if (delta > 0) {

