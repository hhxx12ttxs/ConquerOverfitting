package com.util;

//This class of routines is designed for converting between Java DateTime format and Serial DateTime format
//SerialDates are represented as type DATE (i.e. uppercase)
//SerialDates will actually be stored as doubles
//JavaDates are represented as type date (i.e. lowercase)

//Call the setDate(double) member to convert from SerialDate format to Java DateTime format
//To convert from Java DateTime format to SerialDate format, use the toDouble() member

import java.util.Date;
import java.lang.Math;
import java.lang.String;
import java.text.*;

//Let us extend the native Java Calendar class so that it can work with SerialDates
public class SerialDate extends java.util.GregorianCalendar {
  //set to false if you don't want to show internal working of class
  boolean bShowConversions = false;

  String output;
  DecimalFormat fmt;
  double dRetrieve_Date;

  //Create the class and use the set method later to initialize to a date
  public SerialDate() {
//Constructor creates instance of GregorianCalendar class
      super();
  }

  //Create the class and use the number of milliseconds since January 1, 1970
//This is a standard format for Date/Time in Java
  public SerialDate(long JavaDateTime) {
      Date dJavaDateTime = new Date(JavaDateTime); // convert milliseconds to standard Date format
      setTime(dJavaDateTime);

//Optionally, show the various formats for our Date
      if (bShowConversions) {
          int iYear = get(YEAR);
          int iMonth = get(MONTH) + 1;
          int iDay = get(DATE);

          Print(iYear, iMonth, iDay);
      }
  }

  //Create the class and use the standard Java date class to initialize it
  public SerialDate(Date dJavaDateTime) {
      setTime(dJavaDateTime);

//Optionally, show the various formats for our Date
      if (bShowConversions) {
          int iYear = get(YEAR);
          int iMonth = get(MONTH) + 1;
          int iDay = get(DATE);

          Print(iYear, iMonth, iDay);
      }
  }

  //Create SerialDate given the year, month and day
  public SerialDate(int iYear, int iMonth, int iDay) {
//Constructor creates instance of GregorianCalendar class 
      super(iYear, iMonth - 1, iDay);

//Optionally, show the various formats for our Date
      if (bShowConversions) {
          // Ensure month and day are in the expected ranges before we print
          // Let the default constuctor for the GregorianCalendar class do the work
          // for month, 0 is December last year, -1 is November, etc
          iYear = get(YEAR);        // get any year adjustment done by calendar class initialization
          iMonth = get(MONTH) + 1;  // get any month adjustment done by calendar class initialization
          iDay = get(DATE);         // get any day adjustment done by calendar class initialization

          Print(iYear, iMonth, iDay);
      }
  }

  //Create SerialDate given the year, month, day, hour, minute and second
  public SerialDate(int iYear, int iMonth, int iDay, int iHrs, int iMin, int iSec) {
//Constructor creates instance of GregorianCalendar class
      super(iYear, iMonth - 1, iDay, iHrs, iMin, iSec);

//Optionally, show the various formats for our Date
      if (bShowConversions) {
          // Ensure month and day are in the expected ranges before we print
          // Let the default constuctor for the GregorianCalendar class do the work
          // for month, 0 is December last year, -1 is November, etc
          iYear = get(YEAR);        // get any year adjustment done by calendar class initialization
          iMonth = get(MONTH) + 1;  // get any month adjustment done by calendar class initialization
          iDay = get(DATE);         // get any day adjustment done by calendar class initialization
          iHrs = get(HOUR_OF_DAY);
          iMin = get(MINUTE);
          iSec = get(SECOND);

          Print(iYear, iMonth, iDay, iHrs, iMin, iSec);
      }
  }

  //Load specified SerialDate into our class
  public SerialDate(double date) {
//Constructor creates instance of GregorianCalendar class
      super();

//Convert SerialDate into Calendar class format
      setDate(date);

//Optionally, show the various formats for our Date
      if (bShowConversions) {
          int iYear = get(YEAR);
          int iMonth = get(MONTH) + 1;
          int iDay = get(DATE);

          Print(iYear, iMonth, iDay);
      }
  }

