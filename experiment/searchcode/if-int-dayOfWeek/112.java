package com.cyberaka.visualaccounts.common;

/**
 * Title:        Visual Accounts
 * Description:  Accounting Software
 * Copyright:    Copyright (c) 2003
 * Company:      Computer Care
 * @author Abhinav Anand
 * @version 1.0
 */

import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * The DateUtilities class is the public class which handles all the date
 * operations for the Fly by Nights Reservations System.
 *
 */
public class DateUtilities {

    public final static int BEFORE = -1;
    public final static int EQUAL = 0;
    public final static int AFTER = 1;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    public final static int MONTH_TYPE = 1;
    public final static int DAYS_TYPE = 2;
    public final static int[] MONTH_DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    public final static String[] WEEK_DAYS = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
    public final static String[] MONTH_NAMES = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    /**
     * This method takes two date as argument and compares first date
     * with the second date, and returns the result of comparison.
     *
     * @param date1 The first date to compare.
     * @param date2 The second date to compare.
     * @return int  0 if date1 = date2,
     *              1 if  date1 > date2,
     *             -1 if date1 < date2
     */
    public static int compareDates(java.util.Date date1, java.util.Date date2) {
        int compareStatus = 0;
        // Initialize the calendar variables.
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        int month1 = cal1.get(Calendar.MONTH);
        int month2 = cal2.get(Calendar.MONTH);

        int day1 = cal1.get(Calendar.DAY_OF_MONTH);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);

        if (year1 == year2) { // 2002 = 2002
            if (month1 == month2) { // 1 = 1
                if (day1 == day2) { // 15 = 15
                    compareStatus = EQUAL;
                } else { // 16 != 15
                    if (day1 < day2) { // 14 < 15
                        compareStatus = BEFORE;
                    } else { // 15 > 14
                        compareStatus = AFTER;
                    }
                }
            } else { // 1 != 2
                if (month1 < month2) { // 1 < 2
                    compareStatus = BEFORE;
                } else { // 2 > 1
                    compareStatus = AFTER;
                }
            }
        } else { // 2002 != 2003
            if (year1 < year2) { // 2002 < 2003
                compareStatus = BEFORE;
            } else { // 2003 > 2002
                compareStatus = AFTER;
            }
        }

