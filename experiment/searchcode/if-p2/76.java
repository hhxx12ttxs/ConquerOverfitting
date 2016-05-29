public static int remove(int array[], int n) {
int p1 = 0;
int p2 = array.length - 1;
while (p1 < array.length &amp;&amp; array[p1] != n) {
while (p1 < p2 &amp;&amp; array[p2] == n)
p2--;
if (p1 < p2) {
array[p1] = array[p2];
array[p2] = n;

