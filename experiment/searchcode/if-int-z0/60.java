float val;
int x0,y0,z0;

// if out of boundary, replace all with zero
if ( (x<0) || (x>nx-1) || (y<0) || (y>ny-1) || (z<0) || (z>nz-1) )
public static float nearestNeighborInterpolation(float[][][] image, float zero, float x, float y, float z, int nx, int ny, int nz) {
int x0,y0,z0;

// if out of boundary, replace all with zero
if ( (x<0) || (x>nx-1) || (y<0) || (y>ny-1) || (z<0) || (z>nz-1) )

