private int partition(double[] arr, int left, int right) {
int pivot = medianOfThrees(arr, left, right);
double pivotVal = arr[pivot];
swap(arr, pivot, right);
private void swap(double[] arr, int a, int b) {
if (a != b) {
double tmp = arr[a];
arr[a] = arr[b];
arr[b] = tmp;

