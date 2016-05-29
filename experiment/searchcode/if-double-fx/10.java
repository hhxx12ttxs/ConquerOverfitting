double[][] xfx = localization(f, x0, h, e);
double[] x = xfx[0];
double[] fx = xfx[1];
double x3, fx3;

if (x[1] == x[2] || x[0] == x[1] || x[0] == x[2]) return new double[] {x[0], fx[0]};
/ ((x[2] - x[1]) * (fx[0] - fx[1]) + (x[1] - x[0]) * (fx[2] - fx[1])));
double fxs = f.calc(xs);
k++; fk++;
if (Math.abs(xs - x[1]) < e) {

