return (double)this.numerator/(double)this.denominator == (double)B.numerator/(double)B.denominator;
}

public static int gcd(int p, int q) {
if (q==0) return p;
int r = p % q;
return gcd(q,r);
}

public String toString(){
if(this.denominator < 0){
return -this.numerator + &quot;/&quot; + -this.denominator;

