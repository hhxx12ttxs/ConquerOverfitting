int start = 0;
int end = A.length - 1;
int i = -1;
while (start <= end) {
i = partition(A, start, end);
int pivot = A[end];
int i = start - 1;
for (int j = start; j < end; ++j) {
if (A[j] < pivot)
swap(A, ++i, j);

