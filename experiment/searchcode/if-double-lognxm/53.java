final double logm = FastMath.log(denominatorDegreesOfFreedom);
final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +
* </ul>
*/
public double cumulativeProbability(double x)  {
double ret;
if (x <= 0) {

