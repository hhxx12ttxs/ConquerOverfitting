public class Solution {
public int climbStairs(int n) {
if (n <= 0 ) { return 0; }
if (n <= 2) { return n; }

int res = 0;
int stepOne = 1, stepTwo = 1;

