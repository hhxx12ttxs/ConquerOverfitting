private double minReduction;

/** Maximal growth factor for stepsize control. */
private double maxGrowth;
// iterate over step size, ensuring local normalized error is smaller than 1
double error = 10;
while (error >= 1.0) {

if (firstTime || !fsal) {

