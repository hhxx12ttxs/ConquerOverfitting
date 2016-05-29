package cmu.edu.jinguanz.amazon;

public class ConsectiveMaxSum {

public int getMaxSum(int[] a){
int maxSum=0;
int sum=0;
for(int i=0;i<a.length;i++){
sum=sum+a[i];
if(maxSum<sum){

