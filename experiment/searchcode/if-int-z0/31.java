IcoPoint()
{
x = 0;
y = 1;
z = G;
}

IcoPoint( double x0, double y0, double z0 )
{
double d = R / Math.sqrt( x0*x0 + y0*y0 + z0*z0 );
/** interpolate between two IcoPoints
*/
static IcoPoint interpolate( IcoPoint p1, IcoPoint p2, int i, int n )
{
if ( i < 0 || i > n ) return null;

