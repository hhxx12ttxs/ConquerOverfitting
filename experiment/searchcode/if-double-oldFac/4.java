private void updateCovarianceDiagonalOnly(boolean hsig, final RealMatrix bestArz,
final RealMatrix xold) {
// minor correction if hsig==false
double oldFac = hsig ? 0 : ccov1Sep * cc * (2. - cc);
// minor correction if hsig==false
double oldFac = hsig ? 0 : ccov1 * cc * (2. - cc);

