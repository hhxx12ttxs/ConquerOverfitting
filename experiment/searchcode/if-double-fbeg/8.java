double deltaOne = delta;
double deltaTwo = FastMath.sqrt(dsq);
dnorm = FastMath.min(deltaOne, deltaTwo);
if(dnorm < HALF * rho) {
gradientAtTrustRegionCenter.setEntry(nfmm, (f - fbeg) / stepa);
if(npt < numEval + n) {
final double oneOverStepA = ONE / stepa;

