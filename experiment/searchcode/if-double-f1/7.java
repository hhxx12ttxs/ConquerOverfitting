double dx = 0.01;
double f1 = -1000000, f2 = 0;
if (n == 1)
{
while(Math.abs(f2 - f1) >= e)
{
f2 = f1;
f1 = (_GetDifferentialInPoint(x + dx, n - 1) - _GetDifferentialInPoint(x, n - 1)) / dx;
dx /= 2;
}
}
return f1;
}
public static double GetDifferentialInPoint(double x, int n)

