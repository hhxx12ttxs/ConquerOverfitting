while((int)min < (int)max){
double mid = (min + max)/2;
if(mid*mid == x)
return (int)mid;
else if(mid*mid < x)
min = mid;
else
max = mid;

