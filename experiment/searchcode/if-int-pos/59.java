void build( int[] a, int l, int r, int pos){
if ( l ==  r) {
tree[pos] = a[l];
return;
if ( posR < l || posL > r)
return 0;
if ( l <= posL &amp;&amp; r >= posR)
return tree[pos];
int mid = (posR + posL)/2;

