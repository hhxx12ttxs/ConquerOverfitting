private int kthValue(int[] A, int startA, int endA, int[] B, int startB, int endB, int k) {
int lenA = endA - startA + 1, lenB = endB - startB + 1;
return Math.min(A[startA], B[startB]);
int j = Math.min(lenB, k / 2), i = k - j;
if (A[startA + i - 1] > B[startB + j - 1])
return this.kthValue(A, startA, startA + i - 1, B, startB + j, endB, k - j);

