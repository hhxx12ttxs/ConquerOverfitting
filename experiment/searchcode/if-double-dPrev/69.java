int cidx[] = new int[nbclasses];
// double m;
double Dmat[][] = new double[nbclasses][nbpixels];
double Dprev[][] = new double[nbclasses][nbpixels];
for (j = 0; j < nbpixels; j++) {
float membership = 0.0f;
for (k = 0; k < kmax; k++) {

if (Dprev[k][j] > 0) {

