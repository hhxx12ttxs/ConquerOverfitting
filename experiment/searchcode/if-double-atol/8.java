* that is being minimized. Its value will be small if A*x = b has a
* solution.
*/
public double rnorm;
/**
* An estimate of the final value of norm(Abar&#39;*rbar). This is the
* @param maxi maximum number of iterations to perform.
*/
public LsqrSolver(double atol, double btol, double ctol, int maxi) {
_atol = atol;

