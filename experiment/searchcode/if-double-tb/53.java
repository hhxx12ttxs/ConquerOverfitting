public double get(double x, double y) {
int numsteps = numTiers;
if (smooth) --numsteps;
double val = source.get(x, y);
double Tb = Math.floor(val * (double) (numsteps));
double Tt = Tb + 1.0;

