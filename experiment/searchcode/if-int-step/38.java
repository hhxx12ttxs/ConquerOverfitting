public class Solution {
public int climbStairs(int n) {
if(n == 0 || n == 1)
return 1;
int current = 2, oneStep = 1, twoStep = 1;
for(int i = 2; i <= n; i++) {
current = oneStep + twoStep;

