/*compute square root by change every number into [0,2)*/
double cal_sqrt(double a){
double correction = 1.0;

if(a<0){
System.out.println(&quot;WRONG NUMBER&quot;);
double ans = 0.0, b = 0.0, correction = 1.0;
int temp = 0;
double e = Math.E;

if(a > 0){
temp = (int )(a + 0.5);

