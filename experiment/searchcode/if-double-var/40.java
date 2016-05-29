double var_Y = Y / ref_Y;          //ref_Y = 100.000
double var_Z = Z / ref_Z;          //ref_Z = 108.883
if ( var_X > 0.008856 )	{
var_X = ( 7.787 * var_X ) + ( (double)16 / 116 );
}

if ( var_Y > 0.008856 ) {
var_Y = Math.pow(var_Y, (double)1/3);

