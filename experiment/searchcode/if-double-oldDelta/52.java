* @throws IllegalArgumentException if initial is not between min and max
*/
public double solve(double min, double max, double initial)
double oldDelta = delta;

int i = 0;
while (i < maximalIterationCount) {
if (Math.abs(y2) < Math.abs(y1)) {

