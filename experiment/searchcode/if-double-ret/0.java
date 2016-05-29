public class Solution {
public int sqrt(int x) {
if (x == 0)
return 0;
double ret = (double)x;
while (ret * ret - x > 0.001) {
ret = (ret + (double)x / ret) / 2;
}
return (int)ret;
}
}

