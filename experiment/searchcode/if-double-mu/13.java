expectation+=i*1.0/x.length;

return expectation;
}

public static double cov(double[] x, double mu_x, double[] y, double mu_y)
{
double covariance=0;
for(int i=0; i<x.length; i++)
covariance+=(x[i]-mu_x)*(y[i]-mu_y);

