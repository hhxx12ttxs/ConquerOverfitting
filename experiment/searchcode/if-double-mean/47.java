return array;
}

private static double mean(double[] numbers) {
double sum = 0;
private static double chernoffBound(double mean, double x) {
if (x > mean) {
// 1/(e^mean) * (mean * e / x)^x

