public class Solution {
public int numTrees(int n) {
int[] result= new int[n+1];
result[0]=1;
result[1]=1;
//if(n<1) return result[n];

for (int i=2; i<n+1; i++){

