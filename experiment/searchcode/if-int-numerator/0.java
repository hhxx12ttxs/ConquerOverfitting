public int[] getDifferences(int n) {
int numerator = 2 * n * n * n - n * n * n * n;
for (int i = 1; i <= n - 1; i++) {
numerator += 4 * i * i * i;
}
if (numerator % 4 == 0) {
return new int[] { numerator / 4, 1 };
} else if (numerator % 2 == 0) {

