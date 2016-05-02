/**
 * File     : DateUtil.java
 * @author  : Edison Chindrawaly
 * @version : %I%,%G%
 * Purpose  : Utility for Date manipulation or retrieval in Java. 
 */

package ATTS;

import java.util.Calendar;
import java.util.Date;
import java.io.PrintWriter;

public class DateUtil implements DateUtility
{
 private Calendar calendar;
 private Date currentTime;
 private int month;
 private int year;
 private int date;
 private int day;
 private int AMPM;
 private int hour;
 private int minute;
 private int second;
 private int WOM; // Week Of Month
 private int WOY; // Week Of Year
 private int DOY; // Day of Year
 private int flagDiffMonth; //flag of different month
 private int diffYear;
 private int diffMonth;
 private int diffDOY;
 private int mondayDOW; // the date of monday of the week
 private final static int YEAR_LESS = -29;
 private final static int MONTH_LESS= -31;
 private final static int DATE_LESS = -35;
 private final static int YEAR_MORE = -47;
 private final static int MONTH_MORE= -51;
 private final static int DATE_MORE = -53;
 private final static int DAYS_NOT_EQUAL = -17;
 private final static int ALL_OK = 0;
 private final static int ERROR = -19;
/**
 * default constructor: to initiailize all the variables of time
 * @param  none
 * @return none
 */
 public DateUtil()
 {
   initialize();                     
   resetAll();
 }

/**
 * Parameterize constructor: Let the users manipulate the date,
 * the month, the year, or the hour.
 * @param  int byNumber contains how many users want to advance/back
 *         int option contains 
 *         option 1 : Increase by Day of Month
 *         option 2 : Increase by Month
 *         option 3 : Increase by Year
 *         option 4 : Increase by Day of Year
 *         option 5 : Increase by Week of Month
 *         option 6 : Increase by Week of Year
 *         option 7 : Increase by Hour     
 *         option 8 : Increase by Minute
 *         option 9 : Increase by Second
 * @return none
 */
 public DateUtil(int option, int number)
 {
   initialize();                      
   setTime(option,number);
 }

/**
 * initialize's method is to set Calendar to the current Time.
 * @param  none
 * @return none
 */
 public void initialize()
 {
  calendar = Calendar.getInstance();
  calendar.setLenient(false);
 }

/**
 * getMonth's method is to return the current month
 * @param  none
 * @return int current month
 */
 public int getMonth()
 {
   return month;
 }

/**
 * getYear's method is to return the current year 
 * @param  none
 * @return int current year
 */
 public int getYear()
 {
   return year; 
 }

/** 
 * getDate's method is to return the current date
 * @param  none
 * @return int current date
 */
 public int getDate()
 {
   return date;
 }

/**
 * getDay's method is to return the current day (Monday-Sunday)
 * @param  none
 * @return int current day
 */
 public int getDay()
 {
   return day;
 }

/**
 * getDOY's method is to get the day of the year. A year
 * consists of 365 days [or else leap year].
 * @param  none
 * @return int DOY
 */
 public int getDOY()
 {
  return DOY;
 }

/**
 * getAMPM's method is to return AM/PM of the hour
 * @param  none
 * @return int AM/PM
 */
 public int getAMPM()
 {
   return AMPM;
 }
 
/**
 * getHour's method is to return current hour
 * @param  none
 * @return int hour 
 */
 public int getHour()
 {
   return hour;
 }

/**
 * getMinute's method is to return current minute
 * @param  none
 * @return int minute
 */
 public int getMinute()
 {
   return minute;
 }

/**
 * getSecond's method is to return current second
 * @param  none
 * @return int second
 */
 public int getSecond()
 {
   return second;
 }

/**
 * getMondayDOW's method is to get the date of Monday of the Week
 * @param  none
 * @return 
 */
 public int getMondayDOW()
 {
  return mondayDOW;
 }

/**
 * getWOM's method is to return the week of month
 * @param  none
 * @return int week of month
 */
 public int getWOM()
 {
   return WOM;
 }

/**
 * getWOF's method is to return the week of year
 * @param  none
 * @return int week of year
 */
 public int getWOY()
 {
  return WOY;
 }

/**
 * getMaxDate's method is to return the max days in the current month
 * @param none
 * @return max days in the current month
 */
 public int getMaxDate()
 {
  return calendar.getActualMaximum(date);
 }

/**
 * getCurrentTime's method would return a complete set of 
 * time (Date, Day, Hour, Minute, Second, AM/PM, Timezone)
 * @param  none
 * @return String currentTime
 */
 public String getCurrentTime()
 {
   return new String(currentTime.toString());
 }


/**
 * calculateAnyDOW's method is to calculate any given day
 * within the week. It will return the date of the given day.
 * For example, given the day: Wednesday, it will calculate
 * the date.
 */
 public int calculateAnyDOW(int qDay)
 {
  if((qDay > 7) || (qDay < 0))
  {
   System.out.println("ERROR: calculateAnyDOW");
   System.exit(0);
  }

  int diffValue = 0;
  if(qDay != day) diffValue = qDay - day;

  if(diffValue != 0)
  {
   Calendar tmpCal = Calendar.getInstance();
   int diffDOY = getDOY() + diffValue;
   tmpCal.set(tmpCal.DAY_OF_YEAR, diffDOY);
   int tmpMonth = tmpCal.get(tmpCal.MONTH) + 1;
   if(tmpMonth != month)
   {
    flagDiffMonth = 1;
    diffDOY = tmpCal.get(tmpCal.DAY_OF_YEAR);
    diffYear= tmpCal.get(tmpCal.YEAR);
    diffMonth = tmpMonth;
   }
   else
   {
    flagDiffMonth = 0;
    diffDOY = 0;
    diffYear= 0;
    diffMonth = 0;
   }
   int tmpDate = tmpCal.get(tmpCal.DATE);
   tmpCal.clear();
   return tmpDate;
  }
  return date;
 }

/**
 * getDiffYear's method is to return the year of firstDOW
 * if it is in different month
 * @param  none
 * @return none
 */
 public int getDiffYear() 
 { 
   return diffYear; 
 }
 
/**
 * getDiffMonth's method is to return the month of firstDOW
 * if it is in different month
 * @param  none
 * @return none
 */
 public int getDiffMonth()
 { 
  return diffMonth;
 }

/**
 * getDiffDOY's method is to return the year of firstDOY
 * if it is in different month
 * @param  none
 * @return none
 */
 public int getDiffDOY()  
 { 
  return diffDOY;  
 }

/**
 * getFlagDiffMonth's method is to get the flag that indicates 
 * if the first date of the week is in different month [value= 1]
 * and if the first date of the week is in the same month [value= 0]
 * @param  none
 * @return 0 if firstDOW is in the same month, else return 1
 */
 public int getFlagDiffMonth()
 {
  return flagDiffMonth;
 }


/**
 * formTime's method is to form time in AM_PM US time format
 * @param  none
 * @return String contains time
 */
 public String formTime()
 {
   StringBuffer sbMin = new StringBuffer();
   StringBuffer sbSec = new StringBuffer();
   if(getMinute()< 10) 
     sbMin.append("0"+getMinute());
   else 
     sbMin.append(getMinute());
   if(getSecond()< 10)
     sbSec.append("0"+getSecond());
   else
     sbSec.append(getSecond());
   return new String(getHour()+":"+sbMin+":"+sbSec+" "+ 
   convertAM_PM(AMPM));
 }

/**
 * formSQLDate's method is to form SQL form of date YYYY-MM-DD
 * @param  none
 * @return String contains SQL form of Date
 */
 public String formSQLDate()
 {
   return new String(getYear()+"-"+getMonth()+"-"+getDate());
 }

/**
 * formIntlDate's method is to form Intl form of date DD-MM-YYYY
 * @param  none
 * @return String contains Intl  date format.
 */
 public String formIntlDate()
 {
   return new String(getDate()+" "+formMonth()+" "+getYear()); 
 }

/**
 * formUSDate's method is to form US form of date MM-DD-YYYY
 * @param  none
 * @return String contains US date format
 */
 public String formUSDate()
 {
   return new String(formMonth()+" "+getDate()+" "+getYear());
 }

/**
 * formDay's method is to form day (Mon,Tue,Wed,etc)
 * @param  none
 * @return String contains the day of the week
 */
 public String formDay()
 {
   return new String(convertDay(day));
 }

/**
 * formMonth's method is to form the name of month (January,etc)
 * @param  none
 * @return String contains the name of the month in a year
 */
 public String formMonth()
 {
   return new String(convertMonth(month));
 }

/**
 * convertAM_PM's method is to convert the given int to indicate
 * whether the hour of day is PM or AM
 * @param  int 1 or 0 
 * @return String contains "AM" or "PM" 
 */
 public String convertAM_PM(int indication)
 {
   StringBuffer AM_PM = new StringBuffer();
   switch(indication)
   {
    case 0 : AM_PM.append("AM");       
             break;
    case 1 : AM_PM.append("PM");
    default: break;
   }
   return AM_PM.toString();
 }

/**
 * convertDay's method is to convert the given int day to form
 * a meaningful name of the day in a week (Mon,Tue,Wed,etc)
 * Sunday has a value 1 due to JAVA API Spec, and it is not 
 * by choice
 * @param  int day
 * @return String contains the day of the week
 */
 public String convertDay(int indication)
 {
  StringBuffer value = new StringBuffer();
  switch(indication)
  {
   case 1 : value.append("Sunday");
            break;
   case 2 : value.append("Monday");
            break;
   case 3 : value.append("Tuesday");
            break;
   case 4 : value.append("Wednesday");
            break;
   case 5 : value.append("Thursday");
            break;
   case 6 : value.append("Friday");
            break;
   case 7 : value.append("Saturday");
   default: break;
  }
  return value.toString();
 }

/**
 * convertMonth's method is to convert the int month to 
 * a meaningful name of the month (Jan, Feb, etc)
 * @param  int month
 * @return String contains the name of the month
 */
 public String convertMonth(int indication)
 {
  StringBuffer vmonth = new StringBuffer();
  switch(indication)
  {
   case  1: vmonth.append("January"); 
            break;
   case  2: vmonth.append("Febuary"); 
	    break;
   case  3: vmonth.append("March"); 
            break;
   case  4: vmonth.append("April"); 
	    break;
   case  5: vmonth.append("May"); 
            break;
   case  6: vmonth.append("June"); 
            break;
   case  7: vmonth.append("July"); 
            break;
   case  8: vmonth.append("August"); 
            break;
   case  9: vmonth.append("September"); 
            break;
   case 10: vmonth.append("October"); 
            break; 
   case 11: vmonth.append("November"); 
            break;
   case 12: vmonth.append("December"); 
   default: break;
  }
  return vmonth.toString();
 }

/**
 * addTime's method is to set the time forward 
 * either by Year, Month, Week, Day or Hour
 * @param  int number 
 *         int option
 * @return boolean true if successful else return false
 */
 public boolean addTime(int option, int number)
 {
  if((number<0) || ((option <0) || (option>9)))
    return false;
   switch(option)
   {
     case 0  : // Default - Give the current time
               break;
     case 1  : // Increase by Day of Month
               calendar.add(calendar.DAY_OF_MONTH, number);
               break;
     case 2  : // Increase by Month
               calendar.add(calendar.MONTH, number);
               break;
     case 3  : // Increase by Year
               calendar.add(calendar.YEAR, number);
               break;
     case 4  : // Increase by Day of Year
     	       calendar.add(calendar.DAY_OF_YEAR, number);
               break;
     case 5  : // Increase by Week Of Month
               calendar.add(calendar.WEEK_OF_MONTH, number);
               break;
     case 6  : // Increase by Week of Year
               calendar.add(calendar.WEEK_OF_YEAR, number);
	       break;
     case 7  : //Increase by Hour
               calendar.add(calendar.HOUR, number);
               break;
     case 8  : //Increase by Minute
     	       calendar.add(calendar.MINUTE, number);
	       break;
     case 9  : //Increase by Second
     	       calendar.add(calendar.SECOND, number);
     default : break;
   }
   resetAll(); 
   return true;
 }

/**
 * setTime()'s method is to set Time for the specified 
 * option and by the specified number
 * @param  int option, int number
 * @return true upon successful operation, else return false 
 */
 public boolean setTime(int option, int number)
 {
  if((number < 0) || ((option< 0)||(option>9)))
    return false;
  switch(option)
  {
    case 0 : break;
    case 1 : calendar.set(calendar.DAY_OF_MONTH, number); break;
    case 2 : calendar.set(calendar.MONTH, number); break;
    case 3 : calendar.set(calendar.YEAR, number); break;
    case 4 : calendar.set(calendar.DAY_OF_YEAR, number);
    case 5 : calendar.set(calendar.WEEK_OF_MONTH, number); break;
    case 6 : calendar.set(calendar.WEEK_OF_YEAR, number); break;
    case 7 : calendar.set(calendar.HOUR, number); break; 
    case 8 : calendar.set(calendar.MINUTE, number); break;
    case 9 : calendar.set(calendar.SECOND, number); 
    default: break;
  }
  resetAll(); 
  return true;
 }

/**
 * rollTime's method is roll the time. The different between
 * roll and add is "roll" maintains the current year while
 * "add" does not maintain the current year
 * @param  int option, int number
 * @return true if successful else return false
 */
 public boolean rollTime(int option, int number)
 {
   if((number < 0) || ((option < 0)||(option>7)))
     return false;
   switch(option)
   {
     case 0 : break;
     case 1 : calendar.roll(calendar.DAY_OF_MONTH, number); break;
     case 2 : calendar.roll(calendar.MONTH, number); break;
     case 3 : calendar.roll(calendar.YEAR, number); break;
     case 4 : calendar.roll(calendar.DAY_OF_YEAR, number);
     case 5 : calendar.roll(calendar.WEEK_OF_MONTH, number); break;
     case 6 : calendar.roll(calendar.WEEK_OF_YEAR, number); break;
     case 7 : calendar.roll(calendar.HOUR, number); break;
     case 8 : calendar.roll(calendar.MINUTE, number); break;
     case 9 : calendar.roll(calendar.SECOND, number);
     default: break;
   }
   resetAll();
   return true;
 }

/**
 * part31Days' method is to check whether the month is part of 31 days months
 * @param int month
 * @return true if month is part of 31 days months otherwise false
 */
 public boolean part31Days(int month)
 {
  int length = 7;
  for(int i=0;i<length;i++)
   if(thirtyOneDays[i]==month)
     return true;
  return false;
 }

/**
 * part30Days' method is to check whether the month is part of 30 days months
 * @param int month
 * @return true if month is part of 30 days months otherwise false
 */
 public boolean part30Days(int month)
 {
  int length = 4;
  for(int i=0;i<length;i++)
   if(thirtyDays[i]==month)
     return true; 
  return false;
 } 

/**
 * resetAll's method is to reset the values of DateUtil to 
 * the current Calendar's setting
 * @param  none
 * @return none
 */
 public void resetAll()
 {
  currentTime = calendar.getTime();
  month   = calendar.get(calendar.MONTH) + 1;
  year    = calendar.get(calendar.YEAR);
  date    = calendar.get(calendar.DAY_OF_MONTH);
  day     = calendar.get(calendar.DAY_OF_WEEK);
  DOY     = calendar.get(calendar.DAY_OF_YEAR);
  AMPM    = calendar.get(calendar.AM_PM);
  hour    = calendar.get(calendar.HOUR);
  minute  = calendar.get(calendar.MINUTE);
  second  = calendar.get(calendar.SECOND);
  WOM     = calendar.get(calendar.WEEK_OF_MONTH);
  WOY     = calendar.get(calendar.WEEK_OF_YEAR);
  mondayDOW = calculateAnyDOW(MONDAY);
 }

/**
 * clearTime's method is to clear the Calendar's class
 * from all the filled values
 * @param  none
 * @return none
 */
 public void clearTime()
 {
  calendar.clear();
 }

/**
 * checkSQLDate's method is to check the format of the SQL date and the validity
 * of the date. It uses the rule of strict [boolean]. If strict is true, that means
 * the date must be in the current year and current month. If strict is false,
 * that means the date must not be less than the current year and current month
 * @param String dt - contains the SQL form of Date
 *        PrintWriter out - if needs to write to Servlet. else null.
 * @return false if fails otherwise true
 */
 public boolean checkSQLDate(String dt,PrintWriter out)
 {
  if(dt == null)
  {
    if(out == null)
      System.out.println("Input is null");
    else
      out.println("<BR><B>Tidak ada input. Input = null</B></BR>");
    return false;
  }
  if(dt.length()!=10)
  {
    if(out == null)
    {
      System.out.println("Format SQL date harus 10 character: YYYY-MM-DD");
      System.out.println("Contoh: 1990-01-31");
    }
    else
    {
      out.println("<BR><B>Format SQL date harus 10 character: YYYY-MM-DD"); 
      out.println(" Contoh: 1990-01-31</B></BR>");
    }
    return false;
  }
  int pos1 = dt.indexOf("-");
  if(pos1 < 0)
  {
    if(out == null)
    {
     System.out.println("Anda harus mengikuti format SQL date: YYYY-MM-DD");
     System.out.println("Contoh: 2001-01-31"); 
    }
    else
    {
     out.println("<BR><B>Anda harus mengikuti format SQL date: YYYY-MM-DD");
     out.println(" Contoh: 2001-01-31</B></BR>");
    }
    return false; 
  }
  int pos2 = dt.indexOf("-",pos1+1);
  if(pos2 < 0)
  {
    if(out == null)
    {
     System.out.println("Anda harus mengikuti format SQL date: YYYY-MM-DD");
     System.out.println("Contoh: 2001-01-31");
    }
    else
    {
     out.println("<BR><B>Anda harus mengikuti format SQL date: YYYY-MM-DD");
     out.println(" Contoh: 2001-01-31</B></BR>");
    }
    return false;
  }
  if(!checkDigit(dt,out))
    return false;
  return true;
 }

