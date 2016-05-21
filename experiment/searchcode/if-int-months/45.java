        // Find nearest Thursday (defines the week in ISO 8601).
        int thursdayDiff = 4 - dayOfWeek;
        Date nearestThursday = new Date(inputDate.getTime() + thursdayDiff * MILLIS_IN_A_DAY);
/**
 * Adds or subtracts the specified amount of months for the given Date.
 * 
   @SuppressWarnings(value = \"deprecation\")
   public static int differenceInDays(Date endDate, Date startDate) {
      int difference = 0;
      if (!areOnTheSameDay(endDate, startDate)) {
   @SuppressWarnings(\"deprecation\")
   public static Date shiftDate(Date date, int shift) {
      Date result = (Date) date.clone();
         differenceDouble = Math.max(1.0D, differenceDouble);
         difference = (int) differenceDouble;
      }

