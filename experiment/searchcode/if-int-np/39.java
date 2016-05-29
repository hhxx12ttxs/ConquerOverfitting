final int D = lb.length;
final int NP = 5 * D;
final double F = 0.7;
double[] CR = new double[NP];

for (int i = 0; i < NP; ++i) {
fmin = f[0];
for (int i = 1; i < NP; ++i) {
if (f[i] < fmin) {
fmin = f[i];
fminidx = i;

