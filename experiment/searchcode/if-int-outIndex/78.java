// handle the case of having the window size is equal to &quot;nums&quot; length

if (nums.length <= 1 || k == 1) {
return nums;
}

int[] toBeReturned = new int[nums.length - k + 1];
if (out == max) {
if (in >= out)
max = in;
else {
max = nums[outIndex + 1];
for (int j = outIndex + 2; j < inIndex + 1; j++) {

