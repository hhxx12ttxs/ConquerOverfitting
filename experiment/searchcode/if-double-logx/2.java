public double logAdd(double logX, double logY) {

if (logY > logX) {
double temp = logX;
logX = logY;
logY = temp;
}

if (logX == Double.NEGATIVE_INFINITY) {

