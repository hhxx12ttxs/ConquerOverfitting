public static UnicodeEscaper outsideOf(final int codepointLow, final int codepointHigh) {
return new UnicodeEscaper(codepointLow, codepointHigh, false);
}

public static UnicodeEscaper between(final int codepointLow, final int codepointHigh) {

