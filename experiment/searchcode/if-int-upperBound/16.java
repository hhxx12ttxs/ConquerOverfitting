public static int partition(int lowerBound, int upperBound, int a[]) {
int x = a[upperBound];
int i = lowerBound - 1;
for (int j = lowerBound; j < upperBound; j++) {
public static void doquicksort(int lowerBound, int upperBound, int a[]) {
if (lowerBound < upperBound) {
int pivot = partition(lowerBound, upperBound, a);

