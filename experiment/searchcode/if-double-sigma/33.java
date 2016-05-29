public class MaxwellBoltzmann extends Distribution
{
/**
* shape
*/
double sigma;
final public void setShape(double sigma)
{
if(sigma <= 0 || Double.isInfinite(sigma) || Double.isNaN(sigma))
throw new ArithmeticException(&quot;shape parameter must be > 0, not &quot; + sigma);

