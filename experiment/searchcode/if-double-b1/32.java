double v1 = p1.getValue(i);
double v2 = p2.getValue(i);
double diff = Math.abs(v2 - v1);
if (diff > max)
max = diff;
System.out.printf(&quot;%.6f\n&quot;, max - min);
}
in.close();
}

private static class Poly
{
double b0;
double b1;
double b2;
double b3;

