int N = A.length;
int start = 0;
int end   = N - 1;
int[] range = new int[2];

while (end > start) {
int middle = (start + end) / 2;
if (target > A[middle])

