// check orthogonality between function vector and jacobian columns
double maxCosine = 0;
if (cost != 0) {
int    pj = permutation[j];
double s  = jacNorm[pj];
if (s != 0) {

