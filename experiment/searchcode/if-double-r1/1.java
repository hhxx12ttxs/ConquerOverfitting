public static double pmt(double r, double n, double p, double f, boolean t) {
double retval = 0;
if (r == 0) {
retval = -1*(f+p)/n;
}
else {
double r1 = r + 1;
retval = ( f + p * Math.pow(r1, n) ) * r

