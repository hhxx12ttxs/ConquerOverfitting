int MAX = 10000000;
int n = 0;
double ratioMin = Double.MAX_VALUE;
double ratio;

for ( int x = 2; x < MAX; x++ ) {
if ( commonMath.sameDigits(x, fiX) ) {
ratio = (double)x / (double)fiX;
if ( ratio < ratioMin ) {

