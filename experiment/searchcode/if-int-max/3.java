
public class MaximumSubarray {

public int maxSubArray(int[] A) {
int len = A.length;
int max = A[0];
int t = A[0];
for(int i = 1; i < len; i++){
int v = A[i];

