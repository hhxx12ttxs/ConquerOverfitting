while(high <= low){
int mid=(low+high)/2;
if(mid*mid < n)
low=mid;
else if(mid*mid > n)
high=mid;
else
return mid;
}


return low;
}

public static double sqrt2(int n) {

