//A.multAdd(-1, x, r.set(b));
A.mult(x, r);
r.axpy(-1.0, b);

double firstNorm2 = r.norm2();
double norm2 = 0;
for(int i=0;i<maxIter;i++) {
norm2 = r.norm2();
if((norm2<=this.epsRelIter*firstNorm2 &amp;&amp; norm2<=this.epsAbsIterMax) ||

