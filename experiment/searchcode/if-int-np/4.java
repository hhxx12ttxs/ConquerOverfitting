final int D = lb.length;
final int NP = 5 * D;
final double F = 0.7;
final double P = 0.5;
double[] CR = new double[NP];
for (int i = 1; i < NP; ++i) {
if (f[i] < fmin) {
fmin = f[i];
fminidx = i;
}
}
xmin = X[fminidx];

