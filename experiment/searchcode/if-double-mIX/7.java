public static Vector diff(Vector a, Vector b)
{
return mix(a, b, 1, -1);
}

public static Vector mix(Vector a, Vector b, double afac, double bfac)
protected static double[] doMix(double[] a, double[] b, double afac, double bfac)
{
if (a.length != b.length)

