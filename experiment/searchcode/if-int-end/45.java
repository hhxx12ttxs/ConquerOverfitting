sort(A, 0, A.length - 1);
}

private void sort(int[] A, int start, int end) {
if(start >= end) return;

int index = partition(A, start, end);
sort(A, start, index);
sort(A, index + 1, end);

