* == 1.0 + erf(z/Constants.SQRT2)) / 2.0;
*/
public static double Phi (double z)
{
// Taylor approximation

if (z <= -8.0) return 0.0;
if (z >=  8.0) return 1.0;

double sum = 0.0, term = z;

for (int i = 3; sum + term != sum; i += 2) {

