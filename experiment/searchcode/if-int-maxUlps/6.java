* @return true if close enough to equals
*/
public static boolean almostEquals(float A, float B, int maxUlps) {
if (Math.abs(A - B) < maxAbsoluteError)   return true;
int intDiff = Math.abs(aInt - bInt);

if (intDiff <= maxUlps)

