public class NumericEntityEscaper extends CodePointTranslator {

private final int below;
private final int above;
public static NumericEntityEscaper between(final int codepointLow, final int codepointHigh) {
return new NumericEntityEscaper(codepointLow, codepointHigh, true);

