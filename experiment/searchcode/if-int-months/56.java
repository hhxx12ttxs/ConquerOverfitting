            return XInteger.valueOf(minutes);
        }
    /**
     * fn:months-from-duration($arg as xdt:yearMonthDuration?) as xs:integer?.
     */
    public static final class MonthsFromDuration extends ExtractFromDuration {
        private static final long serialVersionUID = 5561751943292591702L;
        public static final String SYMBOL = \"fn:months-from-duration\";
        public MonthsFromDuration() {
            super(SYMBOL, TypeRegistry.safeGet(\"xs:integer?\"));
        public XInteger extract(DurationValue arg) {
            final int months = arg.getMonths();
            return XInteger.valueOf(months);
        }
            final int minutes = arg.getMinutes();

