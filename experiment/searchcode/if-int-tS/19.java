public double closestToAverage(double[] ts) throws ValueOutOfBoundException {
if (ts.length == 0) {
return 0;
}
double average = calculateAverage(ts);
double temp = ts[0];
for (int i = 0; i < ts.length; i++) {

