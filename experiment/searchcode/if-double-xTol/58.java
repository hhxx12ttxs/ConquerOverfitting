public static double optimize( Vector<Example> data , WeightParameter param, double lambda , double eps , int maxnfn ) throws IOException {
int m = 5;
double f = 0, xtol = 1e-30;
boolean diagco = false;
int[] iprint = new int[2];
for( int i = 0; i < x.length; ++i ) {
param.weightvector[i] = (float) x[i];
nn += x[i]*x[i];
}
if ( Double.isNaN(nn) || Double.isInfinite(nn) ) {

