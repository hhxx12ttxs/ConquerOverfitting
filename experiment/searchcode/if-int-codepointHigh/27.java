* @return the newly created {@code UnicodeEscaper} instance
*/
public static UnicodeEscaper outsideOf(int codepointLow, int codepointHigh) {
return new UnicodeEscaper(codepointLow, codepointHigh, false);
public static UnicodeEscaper between(int codepointLow, int codepointHigh) {
return new UnicodeEscaper(codepointLow, codepointHigh, true);

