double start = 0.0;
double end = x;
double mid = 0.0;
double res = Math.sqrt(x);
System.out.println(res);
while(start < end){
mid = start + (end-start) / 2;
if((end - start) / 2 < epsilon){
break;

