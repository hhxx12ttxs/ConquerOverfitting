xNorm = 0;
for (int k = 0 ; k < (cols) ; ++k) {
double dk = jacNorm[k];
if (dk == 0) {
dk = 1.0;
if (nSing < (solvedCols)) {
work[j] = 0;
}
}
if (nSing > 0) {
for (int j = nSing - 1 ; j >= 0 ; --j) {

