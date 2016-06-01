public class Solution {
public int[] productExceptSelf(int[] nums) {
if (nums.length == 0){
int[] products = new int[nums.length];
products[0] = 1;
for (int idx = 1 ; idx < nums.length ; idx++){
products[idx] = products[idx - 1] * nums[idx - 1];

