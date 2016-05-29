package maximumSubarray;

public class Solution {
public int maxSubArray(int[] A) {
if (A == null || A.length == 0) {
return 0;
}
int localMax = A[0];

