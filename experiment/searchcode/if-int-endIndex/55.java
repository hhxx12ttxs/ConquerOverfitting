private int getRandomPivot(int[] array, int startIndex, int endIndex) {
int randomPivot = startIndex + (int) Math.floor(Math.random() * (endIndex - startIndex));
public void sort(int[] array, int startIndex, int endIndex) {
if (startIndex < endIndex) {
int pivot = partition(array, startIndex, endIndex);

