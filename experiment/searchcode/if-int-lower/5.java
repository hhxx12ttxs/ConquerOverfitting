public int searchInsert(int[] A, int target) {
int lower = 0;
int upper = A.length - 1;
while (lower <= upper) {
int middle = (lower + upper) / 2;
if (A[middle] == target) {
return middle;

