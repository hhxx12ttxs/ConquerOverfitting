public boolean isDiscrete()
{
return true;
}

public double density(double startValue, double endValue)
{
if (endValue < startValue)
throw new RuntimeException(this+&quot;&#39;s density function called with endValue <= startValue.  Note that startValue >= endValue&quot;);

