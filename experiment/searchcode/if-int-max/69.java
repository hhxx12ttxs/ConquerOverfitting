public class Solution {
public int maxProfit(int[] prices) {
if (prices.length < 2)
return 0;

int[] maxL = new int[prices.length];
int[] maxR = new int[prices.length];

