while (array[endIndex] > pivot)
endIndex--;

if (startIndex <= endIndex) {
int temp = array[startIndex];
public static void getKthSmallest(int[] array, int startIndex,
int endIndex, int k) {
int pivotIndex = partition(array, startIndex, endIndex);
if (pivotIndex == k - 1 || (pivotIndex == k &amp;&amp; k == 1))

