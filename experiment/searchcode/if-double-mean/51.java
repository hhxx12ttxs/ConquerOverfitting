public class Gaussian extends Randomised implements Process
{
private final double mean;
private final double variance;

public Gaussian(double mean, double variance, Random r)
{
super(r);
if (mean < 0.3) throw new IllegalArgumentException(&quot;Mean must be >= 0.3&quot;);

