public double[] end = new double[2];

public double[] center = new double[2];
public double[] f1 = new double[2];
public double[] f2 = new double[2];
c = Math.sqrt(a * a - b * b);//a*a=b*b+c*c

if (t1 >= t2) {// 椭圆焦点在同一经度上
f1[0] = center[0] + c;
f2[0] = center[0] - c;

