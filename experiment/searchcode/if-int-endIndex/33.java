public int[] searchRange(int[] nums, int target) {
int[] result = new int[2];
int startIndex = 0;
int endIndex = nums.length - 1;
int mid = -1;
Boolean foundTarget = false;

if (nums.length > 0) {
while (startIndex <= endIndex) {
mid = (startIndex + endIndex) / 2;

