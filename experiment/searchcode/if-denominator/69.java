return a;
}

public void simplify(){
int gcd = gcd(numerator, denominator);
if(gcd != 1){
numerator = numerator / gcd;
denominator = denominator / gcd;
}
if (numerator > 0 &amp;&amp; denominator < 0){

