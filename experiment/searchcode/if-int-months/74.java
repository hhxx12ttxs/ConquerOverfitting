     * @param mm month 1 to 12 (not 0 to 11 as in Sun's Date), no lead 0.
        int ageInYears = asOfYYYY - birthYYYY;
        int ageInMonths = asOfMM - birthMM;
        int ageInDays = asOfDD - birthDD;
     * @param leap true if you are interested in a leap year
     *            result. Usually asOf > birthDate. Difference is always positive no matter if asOf is > or < birthDate
     * @return array of three ints (not Integers). [0]=age in years, [1]=age in months, [2]=age in days.
     * @see #localToday
     * @return how many days are in that month
        if (ageInMonths < 0) {
            ageInMonths += 12;
 * term storage should store the ordinal either as an int, or possibly as a short. The BigDate constructor stores the
 * date both in ordinal and Gregorian forms internally. If you store one, it creates the other.
 * <p/>

