int pivotindex=-1;
int replaceindex=-1;

for(int i=len-1;i>=0;i--)
{
//i=4
//i=3
if(i==0)
for(int  j = pivotindex+1 ;j<len;j++)
{
//j=4
//j=3
int d= arr[j]-arr[pivotindex];
//d=1

if(d<min &amp;&amp; d>0){

