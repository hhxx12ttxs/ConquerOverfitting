public  class Solution{

public static void merge(int A[], int m, int B[], int n)
{
for(int idx=m+n-1;idx>=0;idx--){
if(m<=0){
A[idx]=B[--n];
}
else if(n<=0)
{
break;
}else if(A[m-1]<B[n-1])

