double wa1[], double wa2[])
{
int i,iter,j,jm1,jp1,k,l,nsing;             //had ij, jj
double dxnorm,fp,gnorm,parc,parl,paru;
* rank-deficient, obtain a least squares solution.
*/
nsing = n;
//jj = 0;
for (j=0; j<n; j++) {
wa1[j] = qtb[j];
if ((r[j][j] == 0.0) &amp;&amp; (nsing == n))

