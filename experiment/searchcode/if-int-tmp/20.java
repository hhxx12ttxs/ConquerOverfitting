public int nextLargest(final int n) {
if (n == -1 || n == 0) {
return 0;
}
int tmp = n;
int i = 0;
while (true) {
if ((tmp &amp; 3) == 1 || tmp == 0) {

