private static void reconstruct(int[] x, int n) {
int endIndex = n * 2;

// Unpack
int[] temp = new int[endIndex];
for (int i = 2; i < endIndex; i += 2) {
x[i] += (x[i - 1] + x[i + 1] + 2) >> 2;
}
x[0] += x[1];

// Pack
int[] temp = new int[endIndex];
for (int i = 0; i < endIndex; i++) {
if (i % 2 == 0) {

