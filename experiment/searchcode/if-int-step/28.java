int n = nums.length;

int i = 0;
while (i < n - 1) {
if (nums[i] + i >= n - 1) {
return true;
}

int stepNext = 0;
int maxStep = 0;
for (int step = 1; step <= nums[i]; step++) {
if (step + nums[i + step] >= maxStep) {

