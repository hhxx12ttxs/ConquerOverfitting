public class Solution {
public void rotate(int[] nums, int k) {
int len = nums.length;
int res[] = new int[len];

if(len==k) return; // worst case

