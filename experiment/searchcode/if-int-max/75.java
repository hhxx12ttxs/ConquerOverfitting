public class Solution {
public int maxSubArray(int[] A) {
int t=0, max = Integer.MIN_VALUE, n=A.length;
for(int i=0; i<n; i++) {
t += A[i];
max = Math.max(max,t);

