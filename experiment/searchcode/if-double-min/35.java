max = a[i];
}
return max;
}
public static double min(double[] a)
{
double min = Double.POSITIVE_INFINITY;
for (int i = 0; i < a.length; i++)
if (a[i] < min)
{
min = a[i];

