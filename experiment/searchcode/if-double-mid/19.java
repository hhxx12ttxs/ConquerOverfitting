int end =x;
int mid =(start+end)/2;
while (!(mid*mid<=x &amp;&amp; (mid+1)*(mid+1)>x))
{
if (mid*mid>x)
end =mid-1;

if ((mid+1)*(mid+1) <=x)
start =mid+1;

mid =(start+end)/2;

