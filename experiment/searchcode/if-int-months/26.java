     * <p>
     * 
     */
    public MonthDay plusMonths(int months) {
        return withFieldAdded(DurationFieldType.months(), months);
     * like {@link #plusMonths(int)}.
     * Adding one field is best achieved using methods
     * like {@link #withFieldAdded(DurationFieldType, int)}
     * or {@link #plusMonths(int)}.
     * 
     * 
     * like {@link #minusMonths(int)}.
     * Subtracting one field is best achieved using methods
    /**
     * Returns a copy of this month-day plus the specified number of months.

