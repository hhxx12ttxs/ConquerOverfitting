public enum RoundingMode {

/** Rounds toward zero (truncation). */
ROUND_DOWN,

/** Rounds away from zero if discarded digit is non-zero. */
* @param highPrecisionDecimalDigits precision at which the string constants mus be computed
*/
private static void computeStringConstants(final int highPrecisionDecimalDigits) {
if (sqr2String == null || sqr2String.length() < highPrecisionDecimalDigits - 3) {

