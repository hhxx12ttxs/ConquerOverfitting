int sum = 0;
for (int a = 0; a < n; a++)

{
sum += A[a];
if (sum > maxSum)
maxSum = sum;
if (sum < 0)
int sum = Integer.MIN_VALUE;
for (int b = a; b < n; b++) {
sum += A[b];
if (sum > maxSum)
maxSum = sum;