        return compareStatus;
    }

    /**
    * Method to get the "Day" corresponding to any "Date".
    *
    * @param dateString: The date as a string whose Day is to be worked out.
    * @return String: The day of the date
    */
    public static String getDayOfDate(String dateString) throws Exception {
        Calendar calendar = Calendar.getInstance();
        String day = null;
        Date date = null;

        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException pe) {
            throw new Exception("getDayOfDate: Wrong format of date");
        }
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
        case Calendar.SUNDAY:
            day = "Sun";
            break;
        case Calendar.MONDAY:
            day = "Mon";
            break;
        case Calendar.TUESDAY:
            day = "Tue";
            break;
        case Calendar.WEDNESDAY:
            day = "Wed";
            break;
        case Calendar.THURSDAY:
            day = "Thu";
            break;
        case Calendar.FRIDAY:
            day = "Fri";
            break;
        case Calendar.SATURDAY:
            day = "Sat";
            break;
        }

        return day;
    }

    /**
    * The method to validate the date against a certian format.
    *
    * @param String date : The date to be validated
    * @return boolean returns true if date is valid, false if date is invalid.
    */
    public static boolean validateDate(String date) {
        StringTokenizer tokenizer = new StringTokenizer(date, "/");
        try {
            int days = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int year = Integer.parseInt(tokenizer.nextToken());

            // Make sure that the month number is valid
            if (!(month >= 1 && month <= 12)) {
                return false;
            }

            // Make sure that the year is valid
            if (!(year >= 1900 && year <= 9999)) {
                return false;
            }

            // If the month is february and the year is a leap then month capacity = 29
            int monthCapacity = MONTH_DAYS[month - 1];
            if (month == 2 && isYearLeap(year)) { // if the month is february and a leap year
                monthCapacity = 29;
                //MessageBox.showMessage(year + " is leap, so the month number " + month + " has the capacity " + monthCapacity);
            }
            if (!(days <= monthCapacity && days >= 1)) {
                return false;
            }

            // Now just to be sure, we will create a date instance
            Date givenDate = null;

            try {
                givenDate = dateFormat.parse(date);
            } catch (ParseException pe) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the date equivalent of a date string
     */
    public static Date toDate(String date) throws ParseException {
        Date givenDate = null;
        givenDate = dateFormat.parse(date);
        return givenDate;
    }

    /**
     * Returns the date equivalent of a date string in the java.sql.Date format
     */
    public static java.sql.Date toSQLDate(String date) throws ParseException {
        Date givenDate = null;
        givenDate = dateFormat.parse(date);
        return new java.sql.Date(givenDate.getTime());
    }

    /**
     * Returns a calendar instance of a date
     */
    public static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * Returns the java.sql.date instance for the given calendar.
     */
    public static java.sql.Date toSQLDate(Calendar cal) {
        if (cal == null) {
            return null;
        }
        java.sql.Date date = new java.sql.Date(cal.getTime().getTime());
        return date;
    }

    /**
     * Returns the string equivalent of a date
     */
    public static String toString(Date date) {
        if (date == null) {
            return "xx/xx/xxxx";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int days = cal.get(Calendar.DAY_OF_MONTH);
        String str = ((days < 10 ? "0" : "") + days) + "/" + ((month < 10 ? "0" : "") + month) + "/" + year;
        return str;
    }

    public static java.util.GregorianCalendar toGregorianCalendar(java.sql.Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date.getTime()));
        java.util.GregorianCalendar gcal = new java.util.GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        return gcal;
    }

    /**
     * Returns the accounting date
     */
    public static java.sql.Date getAccountsStartingDate() {
        Calendar cal = DateUtilities.toCalendar(new java.util.Date());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, Calendar.APRIL);
        return toSQLDate(cal);
    }

    /**
     * Returns the current Date
     */
    public static java.sql.Date getCurrentDate() {
        Calendar cal = DateUtilities.toCalendar(new java.util.Date());
        return toSQLDate(cal);
    }

    /**
    * Find the list of all dates in the next week inclusive of TODAY
    *
    * @return String[] : The list of dates of next week.
    */
    public static String[] nextWeekDates() {
        String[] dateList = new String[7]; //List of Dates in for the
                                           //next week,inclusive today

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int index = 0;
        String date = null;

        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + i);
            date = dateFormat.format(calendar.getTime());
            dateList[index] = date;
            index++;
        }
        return dateList;
    }

    /*
    * The static method to get the "Date" corresponding to the "Day".
    * This date is within the next one week.
    * The calculation of date is done everytime the method is called.
    * This is because, the client program may run over 24 hours, in which
    * case the recalculation of dates is needed.
    *
    * @param day : The day of the week whose date in the next
    *               week is to be calculated.
    * @return  String : The date of the day in the next week.
    */
    public static String getDateForDay(String day) {
        String[] dateList = new String[7]; // List of Dates in for the next
                                           // week,inclusive today

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int index = 0;
        String date = null;

        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + i);
            date = dateFormat.format(calendar.getTime());
            index = (dayOfWeek + i - 1) % 7;
            dateList[index] = date;
        }

        if (day.equalsIgnoreCase("Sun"))
            date = dateList[0];
        else if (day.equalsIgnoreCase("Mon"))
            date = dateList[1];
        else if (day.equalsIgnoreCase("Tue"))
            date = dateList[2];
        else if (day.equalsIgnoreCase("Wed"))
            date = dateList[3];
        else if (day.equalsIgnoreCase("Thu"))
            date = dateList[4];
        else if (day.equalsIgnoreCase("Fri"))
            date = dateList[5];
        else if (day.equalsIgnoreCase("Sat")) //Not needed
            date = dateList[6];

        return date;

    }

    /**
     *  Checks the date for being before/equal to/after the current date.
     *
     * @parameter String date to be compared
     * @return int A value 1 for date after current date
     *             A value -1 for date before current date
     *             A value 0 for date equal to current date
     * @exception InvalidDateException If the date parameter is invalid
     */
    public static int compareToCurrentDate(String date) throws Exception {
        Date givenDate = null;
        Date today = new Date();
        String todayString = dateFormat.format(today);
        int returnValue = 0;

        try {
            givenDate = dateFormat.parse(date);
            today = dateFormat.parse(todayString);
        } catch (ParseException pe) {
            throw new Exception(pe.getMessage());
        }

        if (givenDate.before(today)) {
            returnValue = -1;
        } else if (givenDate.after(today)) {
            returnValue = 1;
        } else if (givenDate.equals(today)) {
            returnValue = 0;
        }
        return returnValue;
    }

    /**
     * Returns current date and time as a string
     *
     * @return String - current date
     */
    public static String getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        //		String todayString = cal.getTime().toString();
        //		String am_pm = cal.get(Calendar.AM_PM)==Calendar.AM? " AM": " PM";
        //		String hour = "" + cal.get(Calendar.HOUR);
        //		String minutes = "" + cal.get(Calendar.MINUTE);
        //		String seconds = "" + cal.get(Calendar.SECOND);
        //		String year = "" + cal.get(Calendar.YEAR);
        String todayString = WEEK_DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", " + MONTH_NAMES[cal.get(Calendar.MONTH)] + " "
                + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ", " + cal.get(Calendar.HOUR) + ":"
                + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + (cal.get(Calendar.AM_PM) == Calendar.AM ? " AM" : " PM");

        //		String month = "" + cal.get(Calendar.MONTH);
        //		String day = "" + "";
        //		String dayChar = WEEK_DAYS[cal.get(Calendar.DAY_OF_WEEK)];
        return todayString;
    }

    /**
     * Returns current time in milliseconds
     */
    public static long getTimeInMilliseconds() {
        Date date = new Date();
        return date.getTime();
    }

    /**
     * Method to find out the total number of days difference between two
     * given date.
     */
    public static int getDateDifference(Date date1, Date date2) {
        int totalDifference = 0;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH);
        int days1 = cal1.get(Calendar.DAY_OF_MONTH);
        //System.out.println(days1 + "/" + month1 + "/" + year1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONDAY);
        int days2 = cal2.get(Calendar.DAY_OF_MONTH);

        // Calculate the total number of days
        int j = month1;

        //System.out.println(days2 + "/" + month2 + "/" + year2);
        if (year1 == year2 & month1 == month2) {
            totalDifference = days2 - days1;
        } else {
            outer: for (int i = year1; i <= year2; i++) {
                for (; j <= 11; j++) {
                    //System.out.print(totalDifference + "  ");
                    //System.out.println(i + "/" + j);
                    if (i == year1 && j == month1) {
                        totalDifference += MONTH_DAYS[j] - days1;
                        continue;
                    }
                    if (i == year2 && j == month2) {
                        totalDifference += days2;
                        break outer;
                    }

                    // Check if the month being processed is february or not.
                    totalDifference += MONTH_DAYS[j];
                    if (isYearLeap(i)) {
                        totalDifference++;
                    }
                }
                j = 0;
            }
        }
        return totalDifference;
    }

    /**
     * Method to find out the resultant date after adding a given number of
     * days/month/year to a given date.
     */
    public static Date addToDate(Date baseDate, int toAdd) {
        return addToDate(baseDate, toAdd, DAYS_TYPE);
    }

    public static Date addToDate(Date baseDate, int toAdd, int addType) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int days = cal.get(Calendar.DAY_OF_MONTH);
        //System.out.println(days + "/" + month + "/" + year);

        int totalNumberOfDays = 0;
        if (addType == MONTH_TYPE) {
            toAdd = toAdd--;
            int yearNumber = year;
            for (int i = 0; i < toAdd; i++) {
                int monthNumber = month + i;

                if (mod(monthNumber, 12) == 0) {
                    yearNumber++;
                }
                if (monthNumber > 11) {
                    monthNumber = mod(monthNumber, 12);
                }

                int monthCapacity = MONTH_DAYS[monthNumber];
                if (monthNumber == 1 & isYearLeap(yearNumber)) { // If the month is february, check for leap
                    monthCapacity++;
                }
                totalNumberOfDays += monthCapacity;
                //System.out.println("i [" + i + "],\tm.number [" + (month+i) + "],\tmonth,year [" + monthNumber +"," + yearNumber + "],\tdays [" + monthCapacity + "], total days [" + totalNumberOfDays + "]");
            }
            //System.out.println("Total number of days = " + totalNumberOfDays);
        } else {
            totalNumberOfDays = toAdd;
        }

        // Loop until the value of totalNumberOfDays becomes 0
        while (totalNumberOfDays > 0) {
            // If the total number of days is greater than the present months
            // limit then add the total number of days left in days and substract
            // the same from totalNumberOfDays.

            int monthCapacity = MONTH_DAYS[month];
            if (month == 1 & isYearLeap(year)) { // If the month is february, check for leap
                monthCapacity++;
            }

            int monthDaysLeft = monthCapacity - days;
            int monthDaysToSubstract = 0;
            if (totalNumberOfDays >= monthDaysLeft) {
                monthDaysToSubstract = monthDaysLeft;
            } else if (totalNumberOfDays < monthDaysLeft) {
                monthDaysToSubstract = totalNumberOfDays;
            }
            days += monthDaysToSubstract;
            totalNumberOfDays -= monthDaysToSubstract;

            // If the total number of days exceeds the month limit.
            if (days >= MONTH_DAYS[month] & totalNumberOfDays > 0) {
                days = 0;
                month++;
                if (month > 11) {
                    year++;
                    month = 0;
                }
            }
        }

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * Method to find out the resultant date after substracting a given number of
     * days/month/year to a given date.
     */
    public static Date substractFromDate(Date baseDate, int toSubstract) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int days = cal.get(Calendar.DAY_OF_MONTH);
        //System.out.println(days + "/" + month + "/" + year);

        int totalNumberOfDays = toSubstract;

        // Loop until the value of totalNumberOfDays becomes 0
        while (totalNumberOfDays > 0) {
            // If the total number of days is more than the days in this month
            // then substract the days from the month and the same from the
            // totalNumberOfDays.

            int monthDaysElasped = days;
            int monthDaysToSubstract = 0;
            if (totalNumberOfDays >= monthDaysElasped) {
                monthDaysToSubstract = monthDaysElasped;
            } else if (totalNumberOfDays < monthDaysElasped) {
                monthDaysToSubstract = totalNumberOfDays;
            }
            days -= monthDaysToSubstract;
            totalNumberOfDays -= monthDaysToSubstract;

            // If the total number of days exceeds the month limit.
            if (days <= 0 & totalNumberOfDays > 0) {
                month--;
                if (month < 0) {
                    month = 11;
                    year--;
                }

                int monthCapacity = MONTH_DAYS[month];
                if (month == 1 & isYearLeap(year)) { // If the month is february, check for leap
                    monthCapacity++;
                }
                days = monthCapacity;
            }
        }

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * Returns true if the given date is a leap year
     * Returns false if the given date is not a leap year
     */
    public static boolean isDateLeap(Date date) {
        // If the year is evenly divisible by 4 and not divisible
        // by 100, or if the year is evenly divisible by 400, then
        // it's a leap year:
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        //System.out.println("Year = " + year);
        //System.out.println(mod(year, 4));
        //System.out.println(mod(year, 100));
        //System.out.println(mod(year, 400));
        if (mod(year, 4) == 0 & mod(year, 100) != 0) {
            return true;
        }

        if (mod(year, 400) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given year is a leap year
     * Returns false if the given year is not a leap year
     */
    public static boolean isYearLeap(int year) {
        // If the year is evenly divisible by 4 and not divisible
        // by 100, or if the year is evenly divisible by 400, then
        // it's a leap year:
        if (mod(year, 4) == 0 & mod(year, 100) != 0) {
            return true;
        }
        if (mod(year, 400) == 0) {
            return true;
        }
        return false;
    }

    public static int mod(int num1, int num2) {
        int multiples = num1 / num2;
        int remainder = num1 - (multiples * num2);
        return remainder;
    }

    /**
     * Returns a clause which can be incorporated in the sql query statement to
     * enforce date range.
     */
    public static String getSqlDateRange(String fieldName, java.sql.Date date1, java.sql.Date date2) {
        //(doj > {D '2002-07-01'} and doj < {D '2002-12-01'})
        // ATTENTION:
        // Due to a bug in JDataStore entries are displayed according to the date
        // range specification. Due to this bug the ending date range should be one
        // more than the date2 and the comparator should be in the format
        // xyzdate < date2+1 // this will fetch all matching rows.
        // instead of
        // xyzdate <= date2 // this will fetch only the first row.

        String query = "";
        String range1 = "";
        String range2 = "";

        if (date1 != null) {
            query = fieldName + " >= " + getSqlDateString(date1);
            if (date2 != null) {
                query += " AND " + fieldName + " < " + getSqlDateString(new java.sql.Date(DateUtilities.addToDate(date2, 1).getTime()));
            }
        } else {
            if (date2 != null) {
                query = fieldName + " < " + getSqlDateString(new java.sql.Date(DateUtilities.addToDate(date2, 1).getTime()));
            }
        }
        return query;
    }

    /**
     * Returns a sql query representation of a date.
     */
    public static String getSqlDateString(java.sql.Date date) {
        String range = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date(date.getTime()));
        range = "{D '" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + "'}";
        return range;
    }

    public static void main(String args[]) throws Exception {
        // Check for equlity
        java.util.Date date1 = DateUtilities.toDate("01/04/2003");
        java.util.Date date2 = DateUtilities.toDate("01/04/2003");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
        // Check for date difference
        date1 = DateUtilities.toDate("01/04/2003");
        date2 = DateUtilities.toDate("02/04/2003");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
        date1 = DateUtilities.toDate("02/04/2003");
        date2 = DateUtilities.toDate("01/04/2003");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
        // Check for month difference.
        date1 = DateUtilities.toDate("01/04/2003");
        date2 = DateUtilities.toDate("01/05/2003");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
        date1 = DateUtilities.toDate("01/05/2003");
        date2 = DateUtilities.toDate("01/04/2003");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
        // Check for year difference.
        date1 = DateUtilities.toDate("01/04/2003");
        date2 = DateUtilities.toDate("01/04/2004");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
        date1 = DateUtilities.toDate("01/04/2004");
        date2 = DateUtilities.toDate("01/04/2003");
        System.out.println(DateUtilities.toString(date1) + ", " + DateUtilities.toString(date2) + " = "
                + DateUtilities.compareDates(date1, date2));
    }
}

