while (array[endIndex] > pivot)
endIndex--;

if (startIndex <= endIndex) {
int temp = array[startIndex];
public static void quickSort(int[] array, int startIndex, int endIndex) {
int pivotIndex = partition(array, startIndex, endIndex);
if (startIndex < pivotIndex - 1)

