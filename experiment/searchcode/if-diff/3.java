public int[] singleNumber(int[] nums) {
int diff = 0;
for(int n : nums) {
for (int n : nums) {
if ((n&amp;diff) == 0) {
result[0] ^= n;
} else {

