public int index(int[] nums, int start, int end, int searchSeq) {
int flag = nums[start];
int startIndex = start;
int endIndex = end;
while (startIndex < endIndex) {
if (flag > nums[startIndex + 1]) {

