public double distanceToRect(double[] point, double[] min, double[] max) {
double d = 0;

for (int i = 0; i < point.length; i++) {
double diff = 0;
if (point[i] > max[i]) {

