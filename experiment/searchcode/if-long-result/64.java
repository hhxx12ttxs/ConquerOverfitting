* @param b
* @return
*/
public static long exponentiation(long a, int b) {

if (b == 0) return 1;
if (b == 1) return a;

long z = exponentiation(a, b >> 1);
if ((b &amp; 1) == 0) {

