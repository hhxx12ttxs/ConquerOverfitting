
public class maxSubArray {
public int maxSubArray(int[] A) {
int len = A.length;
int sum = 0;
int maxSum = 0;
for(int i = 0; i < len; i++) {
sum = sum + A[i];
if(sum > maxSum) {

