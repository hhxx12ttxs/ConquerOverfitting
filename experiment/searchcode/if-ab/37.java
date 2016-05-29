for( int i = 0; i < n; i++ )
{
ab[i][0] = in.nextInt();
ab[i][1] = in.nextInt();
if( ab[i][0]>ab[i][1] )
ab[i][0]^=ab[i][1];
}
}
int min = -1, max = 1001;
for( int i = 0; i < n; i++ )
{
if( ab[i][0]>min ) min = ab[i][0];

