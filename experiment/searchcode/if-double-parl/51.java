for (int k = 0; k < cols; ++k) {
double dk = jacNorm[k];
if (dk == 0) {
dk = 1.0;
int    pj = permutation[j];
double s  = jacNorm[pj];
if (s != 0) {
double sum = 0;

