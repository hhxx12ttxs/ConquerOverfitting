* 2. mid * mid may result in integer overflow. need to either cast it to long/double, or need to do mid < x / mid;
* @param args
*/
public int sqrt(int x) {
if (x < 0) return -1;
if (x <= 1) return x;

