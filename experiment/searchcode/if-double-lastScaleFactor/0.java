double q2 = a * q1 + b * q0;
boolean infinite = false;
if ((Double.isInfinite(p2)) || (Double.isInfinite(q2))) {
double scaleFactor = 1.0D;
double lastScaleFactor = 1.0D;
int maxPower = 5;

