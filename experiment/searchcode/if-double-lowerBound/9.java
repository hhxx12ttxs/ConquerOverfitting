public static double closestOctant(double angle) {
double step = Math.PI / 4;

if (angle < 0) {
angle += Math.PI;
}

double lowerBound = Math.floor(angle / step) * step;
double upperBound = lowerBound + step;

