* @return delta from a, b, c
*/
double Delta (double a, double b, double c)
{
return b * b - (4 * a * c);
double mzerowe;
double delta = Delta(a, b, c);

if (delta < 0)
{
mzerowe = Double.POSITIVE_INFINITY;

