public double easeOut( double t, double b, double c, double d ) {
if ( ( t /= d ) < ( 1.0 / 2.75 ) ) {
public double easeInOut( double t, double b, double c, double d ) {
if ( t < d / 2.0 )
return easeIn( t * 2.0, 0, c, d ) * .5 + b;

