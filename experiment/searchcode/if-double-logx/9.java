public static double logAdd(double logX, double logY) {

// 1. make X the max
if (logY > logX) {
double temp = logX;
logX = logY;
logY = temp;
}

// 2. now X is bigger
if (logX == Double.NEGATIVE_INFINITY) {

