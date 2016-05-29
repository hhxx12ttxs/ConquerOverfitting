while (idx < k) {
int gap1 = Integer.MAX_VALUE;
int gap2 = Integer.MAX_VALUE;
if (i >= 0) {
gap1 = Math.abs(target - array[i]);
}
if (j < array.length) {
gap2 = Math.abs(target - array[j]);

