public class Solution {
public int maxSubArray(int[] A) {

int max = A[0];
for (int i=1; i<A.length; i++){
A[i] = Math.max(A[i-1]+A[i], A[i]);
if (A[i] > max) max = A[i];

