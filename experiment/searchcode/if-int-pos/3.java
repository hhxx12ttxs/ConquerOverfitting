public int missingPositive(int[] A) {
int misMatchPos = -1;
for (int i = 0; i < A.length; ++i) {
if (A[i] < 0 || A[i] >= A.length) {
return A.length;
}

int pos = 0;
while (pos < A.length) {
if (A[pos] >= 0 &amp;&amp;
A[pos] < A.length &amp;&amp;

