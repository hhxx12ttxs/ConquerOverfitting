return (dv0 < dv1 ? dv0 : dv1);
}

private double calcv0(double[][] a, double[][] b){
double[][] r1 = Util.alignToX(a);
double d0 = getNaiveDistance(r1,r2);
double d1 = getNaiveDistance(r1,r2rot);

//System.out.println(&quot;STD.calcv0 :: d0=&quot; + Util.dts(d0) + &quot;, d1=&quot; + Util.dts(d1) );

