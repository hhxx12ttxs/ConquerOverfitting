private static int partition(ArrayList<Double> list, int left, int right, int pivotIndex) {
double pivotVal = list.get(pivotIndex);
for (int i = left; i < right; ++i) {
if (list.get(i) <= pivotVal) {
Collections.swap(list, i, storeIndex);

