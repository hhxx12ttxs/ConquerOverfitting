private void simplify()
{
if (den == 0)
divby0();
long gcd = GCD(num, den);
num /= gcd;
den /= gcd;
if (den < 0)
Fraction f = (Fraction) o;
return num == f.num &amp;&amp; den == f.den;
}

@Override
public int hashCode()
{
return (int) (num ^ (num >>> 32)) * 31 + (int) (den ^ (den >>> 32));

