//          0     1      2      3         n-2    n-1
//-------------------------------------------------------------------
void sum()
{
int n = rhsSize();
double s = (Double)rhs(1).get();
if (!rhs(0).isEmpty()) s = -s;
for (int i=2;i<n;i+=2)
{
if (!rhs(i-1).isEmpty() &amp;&amp; rhs(i-1).charAt(0)==&#39;/&#39;)
s /= (Double)rhs(i).get();

