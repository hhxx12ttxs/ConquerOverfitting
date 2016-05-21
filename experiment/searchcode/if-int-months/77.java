         *         if the string expression cannot be parsed into a valid 
         *         <CODE>CronExpression<\/CODE>
                }
                if (months == null) {
                    months = new TreeSet<Integer>();
                }
                    int val = daysOfMonth.last().intValue();
                    if (val == NO_SPEC_INT) {
                        throw new ParseException(
        protected transient TreeSet<Integer> daysOfMonth;
        protected transient TreeSet<Integer> months;
        protected transient TreeSet<Integer> daysOfWeek;
        /**
         * Indicates whether the specified cron expression can be parsed into a 
         * valid cron expression
         * @throws java.text.ParseException

