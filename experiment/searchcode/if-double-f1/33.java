double[] f2 = (double[]) t[1];

if(f1.length!=f2.length)
throw new Exception(&quot;Vectors have not the same size: distance not computed&quot;);
private double distance(double[] f1, double[] f2)
{
double d = 0;
for(int i=0;i<f1.length;i++)
{
double r = f1[i]-f2[i];
if(r>d) d = r;
}
return d;
}
}

