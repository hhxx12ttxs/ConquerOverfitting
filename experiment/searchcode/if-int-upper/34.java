return search(A, target, 0, A.length - 1);
}

boolean search(int[] A, int target, int lower, int upper) {
if (A[lower] < A[upper] &amp;&amp; !(target >= A[lower] &amp;&amp; target <= A[upper])) {
return false;
}
int middle = (lower + upper) / 2;
if (A[middle] == target) {

