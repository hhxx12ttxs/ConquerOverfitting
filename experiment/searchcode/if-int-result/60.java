public class Solution {

public int uniquePaths(int m, int n) {

if(m==0 &amp;&amp; n==0) return 0;

int[] result=new int[n];

result[0]=1;

for(int i=0;i<m;i++){

