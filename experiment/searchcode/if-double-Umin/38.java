// Get parameters intervals
double[] paramBounds=new double[4];
geom.bounds(paramBounds);
double umin=paramBounds[0];
// Handle the case of infinite geometry
if(!geom.isUClosed())
{
umin=0;
umax=1;
}

if(!geom.isVClosed())

