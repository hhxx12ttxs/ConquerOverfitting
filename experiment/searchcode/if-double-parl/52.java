double dk = jacNorm[k];
if (dk == 0) {
dk = 1.0;
}
double xk = dk * point[k];
double sum2;
double parl = 0;
if (rank == solvedCols) {
for (int j = 0; j < solvedCols; ++j) {

