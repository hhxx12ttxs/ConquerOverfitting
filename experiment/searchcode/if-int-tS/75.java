public double closesToZero(double[] ts) throws Exception {
if (ts == null || ts.length == 0) {
return 0;
}
double result = ts[0];
for (int i = 0; i < ts.length; i++) {
if (ts[i] < MIN_TEMPERATURE || ts[i] > MAX_TEMPERATURE) {

