for( int j = 1; j < A.length - 1; j++ ) {
if( i == 0 || j == 0 )
A[ i ][ j ] = 0;
else
A[ i ][ j ] = random.nextInt( 99 ) + 1;
int largest = 0, colIndex = -1, rowIndex = -1;		//x, y

for( int i = 0; i < A.length; i++ ) {
if( A[ my ][ i ] >= largest ) {

