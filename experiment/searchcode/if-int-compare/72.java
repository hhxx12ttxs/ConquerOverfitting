public static void sort(short a[][], int[] compareMask) {
qSort(a, 0, a.length - 1, compareMask);
}

private static void qSort(short a[][], int left, int right,
int[] compareMask) {
int i = partition(a, left, right, compareMask);

