int i;
int len;
int ans;
len=0;
ans=0;
for(i=0;i<n;i++)
{
int x=cin.nextInt();
int h=cin.nextInt();
while(len>0&amp;&amp;a[len-1]>h)
{
if(a[len-1]>0)	ans++;
len--;

