package chapter17;

public class Q8 {

public static int getMaxSum(int[] arr){
int sum = 0;
int maxSum = 0;

for(int i = 0; i < arr.length;i++){
sum += arr[i];
if(sum >= maxSum) maxSum = sum;

