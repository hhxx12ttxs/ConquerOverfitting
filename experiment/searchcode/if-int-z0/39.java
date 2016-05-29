static public Complex form21(Complex z0, Complex z, double q, int kmax) {
if (kmax == 0) {
return z.sub(z0).ln().scale(q);
} else {
Complex sum = new Complex(0.0,0.0);
for (int k = 1; k < kmax; k++) {
//System.out.println(sum + &quot; : &quot; + z0.div(z).power(k));

