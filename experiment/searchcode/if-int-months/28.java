     * Similarly, a day can vary at Daylight Savings cutover, typically between
 * This class should generally not be used directly by API users.
 * The {@link ReadablePeriod} interface should be used when different 
 * kinds of period objects are to be referenced.
     * @param years  amount of years in this period, which must be zero if unsupported
     * @param months  amount of months in this period, which must be zero if unsupported
     * @param weeks  amount of weeks in this period, which must be zero if unsupported
     * For example, a period of 1 month could vary between the equivalent of
     * 28 and 31 days in milliseconds due to different length months.
     */
        iType = type;
        iValues = setPeriodInternal(years, months, weeks, days, hours, minutes, seconds, millis); // internal method
    }

