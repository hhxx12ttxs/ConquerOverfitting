public double[] coeff;

public IPolynomialFunction(int deg){ degree=deg; coeff=new double[degree+1]; }
public IPolynomialFunction(int deg, double[] coef){
degree = deg;
coeff = coef;
if(coeff.length != degree+1)
IOut.p(&quot;ERROR: length of coefficients is wrong&quot;);

