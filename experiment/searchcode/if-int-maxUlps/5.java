public static boolean almostEqual2sComplement(float A, float B, int maxUlps){

// Make sure maxUlps is non-negative and small enough that the
int intDiff = Math.abs(aInt - bInt);

if (intDiff <= maxUlps)
return true;

return false;

