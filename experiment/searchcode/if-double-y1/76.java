private static int detectarQuadrante(double x1, double y1, double x2, double y2 )
{
if ( ( x2 > x1 &amp;&amp; y2 > y1 ) ||
( x2 > x1 &amp;&amp; y2 == y1 ) ||
int incr = calcularIncrementoAngulo( x1, y1, x2, y2 );
double ang = Math.toDegrees( Math.atan2( y, x ) );

if ( incr == 90 || incr == 270 )

