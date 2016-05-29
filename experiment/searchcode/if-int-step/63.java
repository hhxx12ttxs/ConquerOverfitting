public static int climbStairs(int n) {
int stepOne = 1, stepTwo = 2, stepNow = 0;
if (n == 0 || n == 1 || n == 2) return n;
for (int index = 2; index < n; index++){
stepNow = stepOne + stepTwo;

