double fu = evaluate(b);
double upper = b;
double lower = a;
double x = 0.0;
double fx;
//System.out.println(&quot;delta = &quot; + (upper - lower));
else if((fx<0) &amp;&amp; (fu<0)){ upper = x; fu = fx;}
else { lower = x; fl = fx;}
}
return(x);
}

public double defIntegral(double a, double b, int nsteps){

