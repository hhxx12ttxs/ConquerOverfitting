public static NumericEntityEscaper between(final int codepointLow, final int codepointHigh) {
return new NumericEntityEscaper(codepointLow, codepointHigh, true);

