if(t.length!=2) throw new Exception(&quot;wrong data number: &quot;+t.length);

double[] f1 = (double[]) t[0];
double[] f2 = (double[]) t[1];

if(f1.length!=f2.length)
throw new Exception(&quot;Vectors have not the same size: distance not computed&quot;);

