IcoPoint()
{
x = 0;
y = 1;
z = G;
}

IcoPoint( double x0, double y0, double z0 )
{
double d = R / Math.sqrt( x0*x0 + y0*y0 + z0*z0 );
x = x0 * d;
y = y0 * d;
z = z0 * d;
}

double distance( double x0, double y0, double z0 )

