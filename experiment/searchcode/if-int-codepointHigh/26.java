* @return the newly created {@code UnicodeEscaper} instance
*/
public static UnicodeEscaper outsideOf(final int codepointLow, final int codepointHigh) {
return new UnicodeEscaper(codepointLow, codepointHigh, false);

