double convergence,
double steadyStateThreshold) {
setMaxCostEval(maxCostEval);
this.steadyStateThreshold = steadyStateThreshold;
for (int i = 0; i < measurements.length; ++i) {
if (! measurements [i].isIgnored()) {

double weight   = measurements[i].getWeight();

