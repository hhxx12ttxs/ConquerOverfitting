public void nextPermutation(int[] num) {
for (int i = num.length - 1; i > 0; i--) {
if (num[i] > num[i-1]) {
for (int j = num.length - 1; j > i - 1; j--) {
if (num[j] > num[i-1]) {