  //Print our Date in multiple formats
  public void Print(int iYear, int iMonth, int iDay) {
      // Print what we passed in
      PrintDateParts(iYear, iMonth, iDay);

      // Let's build a SerialDate again using the toDouble() method and then print it
      PrintDate(dRetrieve_Date = toDouble());

      // Let's print the JavaDateTime for the current date
      PrintJavaDateTime(getTime());

      // Let's print the JavaDateTime in its internal format
      PrintJavaDateTimeAsLong(getTime());
  }

  //Print our Date in multiple formats
//method overloaded to allow for hours, minutes, seconds
  public void Print(int iYear, int iMonth, int iDay, int iHrs, int iMin, int iSec) {
//Print what we passed in 
      PrintDateTimeParts(iYear, iMonth, iDay, iHrs, iMin, iSec);

//Let's build a SerialDate again using the toDouble() method and then print it
      PrintDate(dRetrieve_Date = toDouble());

//Let's print the JavaDateTime for the current date
      PrintJavaDateTime(getTime());

//Let's print the JavaDateTime in its internal format
      PrintJavaDateTimeAsLong(getTime());
  }

  //Print in SerialDate format
  public void PrintDate(double Date) {
//choose number of decimals that we want 
      fmt = new DecimalFormat("0.0000;-0.0000");
      output = "SerialDate is " + fmt.format(Date);
      System.out.println(output);
  }

  //Print in Year, Month, Day format
  public void PrintDateParts(int year, int month, int day) {
//choose number of decimals that we want 
      fmt = new DecimalFormat("0;-0");
      output = "Year = " + fmt.format(year) + ": Month = " + fmt.format(month) + ": Day = " + fmt.format(day);
      System.out.println(output);
  }

  //Print in Full format (Year, Month Day, Hour, Minute, Second)
  public void PrintDateTimeParts(int year, int month, int day, int hrs, int min, int sec) {
//choose number of decimals that we want 
      fmt = new DecimalFormat("0;-0");
      output = "Year = " + fmt.format(year) + ": Month = " + fmt.format(month) + ": Day = " + fmt.format(day) + "\r\n";
      output = output + "Hour = " + fmt.format(hrs) + ": Minute = " + fmt.format(min) + ": Second = " + fmt.format(sec);
      System.out.println(output);
  }

  //Print the JavaDateTime value for the current Date
  public void PrintJavaDateTime(Date currentDate) {
      SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

      System.out.println(format.format(currentDate));
  }

  //Print the JavaDateTime value for the current Date as a long integer
//This is the internal format for a Java Date
  public void PrintJavaDateTimeAsLong(Date currentDate) {
      System.out.println(currentDate.getTime() + "\r\n");
  }

  //Adjust half second so that it is expressed in days
  static double HALF_SECOND = (1.0 / 172800.0);

  //These are the relative time points for the start of each months (assuming a non-leap year)
  static int rgMonthDays[] =
          {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};

