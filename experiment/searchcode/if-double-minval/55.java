public static void deepFill(double[][][] arr, double filler) {
for (int i = 0; i < arr.length; i++) {
if (arr[i] == null) {
double minVal = Double.POSITIVE_INFINITY;
for (int i = 0; i <arr.length; i++) {
if (arr[i] != null) {
minVal = Math.min(minVal, minimum(arr[i]));

