double var_G = ( G / 255.0 );        //G from 0 to 255
double var_B = ( B / 255.0 );        //B from 0 to 255

if ( var_R > 0.04045 ) var_R = Math.pow( ( var_R + 0.055 ) / 1.055, 2.4);
double var_V = ( 9 * Y ) / ( X + ( 15 * Y ) + ( 3 * Z ) );

if (Double.isNaN(var_U)) {
var_U = 0;
}
if (Double.isNaN(var_V)) {
var_V = 0;

