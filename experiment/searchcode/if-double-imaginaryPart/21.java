private double ImaginaryPart,sum;
public double p,q,r,s;
public int t;

void Addition(){
realPart=(p+r);
realPart=(p-r);
ImaginaryPart=(q-s);
if((p>r)&amp;&amp;(q>s))
System.out.println(StrictMath.round(realPart)+&quot;+&quot;+StrictMath.round(ImaginaryPart)+&quot;i&quot;);