  //Set the calendar to the date associated with the serial date
  public void setDate(double dSerialDate) {
//Note that every fourth century is a leap year but other centuries are not
      long nDays;               // Number of days since Jan 1, 1900
      long nDaysAdjust;         // Adjust for Excel treating 1900 as a leap year
      long nSecsInDay;          // Time in seconds since midnight
      long nMinutesInDay;       // Minutes in day
      long QuadCenturies;       // Number of 400 year periods since Jan 1, 1900
      long QuadCenturyCount;    // Which period are we in
      long QuadYearsCount;      // Number of 4 year periods since Jan 1, 1900
      long QuadYearsDayCount;   // Day position within current 4 year period
      long QuadYearsYearCount;  // Year position within 4 year period

      boolean bLeapInCurrentPeriod = true; // FALSE if it includes century that is not a leap year

      double dblDate = dSerialDate; // temporary serial date

      nDays = (long) dblDate;

//Round to the second
      dblDate += ((dSerialDate > 0.0) ? HALF_SECOND : -HALF_SECOND);

//Adjust for Excel treating 1900 as a leap year
//Offset so that 12/30/1899 is 0
      nDaysAdjust = (long) dblDate + 693959L;
      dblDate = Math.abs(dblDate);
      nSecsInDay = (long) ((dblDate - Math.floor(dblDate)) * 86400.);

//Leap years every 4 yrs except where period includes century that is not a leap year
      QuadCenturies = (long) (nDaysAdjust / 146097L);

//Set nDaysAdjust to day within Quad Century block
      nDaysAdjust %= 146097L;

//Subtract 1 to adjust for Excel treating 1900 as a leap year
      QuadCenturyCount = (long) ((nDaysAdjust - 1) / 36524L);  // Non-leap century

      if (QuadCenturyCount != 0) {
          // Set nDaysAdjust to day within current centurY
          nDaysAdjust = (nDaysAdjust - 1) % 36524L;

          // +1 to adjust for Excel treating 1900 as a leap year
          QuadYearsCount = (long) ((nDaysAdjust + 1) / 1461L);

          if (QuadYearsCount != 0)
              QuadYearsDayCount = (long) ((nDaysAdjust + 1) % 1461L);
          else {
              // Current century is not a leap year
              bLeapInCurrentPeriod = false;
              QuadYearsDayCount = (long) nDaysAdjust;
          }
      } else {
          // Current century is leap year
          QuadYearsCount = (long) (nDaysAdjust / 1461L);
          QuadYearsDayCount = (long) (nDaysAdjust % 1461L);
      }

      if (bLeapInCurrentPeriod) {
          // -1 because first year has 366 days
          QuadYearsYearCount = (QuadYearsDayCount - 1) / 365;

          if (QuadYearsYearCount != 0)
              QuadYearsDayCount = (QuadYearsDayCount - 1) % 365;
      } else {
          QuadYearsYearCount = QuadYearsDayCount / 365;
          QuadYearsDayCount %= 365;
      }

//values in terms of year month date.
      int tm_sec = 0;
      int tm_min = 0;
      int tm_hour = 0;
      int tm_mday;
      int tm_mon;
      int tm_year;
      int tm_wday;
      int tm_yday;

      tm_year = (int) (QuadCenturies * 400 + QuadCenturyCount * 100 + QuadYearsCount * 4 + QuadYearsYearCount);

//Handle leap year: before, on, and after Feb. 29.
      if (QuadYearsYearCount == 0 && bLeapInCurrentPeriod && QuadYearsDayCount == 59) { /* Feb. 29 */
          tm_mon = 2;
          tm_mday = 29;
      } else {
          if (QuadYearsYearCount == 0 && bLeapInCurrentPeriod && QuadYearsDayCount >= 59)
              --QuadYearsDayCount;

          // Make QuadYearsDayCount a 1-based day of non-leap year and compute

          //  month/day for everything but Feb. 29.
          ++QuadYearsDayCount;

          // Month number always >= n/32, so save some loop time */
          for (tm_mon = (int) ((QuadYearsDayCount >> 5) + 1); QuadYearsDayCount > rgMonthDays[tm_mon]; tm_mon++) ;

          tm_mday = (int) (QuadYearsDayCount - rgMonthDays[tm_mon - 1]);
      }

      tm_sec = (int) (nSecsInDay % 60L);
      nMinutesInDay = nSecsInDay / 60L;
      tm_min = (int) (nMinutesInDay % 60);
      tm_hour = (int) (nMinutesInDay / 60);

      set(YEAR, tm_year);
      set(MONTH, tm_mon - 1);
      set(DATE, tm_mday);
      set(HOUR_OF_DAY, tm_hour);
      set(MINUTE, tm_min);
      set(SECOND, tm_sec);

      return;
  }

