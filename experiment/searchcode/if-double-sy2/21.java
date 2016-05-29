private void checkSpace()
{
if ( n+1 == x.length )
{
double[] h;
h = new double[x.length * 2];
System.arraycopy(x, 0, h, 0, n);
double xm = getAvgX();
double ym = getAvgY();
double sxy = 0.0, sx2 = 0.0, sy2 = 0.0;
for(int i = 0; i < n; i++) {

