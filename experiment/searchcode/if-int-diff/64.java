public class Solution {
public int[] singleNumber(int[] nums) {
int diff = 0;
int[] ret = {0, 0};
for (int n : nums){
if ( (n &amp; diff) == 0){
ret[0] ^= n;

