public int lastDigit(int N) {
double r,r1;
@SuppressWarnings(&quot;unused&quot;)
int [] num = new int[1000001];
double m = 0;
int x=0;
for(double n=1.0;n<N+1;++n)
{ r1 = (1.0 + n)*Math.log10(n);
//  r1 %=1;
//m= (int)r1/1;

