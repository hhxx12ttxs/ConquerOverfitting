public static double min(double[] data) {
double min = data[0];
for (double entry : data) {
if (entry < min) {
double max = data[0];
for (double entry : data) {
if (entry > max) {
max = entry;
}
}
return max;

