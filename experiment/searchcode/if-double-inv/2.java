public static double[][] Inverse(double[][] B) {
int n = B.length;
double[][] invB = new double[n][n];
for (int i=0; i<n; ++i) {
for (int j=0; j<n; ++j) {
if (i == j) {
invB[i][j] = 1;
} else {
invB[i][j] = 0;

