private int den;

public Fraction() {
}

public Fraction(int num, int den) {
this.num = num;
return num + &quot;/&quot; + den;
}

public double findGcn(int num, int den){
if (num < 0) num = -num;
if (den < 0) den = -den;

