int cidx[] = new int[nbclasses];
// double m;
double Dmat[][] = new double[nbclasses][nbpixels];
double Dprev[][] = new double[nbclasses][nbpixels];
for (j = 0; j < nbpixels; j++) {

Uprev[i][j] = 0;

for (k = 0; k < kmax; k++) {
if (Dprev[k][j] != 0) {
Uprev[i][j] += Math.pow(Dprev[i][j] / Dprev[k][j],

