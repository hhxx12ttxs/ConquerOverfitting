*  Given an integer, write a function to determine if it is a power of two.
* @author VictorQian
*
*/
public class PowerOfTwo {
public boolean isPowerOfTwo(int n) {
//one line solution.
return n > 0 &amp;&amp; (n&amp;(n-1)) == 0;
//		if (n < 0)
//			return false;
//		int count = 0;

