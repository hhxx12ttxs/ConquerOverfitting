public class Solution {
public int numWays(int n, int k) {
if(n <= 0 || k <= 0) return 0;
else if(n == 1) return k;
int diff = k * (k - 1);
int same = k;

