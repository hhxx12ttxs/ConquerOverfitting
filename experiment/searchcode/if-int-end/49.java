public int helper(int[] nums, int k, int left, int right) {
if (left == right) return nums[left];
int start = left + 1, end = right;
int temp = nums[left];
while (start <= end) {
while (start <= end &amp;&amp; nums[start] < temp) {

