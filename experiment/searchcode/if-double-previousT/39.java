throw new ArithmeticException(&quot;Inverse value can not be computed for the range [-e^-1, 0]&quot;);
double u;

if(y < -0.2)
u = 10;
double previousT = 0, t;
do
{
t = (log(y/u)-u)*(u/(1+u));

