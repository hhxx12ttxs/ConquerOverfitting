int p1=m-1;
int p2=n-1;
int p3=m+n-1;
if(n==0)
return;

while(p1>=0&amp;&amp;p2>=0)
{
if(A[p1]>=B[p2])
{
A[p3]=A[p1];

