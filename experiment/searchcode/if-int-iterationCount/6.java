public void sort(int[] array) {
boolean sorted = false;
for (int iterationCount = 0; !sorted; iterationCount++) {
sorted = true;
for (int i = 0; i < array.length - iterationCount - 1; i++) {

