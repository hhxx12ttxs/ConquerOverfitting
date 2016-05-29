private int numerator;
private int denominator;

public int getNumerator() {
return numerator;
}

public void setNumerator(int fenzi) {
this.numerator = numerator;
if (denominator == 0) {
throw new Exception(&quot;fenmu can not be zero&quot;);
}
this.denominator = denominator;

