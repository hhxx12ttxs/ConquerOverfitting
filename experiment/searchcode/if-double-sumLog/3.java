* WARNING: This cache will grow forever
*/
double sumLog[] = { 0.0 };

public static Binomial get() {
if (binomial == null) binomial = new Binomial();
* @return Sum_{i \in 1..n}[ log(i) ]
*/
double sumLog(int n) {
// Not in the array? => update size
if (n >= sumLog.length) newSumLog(n);

