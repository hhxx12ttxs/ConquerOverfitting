//  {  ZERO = 0.0D; } //PARAMETER          ( ZERO = 0.0D0 )
//*     ..
//*     .. Local Scalars ..
int J4, J4P2; //INTEGER            J4, J4P2
double D, EMIN, SAFMIN, TEMP; //DOUBLE PRECISION   D, EMIN, SAFMIN, TEMP
D = Z[ J4 -1]; //D = Z( J4 )
DMIN[0] = D; //DMIN = D
if ( PP == 0 ) { //IF( PP.EQ.0 ) THEN
for (J4 = 4*I0; J4 <= 4*( N0-3 ); J4 += 4) { //DO 10 J4 = 4*I0, 4*( N0-3 ), 4

