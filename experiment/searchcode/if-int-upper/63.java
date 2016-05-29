while (lower <= upper) {
int mid = (lower + upper) / 2;
if (A[mid] < target) {
lower = mid + 1;
while (lower <= upper) {
int mid = (lower + upper) / 2;
if (A[mid] > target) {
upper = mid - 1;

