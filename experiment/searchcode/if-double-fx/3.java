double upper = b;
double lower = a;
double x = 0.0;
double fx;
//System.out.println(&quot;delta = &quot; + (upper - lower));
//System.out.println(&quot;x = &quot; + (x));
fx = f.evaluate(x);
if(fx == 0) return(x);
else if((fx>0) &amp;&amp; (fu>0)){ upper = x; fu = fx;}

