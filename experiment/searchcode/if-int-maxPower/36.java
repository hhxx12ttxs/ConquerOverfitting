for (int i = 0; i < n; i++)
a[i] = Integer.parseInt(tokenizer.nextToken());

int maxPower = 0;
int position = 0;
for (int i = 0; i < n - 2; i++) {
int sum = a[i] + a[i + 1] + a[i + 2];
if (sum > maxPower) {

