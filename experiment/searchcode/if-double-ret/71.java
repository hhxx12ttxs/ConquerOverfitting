public static double divergence(double[] p, double[] q) {
double ret = 0;
for (int i = 0; i < p.length; i++) {
if (p[i] > 0.0) {
double q_i = q[i];
if (q_i < 10e-5)
q_i = 10e-5;
ret += p[i] * (Math.log(p[i]) - Math.log(q[i]));

