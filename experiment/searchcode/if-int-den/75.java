setNumber(Integer.toString(this.num)+&quot;/&quot;+Integer.toString(this.den));
}
public TFrac(int num, int den){
if(debug) System.out.println(&quot;Создал TFrac&quot;);
if(this.num==0){
setDen(1);
}
if(cheak()){
int gcd = gcd(Math.abs(this.num),Math.abs(this.den));

