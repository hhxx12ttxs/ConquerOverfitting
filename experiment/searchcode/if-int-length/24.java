int length = nums.length;
int[] temp = new int[length];
if (k > length) {
k = k % length;
}
if (k < 0) {
k = k % length +length;
}
for (int i = 0; i < length; i++) {

