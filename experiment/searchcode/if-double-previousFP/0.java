xNorm = 0;
for (int k = 0 ; k < (cols) ; ++k) {
double dk = jacNorm[k];
if (dk == 0) {
dk = 1.0;
delta = xNorm == 0 ? initialStepBoundFactor : (initialStepBoundFactor) * xNorm;
}
double maxCosine = 0;
if ((cost) != 0) {
for (int j = 0 ; j < (solvedCols) ; ++j) {

