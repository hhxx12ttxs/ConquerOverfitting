public static NumericEntityEscaper between(final int codepointLow, final int codepointHigh) {
return new NumericEntityEscaper(codepointLow, codepointHigh, true);
* @return the newly created {@code NumericEntityEscaper} instance
*/
public static NumericEntityEscaper outsideOf(final int codepointLow, final int codepointHigh) {

