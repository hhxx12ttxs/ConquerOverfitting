public class Solution {
public int maxSubArray(int[] nums) {
int maxSum = nums[0];
int Sum = nums[0];
for(int i=1; i<nums.length; i++) {
if(Sum <0) {

