private static void insertionSort(int[] numbers) {

for (int i = 0; i < numbers.length; i++) {

int newPos = i;

while (newPos > 0 &amp;&amp; numbers[newPos - 1] > numbers[newPos]) {
int t = numbers[newPos - 1];

