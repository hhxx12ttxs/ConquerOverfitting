this.beta = beta;
}

@Override
public double pdf (double x)
{
if (x<0)
return 0;
else
return (alpha/beta) * Math.pow(x/beta, alpha-1) * Math.exp ( -Math.pow(x/beta,alpha) );
@Override
public double cdf (double x)
{
if (x<0)
return 0;
else
return 1 - Math.exp( -Math.pow(x/beta,alpha) );

