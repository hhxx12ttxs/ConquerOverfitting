private int getStartVal(int n, int i, int j) {
int tmp = i < j ? i : j;
tmp = tmp < (n - 1 - i) ? tmp : (n - 1 - i);
tmp = tmp < (n - 1 - j) ? tmp : (n - 1 - j);
int sum = 0;
i = 0;
while (i < tmp) {
sum += (n - 1 - 2 * i) * 4;

