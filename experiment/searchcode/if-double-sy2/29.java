private void guessAOmega() throws org.apache.commons.math.optimization.OptimizationException {
double sx2 = 0.0;
double sy2 = 0.0;
double sxy = 0.0;
double c1 = (sy2 * sxz) - (sxy * syz);
double c2 = (sxy * sxz) - (sx2 * syz);
double c3 = (sx2 * sy2) - (sxy * sxy);
if (((c1 / c2) < 0.0) || ((c2 / c3) < 0.0)) {

