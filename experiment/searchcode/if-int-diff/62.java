public class Solution {
public int[] singleNumber(int[] nums) {
int diff = 0;
int[] rets = {0, 0};
for (int num : nums) {
if ((num &amp; diff) == 0) {
rets[0] ^= num;

