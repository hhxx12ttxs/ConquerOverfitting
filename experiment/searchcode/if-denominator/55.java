public Fraction(int numerator, int denominator)
{
numeratorV = numerator;
denominatorV = denominator;
if (denominatorV == 0)
{
throw new IllegalArgumentException(&quot;Denominator is zero.&quot;);
}
else if (denominatorV < 0)

