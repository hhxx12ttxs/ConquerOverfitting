Knuth Vol 2, p 131
*/
int a, b ;
double x ;
if (p>=1) return n ;
if (p<=0) return 0 ;
if (n<=0) return 0 ;
return ranb1(n,p) ;  /** small case */
}

a  = 1 + n/2 ;
b  = n + 1 - a ;
x = ranbeta((double) a, (double) b) ;
if (x>=p) return ranbinom(a-1, p/x) ;

