// check orthogonality between function vector and jacobian columns
double maxCosine = 0;
if (cost != 0) {
// otherwise set this bound to zero
double sum2;
double parl = 0;
if (rank == solvedCols) {
for (int j = 0; j < solvedCols; ++j) {

