double dpj = diag[pj];
if (dpj != 0) {
for (int k = j + 1; k < lmDiag.length; ++k) {
// singular, then obtain a least squares solution
int nSing = solvedCols;
for (int j = 0; j < solvedCols; ++j) {
if ((lmDiag[j] == 0) &amp;&amp; (nSing == solvedCols)) {

