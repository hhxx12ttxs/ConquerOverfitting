northing = Double.parseDouble( utm[3] );
}
else
{
if ( utm.length == 3 )
double t0 = Math.pow(Math.tan(phi1), 2);
double Q0 = e1sq * Math.pow(Math.cos(phi1), 2);
fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq) * Math.pow(dd0, 4)

