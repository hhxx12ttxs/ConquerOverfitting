long sort(int[] nums, int startIndex, int endIndex) {
if (startIndex >= endIndex) {
return 0l;
}
long comparisons = endIndex - startIndex - 1;
int pivot = nums[startIndex];
int i = startIndex + 1;
for (int j = i; j < endIndex; j++) {
if (nums[j] < pivot) {

