sort0(a, fromIndex, toIndex - 1);
}

private static void sort0(byte[] a, int start, int end) {
if (start < end) {
byte pivotVal = a[(start + end) >> 1];
while (a[left] < pivotVal)
left++;
while (a[right] > pivotVal)
right--;
if (left <= right) {

