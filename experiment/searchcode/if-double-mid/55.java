double cal(double C)
{
double l=0,r=pi/2;
while(r-l>eps)
{
double mid=(l+r)*0.5;
if(sin(mid)<C*mid) r=mid;
else l=mid;
}
return l;

