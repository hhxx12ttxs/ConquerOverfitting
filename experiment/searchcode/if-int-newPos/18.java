for (int i = 0; i < array.length; i++) {
int newPos = i;

while (newPos > 0 &amp;&amp; array[newPos - 1] > array[newPos]) {

int t = array[newPos - 1];
for (int i = array.length - 1; i >= 0; i--) {
int maxIndex = array[0];

for (int j = 1; j <= i; j++) {
if (array[j] > array[maxIndex]) {

