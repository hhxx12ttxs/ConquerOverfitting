public int removeElement(int[] A, int elem) {
if (A == null || A.length < 1)
return 0;
int len = A.length;
for (int i = 0;i < len; i++) {
if (A[i] != elem)
continue;
len--;

