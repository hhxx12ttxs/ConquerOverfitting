// check orthogonality between function vector and jacobian columns
double maxCosine = 0;
if (cost != 0) {
for (int j = 0; j < solvedCols; ++j) {
// singular, then obtain a least squares solution
int nSing = solvedCols;
for (int j = 0; j < solvedCols; ++j) {
if ((lmDiag[j] == 0) &amp;&amp; (nSing == solvedCols)) {

