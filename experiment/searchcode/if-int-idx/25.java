void push(int val) {
if (cnt != size) {
int idx = cnt;
array[idx] = val;
while (idx != 0 &amp;&amp; idx / 2 >= 0) {
if (array[idx / 2] > val) {

