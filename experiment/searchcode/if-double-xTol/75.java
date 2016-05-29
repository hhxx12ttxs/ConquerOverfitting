int max_iter, int m, double eps, double xtol)
throws LBFGS.ExceptionWithIflag
{
final int n=x0.length;
double f=func.evaluateFunction(x0, g);
LBFGS.lbfgs(n, m, x0, f, g, diagco, diag, iprint, eps, xtol, iflag);
if (iflag[0]<=0) break;

