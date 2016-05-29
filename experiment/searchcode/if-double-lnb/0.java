* @return log value of the normalizing factor.
*/
public double lnB() {
double lnB = -0.5 * nu * logDetOmega
lnB -= Gamma.logGamma(0.5 * (nu + 1 - i));
}
return lnB;
}

private double expectationLnLambda() {
double ex_ln_lambda = D * Math.log(2) + logDetOmega;

