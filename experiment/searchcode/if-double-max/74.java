public static double getMax(double[] arr) {
double max = arr[0];
int L = arr.length;
for (int i = 1; i < L; i++) {
if (arr[i] > max) {
public static double getMax(List<Double> arr) {
double max = arr.get(0);
for (double item : arr) {
if (item > max) {
max = item;

