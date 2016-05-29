public double interpolate2(
double startValue, double endValue,
double fraction) {
if (Double.isNaN(fraction)) {
return startValue;
}
if (zeroDuration) {
return endValue;

