*      y&#39; - average of y
*      sigma  -  sum of vector.
*/
public static double pearsonSimilary(double[] x, double []y)
{

if(x.length == 0 || y.length == 0) return 0;
double sigma_x = MathVector.cumulativeSum(x);

