for (int ind = 0; ind < A.length; ind++) {
if (A[ind] == 0) {
if (p1 == -1 &amp;&amp; p2 == -1) {
continue;
} else if (p1 == -1) {
A[p2] = 0;
A[ind] = 2;
p2++;
} else if (p2 == -1) {
A[p1] = 0;
A[ind] = 1;

