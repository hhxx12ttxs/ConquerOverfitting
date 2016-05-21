    //-----------------------------------------------------------------------
 * <p>
    int getMonthOfYear(long millis) {
    //-----------------------------------------------------------------------
    int getMonthOfYear(long millis, int year) {
        long monthZeroBased = (millis - getYearMillis(year)) / MILLIS_PER_MONTH;
        return (getDayOfYear(millis) - 1) / MONTH_LENGTH + 1;
    long getYearDifference(long minuendInstant, long subtrahendInstant) {
        // optimsed implementation of getDifference, due to fixed months
        int minuendYear = getYear(minuendInstant);
 * This implementation assumes any additional days after twelve
 * months fall into a thirteenth month.
        int difference = minuendYear - subtrahendYear;
        if (minuendRem < subtrahendRem) {