  //Return the SerialDate associated with the current date
  public double toDouble() {
      long nDate;
      double dblTime;

//get the details for the current date
      int iYear = get(YEAR);
      int iMonth = get(MONTH) + 1;
      int iDay = get(DATE);
      int iHour = get(HOUR_OF_DAY);
      int iMinute = get(MINUTE);
      int iSecond = get(SECOND);

//Check for leap year and set the # of days in month
      boolean bLeapYear = ((iYear & 3) == 0) && ((iYear % 100) != 0 || (iYear % 400) == 0);

  //    int nDaysInMonth = rgMonthDays[iMonth] - rgMonthDays[iMonth - 1] + ((bLeapYear && iDay == 29 && iMonth == 2) ? 1 : 0);

      nDate = iYear * 365L + iYear / 4 - iYear / 100 + iYear / 400 + rgMonthDays[iMonth - 1] + iDay;

//If its a leap year and before March, then do an adjustment:
      if (iMonth <= 2 && bLeapYear)
          nDate = nDate - 1;

//Adjust for Excel treating 1900 as a leap year
//Offset so that 12/30/1899 is 0
      nDate -= 693959L;

      dblTime = (((long) iHour * 3600L) +  // hrs in seconds
              ((long) iMinute * 60L) +   // mins in seconds
              ((long) iSecond)) / 86400.;

      double dblDate = (double) nDate + ((nDate >= 0) ? dblTime : -dblTime);

      return (dblDate);
  }


  //You can choose to test the class against an input date or
//You can choose to run the canned set of date tests
  public static void main(String args[]) {
      double dateSerial;
      int wYear, wMonth, wDay, wHour, wMinute, wSecond;

//work with the serial date passed as an argument
      if (args.length == 1) {
          dateSerial = Double.parseDouble(args[0]);

          new SerialDate(dateSerial);
      }
//work with year, month and day passed as inputs
      else if (args.length == 3) {
          wYear = Integer.parseInt(args[0]);
          wMonth = Integer.parseInt(args[1]);
          wDay = Integer.parseInt(args[2]);

          new SerialDate(wYear, wMonth, wDay);
      }
//work with the full specification of the date parts
      else if (args.length == 6) {
          wYear = Integer.parseInt(args[0]);
          wMonth = Integer.parseInt(args[1]);
          wDay = Integer.parseInt(args[2]);
          wHour = Integer.parseInt(args[3]);
          wMinute = Integer.parseInt(args[4]);
          wSecond = Integer.parseInt(args[5]);

          new SerialDate(wYear, wMonth, wDay, wHour, wMinute, wSecond);
      }
//do some generic tests if no input is passed
      else {
          //
          new SerialDate((long) 1001814600000L);       /* Sept 29, 2001 at 6:50 PM, input in Milliseconds */
          new SerialDate((double) 39800);  /* Dec 18, 2008 */
          new SerialDate((double) 40000);      /* July 6, 2009 */
          new SerialDate((double) 36585);      /* Feb 29, 2000 */
          new SerialDate((double) 73110);      /* March 1, 2100 */
          new SerialDate(2100, 3, 1);  /* March 1, 2100 */
          new SerialDate(2000, 3, 1, 14, 30, 12);  /* March 1, 2000 at 2:30 PM*/
          new SerialDate(2001, 9, 29, 18, 50, 0);  /* Sept 29, 2001 at 6:50 PM*/
          new SerialDate(2001, 9, 29, 0, 0, 0);  /* Sept 29, 2001 at 6:50 PM*/
          new SerialDate(2100, -3, -1);  /* August 30, 2099 */
          new SerialDate(2100, 31, -1);  /* June 29, 2102 */

          Date now = new Date(); /* We will show the Date/Time when this code was run */
          new SerialDate(now);
      }
      return;
  }
}


