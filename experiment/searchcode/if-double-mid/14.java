System.out.println(&quot;sqrt of 25: &quot;+findSquareRoot(25));
}


public static double squareRoot(int num){
double target = (double)num;
double mid = (double)num/2;
double start = 0.0;
double end = target;
while(!((mid * mid) <= target + 0.001 &amp;&amp; (mid * mid) >= target - 0.001)){

