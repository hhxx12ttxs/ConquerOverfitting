* @see ChiSquare#ChiSquare
************************************************************/

public double sampleDouble() {
chi += fExp.sampleDouble();
chi *= 2.0;

if( fDegsOdd ) {
double z = fNorm.sampleDouble();
chi += z*z;
}
return chi;

