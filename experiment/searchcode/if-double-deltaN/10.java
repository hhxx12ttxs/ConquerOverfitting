public void init(double[] d) {


if (d.length < 2) {
return;
}
double last;
double tmp = 0;
double[] deltaN = new double[(int) len - 1];
for (int i = 1; i < d.length; i++) {

