public class Solution {
public int coinChange(int[] coins, int amount) {
int[] dp = new int[amount+1];
if (coins==null) return -1;
//if (amount == 0) return 1;
if (coins.length ==0) return -1;

