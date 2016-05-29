static double find(final double p, final double a, final double ptol,
final double xtol, final IContinuousDistribution cdist) {
double x = a;
double fx = cdist.getProbability(x, ProbabilityType.Lower) - p;
case Asymptotical: {
double lb = cdist.getLeftBound();
if (x <= lb)
x = (xprev + lb) / 2;
break;

