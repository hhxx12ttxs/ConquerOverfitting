int n = c.length, ans = 0;
boolean g[][] = new boolean[n][n], seen[] = new boolean[n];
for( int i = 0; i < n; i++ ) for( int j = 0; j < n; j++ ) g[i][j] = G[i].charAt(j)==&#39;Y&#39;;
for( int i = 0; i < n; i++ ) if( !seen[i] )
{
seen[i] = true;
int best = c[i];
for( int j = 0; j < n; j++ ) if( g[i][j] &amp;&amp; g[j][i] )

