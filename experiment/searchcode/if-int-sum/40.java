public class Solution {
public int minPatches(int[] nums, int n) {
long sum = 0;
int count = 0;
for (int x : nums) {
if (sum >= n) break;
while (sum+1 < x &amp;&amp; sum < n) {

