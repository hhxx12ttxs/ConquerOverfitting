for (int i = 0; i < p1.length; i++) {
// if p1[i]==0 or p2[i]==0 it doesn&#39;t use
if (p1[i] != -1 &amp;&amp;  p2[i] != -1 ) {//teste &amp;&amp;
sum += FastMath.pow((p1[i] - p2[i]),2);
//sumP2 += FastMath.pow(p2[i], 2);
qtderates++;
}
}
if(qtderates==0){//trocar por -1
return(Double.MAX_VALUE);

