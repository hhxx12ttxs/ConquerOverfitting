for (k = 0; k < n; ++k)
{
Fraction maxp = new Fraction();
for (i = k; i < n; ++i)
if (a[i][k].abs().Biger(maxp.abs()))
{
row = i;
maxp = new Fraction(a[i][k]);
}
if (maxp.abs().zero() == 0)

