public boolean almostEqualRelativeOrAbsolute(double[] A, double[] B)
{
for (int i = 0; i < A.length; i++)
if (!almostEqualRelativeOrAbsolute(A[i], B[i], maxRelativeError, maxAbsoluteError))
public boolean almostEqualComplement(double[] A, double[] B)
{
for (int i = 0; i < A.length; i++)
if (!almostEqualComplement(A[i], B[i], maxUlps, maxAbsoluteError))

