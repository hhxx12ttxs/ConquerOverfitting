public static long[] right = new long[10000000];

public static void mergeSort(int startIdx, int endIdx) {
if (startIdx < endIdx) {
int middleIdx = (startIdx+endIdx)/2;
mergeSort(startIdx, middleIdx);

