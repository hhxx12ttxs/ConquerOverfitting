/**
* Fraction Class
* Lab Twelve
**/

public class Fraction
{
protected int num;
protected int den;
public Fraction divide(Fraction otherFraction) throws IllegalArgumentException
{
if (otherFraction.num == 0)
throw new IllegalArgumentException(&quot;Division by Zero&quot;);

