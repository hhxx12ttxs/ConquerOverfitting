public static double variance(double x[], double mean) {
double dev = 0;
if (x.length > 1) {
for (int i = x.length - 1; i >= 0; i--) {
public static double variance(Collection<Double> x, double mean) {
if (x.size() == 0) {
return Double.NaN;
}
double dev = 0;

