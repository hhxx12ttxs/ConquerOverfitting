public class Solution{
public int maxProfit(int[] prices){
if (prices.length <= 1 || prices == null){
int[] b = new int[m];
int tmp = prices[0];
f[0] = 0;
for (int i = 1; i < m; i++){
tmp = Math.min(tmp, prices[i]);

