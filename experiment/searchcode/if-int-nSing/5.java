int pk = permutation[k];
jacobian[k][pk] = diagR[pk];
}
if (firstIteration) {
xNorm = 0;
for (int k = 0 ; k < (cols) ; ++k) {
int nSing = solvedCols;
for (int j = 0 ; j < (solvedCols) ; ++j) {
if (((lmDiag[j]) == 0) &amp;&amp; (nSing == (solvedCols))) {

