public static double LogDiff(double lna, double lnc){
if (lna > lnc)
return lna + Math.exp(1.0 - Math.exp(lnc - lna));
* @param lnc Value.
* @return Result.
*/
public static double LogSum(double lna, double lnc){
if (lna == Double.NEGATIVE_INFINITY)

