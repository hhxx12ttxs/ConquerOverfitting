public int helper(int target, double start, double end, double diff){
double mid = (start + end) / 2;
if((mid*mid - target) > 0 &amp;&amp; (mid * mid - target) < diff)
return (int)mid;
if(mid * mid > target)

