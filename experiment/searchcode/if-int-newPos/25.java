for (int i = 1; i < array.length; i++) {
int newPos = i;

while (newPos > 0 &amp;&amp; array[newPos - 1] > array[newPos]) {
int tmp = array[newPos - 1];
array[newPos - 1] = array[newPos];
array[newPos] = tmp;

