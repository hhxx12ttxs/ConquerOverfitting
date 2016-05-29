if(A.length == 0) return 0;
if(A.length == 1) return 1;

int p1 = 0;
int p2 = 0;
int num_du = 0;
int if_du = 0;

while(p2<A.length-1) {

if(A[p2] == A[p2+1] &amp;&amp; if_du == 1) {
num_du ++;
p2++;

