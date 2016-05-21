     * <p>When calculating the difference between months/days, it chooses to 
     * calculate months first. So when working out the number of months and 
     */
            int milliseconds, boolean padWithZeros) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(months), count, '0') : Integer
        int days = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH);
        int months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
                } else if (value == M) {
                int target = end.get(Calendar.YEAR);
                if (months < 0) {
                    // target is end-year -1
                            .toString(months));
     *

