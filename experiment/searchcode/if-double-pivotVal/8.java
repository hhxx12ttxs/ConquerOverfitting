private int partition(int start, int end, int pivot) {
double pivotVal = array[pivot];
double temp;
array[pivot] =  array[end];
array[end] = pivotVal;
pivot = start;
for (int i = start; i < end; i++) {
if (array[i] < pivotVal) {

