public Plane (double a, double b, double c){

this(
new double[][] {{a},{b},{c},{1}}
);

//		double[][] tmp1 = new double[4][1];
throw new RuntimeException(&quot;このコンストラクタは3次元以外で平面を定義できません&quot;);
}
double[][] tmp1 = new double[3][1];
tmp1[0][0] = source[0][0];
tmp1[1][0] = source[1][0];

