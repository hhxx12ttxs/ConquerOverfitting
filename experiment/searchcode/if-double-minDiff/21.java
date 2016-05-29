// nums[right] <= target
// nums[left] > target
double minDiff = Double.MAX_VALUE;
int optIdx = -1;
if (right >= 0 &amp;&amp; Math.abs(nums[right] - target) < minDiff) {
minDiff = Math.abs(nums[right] - target);

