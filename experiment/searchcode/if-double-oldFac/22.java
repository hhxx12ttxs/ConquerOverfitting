final RealMatrix bestArz) {
// minor correction if hsig==false
double oldFac = hsig ? 0 : ccov1Sep * cc * (2 - cc);
// minor correction if hsig==false
double oldFac = hsig ? 0 : ccov1 * cc * (2 - cc);
oldFac += 1 - ccov1 - ccovmu;

