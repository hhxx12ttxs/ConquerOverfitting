double m2 = 0d;  //second moment
int count = 0;

double dev = 0.0d;
double nDev = 0.0d;

public void increment(double d) {
m2 += ((double) count - 1) * dev * nDev;
}

public double getResult() {
if (count == 0) {
return Double.NaN;
} else if (count == 1) {

