return Math.abs(a - b) < EPS;
}

static double[] solveQuadratic(double a, double b, double c)
{
double delta = b * b - 4 * a * c;
if (delta < -EPS)
return new double[0];

double[] ret;

