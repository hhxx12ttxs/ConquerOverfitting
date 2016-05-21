    /**
 * This class does not represent a day, but the millisecond instant at midnight.
 * If you need a class that represents the whole day, then an {@link Interval} or
 * a {@link LocalDate} may be more suitable.
     * Returns a copy of this date plus the specified number of months.
    /**
     * Returns a copy of this date minus the specified number of months.
     */
    public DateMidnight plusMonths(int months) {
        if (months == 0) {
     * <p>
            return this;
     * <p>
     */
    public DateMidnight minusMonths(int months) {
        if (months == 0) {

