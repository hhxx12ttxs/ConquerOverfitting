int minDiff = Integer.MAX_VALUE;
while ( i < A.length &amp;&amp; j < B.length) {
int curDiff = Math.abs(A[i] - B[j]);
minDiff = (curDiff < minDiff) ? curDiff : minDiff;
if (A[i] > B[j]) {

