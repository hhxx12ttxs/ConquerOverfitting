public static double betai(final double a, final double b, final double x) {
double bt;
if (a <= 0.0 || b <= 0.0) throw new IllegalArgumentException(&quot;Bad a or b in routine betai&quot;);
double lna = log(a/(a+b)), lnb = log(b/(a+b));
t = exp(a*lna)/a;
u = exp(b*lnb)/b;
w = t + u;
if (p < t/w) x = pow(a*w*p,1./a);

