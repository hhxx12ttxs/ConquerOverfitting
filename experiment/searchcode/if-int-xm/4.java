public static double median(int x[], int y[]){
int n = x.length;
if(n==1)
return (double)(x[0]+y[0])/2.0;

int xl=0, yl=0, xh=n-1, yh=n-1;
int xm, ym;
while(){
xm = (xl+xh)/2;
ym = (yl+yh)/2;
if(x[xm] < y[ym]){

