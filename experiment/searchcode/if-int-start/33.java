int start = -1;
int end = N;

for (int i = 0; i < N; i++) {
if (i >= end)
while (A[i] != 1 &amp;&amp; end > i) {
if (A[i] == 0) {
int temp = A[start + 1];

