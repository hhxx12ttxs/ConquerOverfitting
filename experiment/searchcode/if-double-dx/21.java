System.out.print(&quot;[&quot; + a + &quot;, &quot; + b + &quot;]\t n = &quot; + n + &quot;\tI = &quot;);

double s = 0;
double st = 0;

double dx = (b - a) / n;
for(int i = 1; i<=n; i++) {
double x = a + i*dx;
st += getValue(f, x-dx/2);
if(i < n) s += getValue(f, x);

