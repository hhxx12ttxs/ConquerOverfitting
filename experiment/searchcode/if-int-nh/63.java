super();
}

/**
* From Numerical Recipes, but arrays are zero-based here.
*/
public void filter(float[] a, int n, int isign, float[] wksp) {
// Note the trailing &#39;&#39;-1&#39;&#39; in all subindexing expressions.
//
int nh, nh1, i, j;

if (n < 4)
return;

nh1 = (nh = n >> 1) + 1;
if (isign >= 0) {

