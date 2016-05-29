
public class Solution {
public int climbStairs(int n) {
if (n < 2) return 1;
int min = 1, max = 2;
for (int i = 2; i < n; ++i) {
min += max;
min ^= max;
max ^= min;
min ^= max;
}
return max;
}
}

