public static boolean compareFloatEquals(float expected, float actual, int maxUlps) {

int expectedBits = Float.floatToIntBits(expected) < 0
int difference = expectedBits > actualBits ? expectedBits - actualBits : actualBits - expectedBits;
if (difference > maxUlps) {

