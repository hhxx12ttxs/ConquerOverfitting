private double fCheb() {
double[] p = new double[101];
double R       = 0.0 ;
double tmp1,tmp2;
Random r = new Random();
p[i] = -1.0 + 2.0 * r.nextDouble();

for (int i = 0; i < 101; i ++) {
tmp1 = Pc(p[i]);
if ((tmp1 < -1) || (tmp1 > 1))

