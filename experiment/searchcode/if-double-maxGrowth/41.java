interpolator.shift();

double error = 0;
for (boolean loop = true; loop;) {

if (firstTime || !fsal) {
computeDerivatives(stepStart, y, yDotK[0]);
}

if (firstTime) {
final double[] scale;
if (vecAbsoluteTolerance != null) {

