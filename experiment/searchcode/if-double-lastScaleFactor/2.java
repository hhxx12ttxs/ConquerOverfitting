double p2 = (a * p1) + (b * p0);
double q2 = (a * q1) + (b * q0);
boolean infinite = false;
if ((java.lang.Double.isInfinite(p2)) || (java.lang.Double.isInfinite(q2))) {
double lastScaleFactor = 1.0;
final int maxPower = 5;
final double scale = java.lang.Math.max(a, b);
if (scale <= 0) {

