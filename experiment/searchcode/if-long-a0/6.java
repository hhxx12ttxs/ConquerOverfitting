for (long z = 1; z < LIMIT * 2; z++) {
long a0 = z / 3 + 1;
long n = -z * z + 2 * a0 * z + 3 * a0 * a0;
for (long a = a0;; a++, n += 2 * z + 6 * a - 3) {
if (n >= LIMIT) {
break;
}
solutionNumbers[(int) n]++;

