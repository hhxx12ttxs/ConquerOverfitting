y1 = Y[0] + D[0];
z0 = Z[0];
z1 = Z[0] + D[0];

for ( int i = 1; i < N; i++ ) {
if ( X[i] + D[i] <= x0 || X[i] >= x1 ) {
if ( Z[i] + D[i] <= z1 )
z1 = Z[i] + D[i];
if ( Z[i] >= z0)
z0 = Z[i];
}

}
int dx = x1 - x0;

