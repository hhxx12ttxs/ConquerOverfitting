* @version $Id: UnicodeEscaper.java 1552652 2013-12-20 13:23:16Z britter $
*/
public class UnicodeEscaper extends CodePointTranslator {

private final int below;
private final int above;
public static UnicodeEscaper outsideOf(final int codepointLow, final int codepointHigh) {
return new UnicodeEscaper(codepointLow, codepointHigh, false);

