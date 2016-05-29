Fraction(int numumerator, int denominator) {
if (denominator == 0) {
throw new ArithmeticException(&quot;Denominator Cannot Be 0.&quot;);
}
if (denominator < 0){
numerator *= -1;
denominator *= -1;
}

this.numerator = numumerator;

