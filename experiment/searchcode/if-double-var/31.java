covariance+=(x[i]-mu_x)*(y[i]-mu_y);

return covariance*1.0/(x.length-1);
}

public static double Var(double[] x)
public static double rho(double[] x, double mu_x, double[] y, double mu_y)
{
return cov(x,mu_x,y,mu_y)/Math.sqrt(Var(x)*Var(y));
}

@Override

