totalEvaluations += optimizer.getEvaluations();
}
int lastNaN = optima.length;
for (int i = 0 ; i < lastNaN ; ++i) {
if (java.lang.Double.isNaN(optima[i])) {
double currY = optimaValues[0];
for (int j = 1 ; j < lastNaN ; ++j) {
final double prevY = currY;
currX = optima[j];
currY = optimaValues[j];

