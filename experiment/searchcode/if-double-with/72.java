boolean weighted;
double[] weights;
public AnomalousDiffusionWithOffset(double[] initial, boolean weighted, double[] weights)
for( int i = 0; i < npts; i++ )
{
x[i][0] = (double)i;
y[i] = val(x[i], a);
if(weighted) s[i] = weights[i];

