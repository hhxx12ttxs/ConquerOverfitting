public static UnicodeEscaper outsideOf(int codepointLow, int codepointHigh) {
UnicodeEscaper escaper = new UnicodeEscaper(codepointLow, codepointHigh, false);
* @param codepointHigh below which to escape
*/
public static UnicodeEscaper between(int codepointLow, int codepointHigh) {

