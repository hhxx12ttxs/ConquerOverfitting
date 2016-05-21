    public long add(long instant, long months) {
        // Copied from add(long, int) and modified slightly:
            return add(instant, i_months);
        // Initially, monthToUse is zero-based
        int monthToUse = thisMonth - 1 + months;
        if (monthToUse >= 0) {
     * @see org.joda.time.DateTimeField#add
     * @see org.joda.time.ReadWritableDateTime#addMonths(int)
     * @param instant  the time instant in millis to update.
        if (i_months == months) {
        int i_months = (int)months;
     */
    public long add(long instant, int months) {
        if (months == 0) {
            return instant; // the easy case

