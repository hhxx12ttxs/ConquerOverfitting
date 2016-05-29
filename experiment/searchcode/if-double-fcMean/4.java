/** Estimate a first guess of the &amp;phi; coefficient.
*/
private void guessPhi() {

// initialize the means
double fcMean = 0.0;
double   cosine = FastMath.cos(omegaX);
double   sine   = FastMath.sin(omegaX);
fcMean += omega * currentY * cosine - currentYPrime *   sine;

