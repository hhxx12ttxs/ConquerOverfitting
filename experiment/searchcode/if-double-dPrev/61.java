int cidx[] = new int[nbclasses];
// double m;
double Dmat[][] = new double[nbclasses][nbpixels];
double Dprev[][] = new double[nbclasses][nbpixels];
double b2 = Math.pow(blue[j] - c[k][2], 2);

Dprev[k][j] = r2 + g2 + b2;
Uprev[k][j] = 0.0;

if (Dprev[k][j] < min) {
min = Dprev[k][j];

