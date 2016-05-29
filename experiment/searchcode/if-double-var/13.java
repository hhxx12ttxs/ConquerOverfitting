double point = norm(normvar, (Math.random() - 0.5) * 40);
if (i % 2 == 0)
point = -point;
double[] in = new double[]{x, y, y + point};
new DiskIO().saveSampleCSV(&quot;sup-2-sines.data&quot;, data, 7);
}


static double norm(double var, double x)
{
return (Math.exp(-0.5 * (x / var) * (x / var)))

