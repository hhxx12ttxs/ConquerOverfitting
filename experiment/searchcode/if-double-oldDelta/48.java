* @throws IllegalArgumentException if initial is not between min and max
*/
public double solve(double min, double max, double initial)
double delta = x1 - x0;
double oldDelta = delta;

int i = 0;
while (i < maximalIterationCount) {