 public boolean checkDigit(String dt,PrintWriter out)
 {
  if(dt == null)
    return false;
  int size = dt.length();
  for(int i=0;i<size;i++)
   if(!Character.isDigit(dt.charAt(i)))
     if((dt.charAt(i)!='.')&&(dt.charAt(i)!=',')&&(dt.charAt(i)!='-')&&(dt.charAt(i)!='/'))
     {
      if(out==null)
        System.out.println("Character di posisi :"+i+" bukan digit");
      else
        out.println("<BR><B>Character di posisi :"+i+" bukan digit</B></BR>");
      return false;
     }
  return true;
 }
 
/**
 * checkSQLDateValidity's method is to check the SQL Date Validity - things
 * like if the day is more than max allowed day in a particular month or
 * the month is less than 1 or more than 12.
 * @param String dt - contains the SQL Date format
 *        int strict- 1 check for SQL Date that is more than current
 *                    2 check for SQL Date that is less than current
 *                    3 check for SQL Date that is less than current date 
 *                    4 check for SQL Date in normal way. Allowed year more or less.
 * @return true if the SQL Date is valid else return false
 */
 public boolean checkSQLDateValidity(String dt,int strict,PrintWriter out)
 {
  boolean flag = false; 
  int status = 0;
  int y1 = Integer.parseInt(dt.substring(0,4));
  int m1 = Integer.parseInt(dt.substring(5,7));
  int d1 = Integer.parseInt(dt.substring(8,10));
  if(strict == 1)
    status = checkMore(y1,m1,d1);
  else if(strict == 2)
    status = checkLess(y1,m1,d1);
  else if(strict == 3)
    status = checkLessStrict(y1,m1,d1);
  else if(strict == 4)
    status = checkNormal(y1,m1,d1);
  switch(status)
  {
   case YEAR_LESS  : 
    if(out == null)
     System.out.println("Tahun yg diinput lebih kecil dari "+getYear());
    else
     out.println("<BR><B>Tahun yg diinput lebih kecil dari "+getYear()+"</b>"); 
    break;
   case MONTH_LESS :
     if(out == null)
      System.out.println("Bulan yg diinput lebih kecil dari bulan ini: "+getMonth());
     else
      out.println("<BR><B>Bulan yg diinput lebih kecil dari bulan "+getMonth()+"</B>");
     break;
   case DATE_LESS  :
     if(out == null)
      System.out.println("Tanggal yg diinput lebih kecil dari tanggal ini: "+getDate());
     else
      out.println("<BR><B>Tanggal yg diinput lebih kecil dari tanggal:"+getDate()+"</B>"); 
     break;
   case YEAR_MORE  :
     if(out == null)
      System.out.println("Tahun yg diinput lebih besar dari "+getYear());
     else
      out.println("<BR><B>Tahun yg diinput lebih besar dari "+getYear()+"</B>");
     break;
   case MONTH_MORE :
     if(out == null)
      System.out.println("Bulan yg diinput lebih besar dari "+getMonth());
     else
      out.println("<BR><B>Bulan yg diinput lebih besar dari "+getMonth()+"</B>");
     break;
   case DATE_MORE  :
     if(out == null)
      System.out.println("Tanggal yg diinput lebih besar dari "+getDate());
     else
      out.println("<BR><B>Tanggal yg diinput lebih besar dari "+getDate()+"</B>");
     break;
   case DAYS_NOT_EQUAL :
     if(out == null)
      System.out.println("Tanggal yg diinput tidak sesuai dgn jumlah hari dibulan ini");
     else
     out.println("<BR><B>Tanggal yg diinput tidak sesuai dgn jumlah hari dibulan ini</b>"); 
     break;
   case ALL_OK : flag = true; 
                 break;
   default : 
     if(out == null)
       System.out.println("Should not happen in checkSQLValidation");
     else
       out.println("<BR><B>Should not happen in checkSQLValidation</B>");
     break;
  }
  return flag;
 }

/**
 * checkNormal's method is to check date format in the normal way. It allows more/less
 * than the current date. It checks for date/month boundaries only.
 * @param  int y1 - year
 *         int m1 - month
 *         int d1 - date
 * @return all the static value
 */
 private int checkNormal(int y1,int m1,int d1)
 {
  if(m1<1)
    return MONTH_LESS;
  if(m1>12)
    return MONTH_MORE;
  if(d1<1)
    return DATE_LESS;
  else if(part31Days(m1))
         if(d1>31) return DAYS_NOT_EQUAL;
  else if(part30Days(m1))
         if(d1>30) return DAYS_NOT_EQUAL;
  else if(d1>getMaxDate()) return DAYS_NOT_EQUAL;
            
  return ALL_OK;
 }

/**
 * checkLess' method is to check the year/month not to pass the current year/month.
 * Then it checks for date boundary. Thus the passing year/month must be more or equal
 * to the current year/month
 * @param  int y1 - year, m1 - month,  d1 - date
 * return all the static values 
 */
 private int checkLess(int y1,int m1,int d1)
 {
  int y2 = getYear();
  int m2 = getMonth();
  if(y1<y2)
    return YEAR_LESS;
  if(m1<m2)
    return MONTH_LESS;
  if(m1>12)
    return MONTH_MORE;
  if(d1<1)
    return DATE_LESS; 
  else if(part31Days(m1))
    if(d1>31)
      return DAYS_NOT_EQUAL;
  else if(part30Days(m1))
    if(d1>30)
      return DAYS_NOT_EQUAL;
  else // must be Febuary
    if(d1>getMaxDate())
      return DAYS_NOT_EQUAL; 
  return ALL_OK;
 }

/**
 * checkLessStrict's method is far stricter than the method above. It checks for the date
 * given if it is less than the current date, it return DATE_LESS static value.
 * Thus the passing date/month must be more than the current date/month
 * @param int y1 - year, m1 - month, d1 - date
 * @return all the static value
 */
 private int checkLessStrict(int y1,int m1,int d1)
 {
  int y2 = getYear();
  int m2 = getMonth();
  int d2 = getDate();
  if(y1<y2)
    return YEAR_LESS;
  if(m1<m2)
    return MONTH_LESS;
  if(m1>12)
    return MONTH_MORE;
  if(d1<1)
    return DATE_LESS;
  if(d1<d2)
    return DATE_LESS;
  if(part31Days(m1))
    if(d1>31)
      return DAYS_NOT_EQUAL;
  else if(part30Days(m1))
    if(d1>30)
      return DAYS_NOT_EQUAL;
  else
    if(d1>getMaxDate())
      return DAYS_NOT_EQUAL;
  return ALL_OK;
 }

/**
 * checkMore's method is to check the for input year to be more than current year. 
 * Thus the passing year must be less than current year
 * @param int y1 - year, m1 - month, d1 - date
 * @return int all the static value
 */
 private int checkMore(int y1,int m1,int d1)
 {
  int y2 = getYear();
  if(y1>y2)
    return YEAR_MORE;
  if(m1<1)
    return MONTH_LESS;
  if(m1>12)
    return MONTH_MORE;
  if(d1<1)
    return DATE_LESS;
  if(part31Days(m1))
    if(d1>31)
      return DAYS_NOT_EQUAL;
  else if(part30Days(m1))
    if(d1>30)
      return DAYS_NOT_EQUAL; 
  else // must be Febuary
    if(d1>getMaxDate())
      return DAYS_NOT_EQUAL; 
  return ALL_OK; 
 }

/**
 * checkEarly's method is to check whether date [d1] is earlier than [d2]
 * It assumed that d1 and d2 are already in proper date format
 * @param String d1, String d2
 * @return boolean true if d1 is earlier
 */
 public boolean checkEarly(String d1, String d2)
 {
  if(((d1==null)||(d2==null))||((d1.length()!=10)||(d2.length()!=10)))
    return false;
  int p11 = d1.indexOf('-');
  int p12 = d1.indexOf('-',p11+1);
  int p21 = d2.indexOf('-');
  int p22 = d2.indexOf('-',p21+1);
  if((p11<0)||(p12<0)||(p21<0)||(p22<0))
    return false;
  int year1 = Integer.parseInt(d1.substring(0,p11)); 
  int year2 = Integer.parseInt(d2.substring(0,p21));
  int month1= Integer.parseInt(d1.substring(p11+1,p12));
  int month2= Integer.parseInt(d2.substring(p21+1,p22));
  int date1 = Integer.parseInt(d1.substring(p12+1,d1.length()));
  int date2 = Integer.parseInt(d2.substring(p22+1,d2.length()));
  if(year1>year2)
    return false;
  if((month1>month2)&&(year1==year2))
    return false;
  if((date1>date2)&&(month1==month2)&&(year1==year2))
    return false;
  return true;
 }

} // end of DateUtil class

