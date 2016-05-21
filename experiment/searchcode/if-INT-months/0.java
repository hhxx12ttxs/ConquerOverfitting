     * Creates a <code>Months<\/code> representing the number of whole months
    /** Constant representing the maximum number of months that can be stored in this object. */
    public static final Months MAX_VALUE = new Months(Integer.MAX_VALUE);
    /** Constant representing the minimum number of months that can be stored in this object. */
    public static final Months MIN_VALUE = new Months(Integer.MIN_VALUE);
        return Months.months(amount);
            int months = chrono.months().getDifference(
     * savings time changes that may occur during the interval.
     * in the specified interval. This method corectly handles any daylight
    public static Months monthsBetween(ReadableInstant start, ReadableInstant end) {
     */
    public static Months months(int months) {
        switch (months) {
            Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
        int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.months());
                    ((LocalDate) end).getLocalMillis(), ((LocalDate) start).getLocalMillis());

