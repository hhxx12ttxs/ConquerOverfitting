return a[A]*Math.exp(a[K]*x[0]);
} //val

public double grad(double[] x, double[] a, int a_k)
{
if (a_k == K)
} //grad
public double[] initial()
{
double[] a = new double[2];
a[A] = 100;
a[K] = 1;
return a;

