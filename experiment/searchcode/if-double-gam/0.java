int  j;
double   bet;

int n = a.length;
double [] gam = new double [n];
if (b[0] == 0.0) throw new NRException(&quot;Error 1 in tridag&quot;);
for (j=1;j<n;j++) {
gam[j] = c[j-1]/bet;
bet = b[j]-a[j] * gam[j];
if (bet == 0.0) throw new NRException(&quot;Error 2 in tridag&quot;);

