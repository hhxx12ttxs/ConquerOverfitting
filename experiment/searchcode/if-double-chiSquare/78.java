double diff = supportA - N_A * P_B;
double chiSquare = diff * diff / (N_A * P_B * (1 - P_B));
if (Double.isInfinite(chiSquare) || Double.isNaN(chiSquare)) {
System.out.println(&quot;break&quot;);
}
if (diff > 0 &amp;&amp; chiSquare > minChisquare) {

