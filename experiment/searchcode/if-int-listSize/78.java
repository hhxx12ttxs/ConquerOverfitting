public static void main( String[] args ) {
int listSize = 100000;
int[] list = new int[listSize];
for( int i = 0; i < list.length ; i++ )
list[i] = i;

int index = search( list, 2000 );
if ( index >= 0 )

