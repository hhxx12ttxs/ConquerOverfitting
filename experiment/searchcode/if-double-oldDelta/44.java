// Index 2 is a bracket for the root with respect to x0.
// OldDelta is the length of the bracketing interval of the last
// iteration.
double x0 = min;
double y2 = y0;
double oldDelta = x2 - x1;
int i = 0;
while (i < maximalIterationCount) {

