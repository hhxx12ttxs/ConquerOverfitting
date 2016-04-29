package com.bamcore.util;

import java.io.*;

import java.util.*;

import org.json.simple.JSONAware;


public class BAMDate implements Serializable, Comparable<BAMDate>, Cloneable, JSONAware {

  /**
	 * 
	 */
	private static final long serialVersionUID = 2L;

private static final int [] monthDays = { 31, 28, 31, 30, 31, 30,
					    31, 31, 30, 31, 30, 31 };

  private static final String [] monthNames = { "January", "February",
						"March", "April",
						"May", "June", "July",
						"August", "September",
						"October", "November",
						"December"};

  private static final String [] sMonthNames = { "Jan", "Feb", "Mar", "Apr",
						 "May", "Jun", "Jul", "Aug",
						 "Sep", "Oct", "Nov", "Dec"};

  private static final String [] dayNames = { "Sunday", "Monday", "Tuesday",
					      "Wednesday", "Thursday",
					      "Friday", "Saturday"};

  private static final String [] sDayNames = { "Sun", "Mon", "Tue", "Wed",
					       "Thu", "Fri", "Sat"};

  public static final String InvalidString = "BAD DATE**";

  //
  // When we get a two-digit year, we need to decide if it's 1900 or
  // 2000-based.  Years >= _pivotYear go to 1900, years < pivot_year
  // go to 2000.
  //
  public static final int _pivotYear = 58;
  static private final long _yyPivotDate = 580101L;
  
  //
  // Calendar used for sql.Date -> BAMDate conversions
  //
  private static Calendar cal = Calendar.getInstance();
 
  //
  // The root of our Julian day count.  Days are started from this
  // year, month and day.
  //
  public static final int _julianBaseYear = 1950;
  public static final int _julian_base_month = 1;
  public static final int _julian_base_day = 1;
 
  //
  // Day of the week of the julian base date.  This is used to quickly
  // compute the day of week for any date.
  //
  static private final int _julian_base_dow = 0;
  static private final long _julian_base = 19500101L;
  static private final long _julian_of_12_31_1996 = 17167L;

  //
  // Date I/O Formats:					 Parsable?
  // 	FMT_Slash		MM/DD/YYYY		    Y 
  // 	FMT_Slash0		MM/DD/YYYY		    Y  (0 padded)
  // 	FMT_Slash2		MM/DD/YY		    Y
  // 	FMT_Slash02		MM/DD/YY		    Y  (0 padded)
  //	FMT_Long		Monday, August 23, 1997	    Y
  //	FMT_Short		Aug 23, 1997		    Y
  //	FMT_MonthDay		MM/DD			    N
  //	FMT_MonthYear		MM/YYYY			    N
  //	FMT_MonthYear2		MM/YY			    N
  //    FMT_MonthYear3          MMYY                        N
  //	FMT_YYMMDD		YYMMDD			    Y
  //	FMT_YYYYMMDD		YYYYMMDD		    Y
  //	FMT_ISO			YYYY-MM-DD		    Y
  //	FMT_Sybase		Jul  1 2003 12:00:00:000AM  Y  (time ignored)
  //	FMT_SmSybase		Jul  1 2003 12:00AM         Y  (time ignored)
  //

  public static final int FMT_Slash = 0, FMT_Slash2 = 1, FMT_Long = 2, FMT_Short = 3,
    FMT_MonthDay = 4, FMT_MonthYear = 5, FMT_MonthYear2 = 6, FMT_MonthYear3 = 7, 
    FMT_YYMMDD = 8,
    FMT_YYYYMMDD = 9, FMT_ISO = 10, FMT_Slash0 = 11, FMT_Slash02 = 12,
    FMT_Sybase = 13, FMT_SmSybase = 14, 
    FMT_BadFormat = 16;
    //FMT_MonthDay = 4, FMT_MonthYear = 5, FMT_MonthYear2 = 6, FMT_YYMMDD = 7,
    //FMT_YYYYMMDD = 8, FMT_ISO = 9, FMT_Slash0 = 10, FMT_Slash02 = 11,
    //FMT_Sybase = 12, FMT_SmSybase = 13;
  
  public static final int INIT_today = 0, INIT_undef = 1;
  
  public static final int MONTH_end_stick = 0, MONTH_end_float = 1;
  
  public static final int BETWEEN_incl = 0, BETWEEN_excl = 1;
  
  public static final long INVALID_JULIAN = 0L;
  
  public static final int DAY_Sunday = 0, DAY_Monday = 1, DAY_Tuesday = 2,
    DAY_Wednesday = 3, DAY_Thursday = 4, DAY_Friday = 5, DAY_Saturday = 6;

  private long _date;
  private long _julian;
  
  // ----------------------------------------------------------------

  public BAMDate(boolean init) {
    _date = 0L;
    _julian = INVALID_JULIAN;

    if (init) {
      today();
    }
  }

  // ----------------------------------------------------------------

  /**
   * Construct a BAMDate from a java.sql.Date object.
   * 
   */
  public BAMDate(Date d) {
	  
	  if (d == null) {
		  _julian = INVALID_JULIAN;
		  _date = 0;
	  } else {
		  cal.setTime(d);
		  int year = cal.get(Calendar.YEAR);
		  int month = cal.get(Calendar.MONTH) + 1;   // months start with 0 in this world
		  int day = cal.get(Calendar.DAY_OF_MONTH);
		    
		  if (month >= 1 && month <= 12 && year >= _julianBaseYear
				  && day >= 1 && day <= daysInMonth(month, year))
		      	_date = year*10000L+100L*month+day;
		   else
		      _date = 0;
	  }
  }
  
  // ----------------------------------------------------------------

  public BAMDate() {
    _date = 0L;
    _julian = INVALID_JULIAN;
  }

  // ----------------------------------------------------------------

  public BAMDate(int yyyymmdd) {
    _julian = INVALID_JULIAN;

    //
    // We actually allow either yymmdd or yyyymmdd.  Determine which
    // they are using and use the right set function.
    //
    if (yyyymmdd <= 991231)
	setFromYymmdd(yyyymmdd);
    else
	this._date = yyyymmdd;
  }

  // ---------------------------------------------------------------

  public BAMDate(BAMDate other) {
    this._date = other._date;
    this._julian = other._julian;
  }

  // ---------------------------------------------------------------

  public Object clone() {
      return new BAMDate(this);
  }

  // ----------------------------------------------------------------

  public BAMDate(int month, int day, int year) {
    _julian = INVALID_JULIAN;

    // Convert 2 digit year to 4 digit
    if (year < _pivotYear) {
	year += 2000;
    }
    else if (year >= _pivotYear && year < 100) {
	year += 1900;
    }
    
    if (month >= 1 && month <= 12 && year >= _julianBaseYear
	&& day >= 1 && day <= daysInMonth(month, year))
      _date = year*10000L+100L*month+day;
    else
      _date = 0;
  }

  // ----------------------------------------------------------------

  public BAMDate(String str, int format) throws NumberFormatException {
    set(str, format);
  }

  // ----------------------------------------------------------------

  public BAMDate(String str) throws NumberFormatException {
    set(str);
  }

  // ----------------------------------------------------------------

  public void today() {
    //
    // Make this date hold today's date.
    //
    Date d = new Date();
    set(d);
  }

  // ---------------------------------------------------------------

  public void endOfMonth() {
    int m = month();
    int y = year();
    int d = daysInMonth(m, y);
    set(m, d, y);
  }
       
  // ----------------------------------------------------------------

  public int dayOfYear() {
    //
    // Jan 1 = day # 1.
    //
    BAMDate jan1 = new BAMDate(1, 1, year());
    return (julianDate() - jan1.julianDate()) + 1;
  }

  // ---------------------------------------------------------------

  public int dayOfWeek() {
    //
    // We count on the julian base date being on the day of week
    // julian_base_dow.
    //
    return dayOfWeek(julianDate());
  }
 
  // ----------------------------------------------------------------

  public boolean isMonthEnd() {
    //
    // Return true if this date is the last date in this month.
    //
    return (day() == daysInMonth(month(), year()) ? true : false);
  }
  
  // -----------------------------------------------------------------

  public BAMDate addDays(int days) {
    //
    // After much analysis and hair-pulling it appears that adding 
    // days to the julian date is by far the way to go, even though
    // it involves up to two julian/date conversions.
    //
    if (isValid()) {
      _julian = julianDate() + days;
      _date = _julianToYyyymmdd(_julian);
    }

    return this;
  }

  // ----------------------------------------------------------------

  public BAMDate addMonths(int months, int eomMethod) {
	  //
	  // eom_method has a default value of MONTH_end_stick, which means
	  // we worry about sticking to the end of the month.
	  //
	  if (isValid()) {
		  int oldMonth = month();
		  int oldYear = year();
		  int oldDay = day();
		  int monthCount = (oldMonth-1)+12*oldYear+months;
		  int newMonth = monthCount%12+1;
		  int newYear = (monthCount-newMonth+1)/12;
		  int newDay = oldDay;
		  int newDim = daysInMonth(newMonth, newYear);

		  //
		  // See if we have to follow the stick-to-end-of-month rule.
		  //
		  if (eomMethod == MONTH_end_stick) {
			  //
			  // See if we started at the end of a month.  If we did,
			  // we need to finish there also.
			  //
			  if (oldDay == daysInMonth(oldMonth, oldYear))
				  newDay = newDim;
		  }

		  if (newDay > newDim)	// just in case this was a leap day
			  newDay = newDim;

		  set(newMonth, newDay, newYear);
	  }
	  return this;
  }

  // ----------------------------------------------------------------

  public BAMDate addYears(int years, int eomMethod) {
    //
    // eom_method has a default value of MONTH_end_stick.
    //
    if (isValid()) {
      int newDay = day();
      int newYear = (year() + years);
      int newDim = daysInMonth(month(), newYear);
    
      //
      // See if we have to follow the stick-to-end-of-month rule.
      //
      if (eomMethod == MONTH_end_stick) {
	//
	// See if we started at the end of a month.  If we did,
	// we need to finish there also.
	//
	if (day() == daysInMonth(month(), year()))
	  newDay = newDim;
      }

      if (newDay > newDim)	// just in case this was a leap day
	newDay = newDim;
    
      set(month(), newDay, newYear);			// asserts validity
    }
    return this;
  }

  // ----------------------------------------------------------------

  public BAMDate assignment(BAMDate other) {
    _date = other._date;
    _julian = other._julian;

    return this;
  }

  // ---------------------------------------------------------------

  public boolean between(BAMDate min, BAMDate max, int bet) {
    if (bet == BETWEEN_excl)
      return (_date > min._date) && (_date < max._date);
    else
      return (_date >= min._date) && (_date <= max._date);
  }
 
  // ---------------------------------------------------------------

  public String toString() {
      return get(FMT_Slash);
  }
  
  public String toJSONString() {
	  return new String("\"" + toString() + "\"");
  }

  // --------------------------------------------------------------- 
	/**
	   Converts BAMDate to a standard java.util.Date object.
	*/
	public Date toJavaDate() {
		Calendar _calendar = new GregorianCalendar(this.year(),
												   this.month() - 1,
												   this.day());
  
		return _calendar.getTime();
	}

	  // --------------------------------------------------------------- 
	
	/**
	 * Converts BAMDate to a standard java.sql.Date object.
	 */
	public java.sql.Date toJavaSQLDate() {
		Calendar _calendar = new GregorianCalendar(this.year(),
				this.month() - 1,
				this.day());
	  
		return new java.sql.Date(_calendar.getTimeInMillis());
	}

  // ---------------------------------------------------------------

  public String fmt(int format) {
    return get(format);
  }
 
  // --------------------------------------------------------------

  public String get(int format) {
    //
    // Insert into the given buffer an appropriate string of the
    // given format.  Buffer_len tells how much room we have.
    //

    String day_str;  
      
    switch (format) {
    case FMT_Slash :
      if (!isValid()) {
	return InvalidString;
      }
      return "" + month() + "/" + day() + "/" + year();

    case FMT_Slash0 :
      if (!isValid()) {
	return InvalidString;
      }
      return "" + pad(month()) + "/" + pad(day()) + "/" + year();
	    
    case FMT_Slash2 :
      if (!isValid()) {
	return "BAD DATE";
      }
      return "" + month() + "/" + day() + "/" +
	  pad(Integer.parseInt(shrink_year(year())));
	    
    case FMT_Slash02 :
      if (!isValid()) {
	return "BAD DATE";
      }
      return "" + pad(month()) + "/" + pad(day()) + "/" + pad_year(year());
	    
    case FMT_Long :
      if (!isValid()) {
	return "BAD DATE *****";
      }
      return "" + getDayName(FMT_Long) + ", " + getMonthName(FMT_Long) + " " +
	day() + ", " + year();
      	    
    case FMT_Short :
      if (!isValid()) {
	return "BAD DATE ****";
      }
      return "" + getMonthName(FMT_Short) + " " + day() + ", " + year();
	    
    case FMT_MonthDay :
      if (!isValid()) {
	return "DATE*";
      }
      return "" + month() + "/" + day();
	    
    case FMT_MonthYear :
      if (!isValid()) {
	return "DATE*";
      }
      return "" + month() + "/" + year();
	    
    case FMT_MonthYear2 :
      if (!isValid()) {
	return "DATE*";
      }
      return "" + month() + "/" + pad(Integer.parseInt((shrink_year(year()))));
      
    case FMT_MonthYear3 :
      if (!isValid()) {
	return "DATE*";
      }
      return "" + pad(month()) + shrink_year(year());

    case FMT_YYMMDD :
      if (!isValid()) {
	return "DATE*";
      }
      return "" + pad(Integer.parseInt(shrink_year(year()))) +
	  pad(month()) + pad(day());

    case FMT_YYYYMMDD :
      if (!isValid()) {
	return "BAD DATE";
      }
      return "" + year() + pad(month()) + pad(day());
	    
    case FMT_ISO :
      if (!isValid()) {
	return InvalidString;
      }
      return "" + year() + "-" + pad(month()) + "-" + pad(day());

    case FMT_Sybase :
      if (!isValid()) {
	return "BAD DATE***";
      }
      //return "" + getMonthName(FMT_Short) + " " + month() + " " + year() +
      day_str = String.valueOf(day());
      if (day_str.length() < 2) {
	  day_str = " " + day_str;
      }
      return "" + getMonthName(FMT_Short) + " " + day_str + " " + year() +
	  " 12:00:00:000AM";

    case FMT_SmSybase :
      if (!isValid()) {
	return "BAD DATE***";
      }
      //return "" + getMonthName(FMT_Short) + " " + month() + " " + year() +
      day_str = String.valueOf(day());
      if (day_str.length() < 2) {
	  day_str = " " + day_str;
      }
      return "" + getMonthName(FMT_Short) + " " + day_str + " " + year() +
	" 12:00AM";
    }
    return "BAD FORMAT*****";
  }

// --------------------------------------------------------------

  public int _stringCount(String buffer, char search) {
    //
    // Static local function.
    //
    // Used by the parseFromString method.
    //
    int found = 0;
    for (int i=0;i<buffer.length();i++)
      if (buffer.charAt(i) == search)
	found += 1;
    return found;
  }
  
// --------------------------------------------------------------
  
  public String pad(int num) {
    //
    // converts the num into a string and
    // pads it with a 0.
    //
    String retvalue = new String(Integer.toString(num));
    if (retvalue.length() < 2) {
	retvalue = "0" + retvalue;
    }
    return retvalue;
  }

  // --------------------------------------------------------------
  
  public String pad_year(int num) {
    //
    // converts the year into a two-digit string and
    // pads it with a 0. 
    //
    num = num%100;
    String retvalue = new String(Integer.toString(num));
    if (retvalue.length() < 2) {
	retvalue = "0" + retvalue;
    }
    return retvalue;
  }
  
   // --------------------------------------------------------------
   
   public String shrink_year(int num) {
    //
    // converts the year into a two digit year
    // and returns it as a string.
    //
    num = num%100;
    String retvalue = new String(Integer.toString(num));
    return retvalue;
  }
  
  // --------------------------------------------------------------
  /**
    * Static method.
    *
    * Tries to parse this string by figuring out the format itself.
    * Then calls the formatted version for the actual parsing.
    * Returns the yyyymmdd unsigned long for the date.
    * <br>
    * Heuristics:<br>
    * <br>
    *	    2 commas: FMT_Long. <br>
    *	    1 comma : FMT_Short. <br>
    *	    2 dashes : FMT_ISO <br>
    *	    2 slash characters: FMT_Slash <br>
    *	    1 slash character: No fully parsable formats. <br>
    *	    0 commas, 0 slash characters: <br>
    *		3 colons: FMT_Sybase <br>
    *		1 colon : FMT_SmSybase <br>
    *		If long(buffer) is <= 999999: FMT_YYMMDD. <br>
    *		If long(buffer) is >= 10000000: FMT_YYYYMMDD. <br>
    */
  int parseFromString(String buffer) throws NumberFormatException {
    // Return an invalid date for the default invalid string.
    if (buffer.equals(InvalidString))
      return 0;

    int commas = _stringCount(buffer, ',');
    int slashes = _stringCount(buffer, '/');
    int dashes = _stringCount(buffer, '-');

    if (dashes == 2)
      return parseFromString(buffer, FMT_ISO);
    if (commas == 2)
      return parseFromString(buffer, FMT_Long);
    else if (commas == 1)
      return parseFromString(buffer, FMT_Short);
    else if (commas == 0) {
      if (slashes == 1)
	return 0;
      else if (slashes == 2) {
	return parseFromString(buffer, FMT_Slash);
      } else if (slashes == 0) {
	int colons = _stringCount(buffer, ':');
	if (colons == 3)
	  return parseFromString(buffer, FMT_Sybase);
	else if (colons == 1)
	  return parseFromString(buffer, FMT_SmSybase);
	// the buffer contains a sequence without any comma, slash, colon
	if (buffer.length() <= 6)
	  return parseFromString(buffer, FMT_YYMMDD);
	else
	  return parseFromString(buffer, FMT_YYYYMMDD);
      }
    }
    
    return 0;
  }

  // --------------------------------------------------------------

  int parseFromString(String buffer, int format) throws NumberFormatException {
    //
    // Static method.
    //
    // Format has a default value of FMT_YYYYMMDD.  Returns the
    // unsigned long yyyymmdd of the date.
    //
    
    int m = 0;
    int d = 0;
    int y = 0; 
    int endDateSpacePos = -1;
    String monthName;
    
    switch (format) {
    case FMT_Slash :
      /* FALL THROUGH */
    case FMT_Slash0 :
      /* FALL THROUGH */
    case FMT_Slash2 :
      /* FALL THROUGH */
    case FMT_Slash02 :
      int slash1Pos = buffer.indexOf('/', 1);
      int slash2Pos = buffer.indexOf('/', slash1Pos + 1);
      endDateSpacePos = buffer.indexOf(' ', slash2Pos + 1);

  
      if (slash1Pos == -1 || slash2Pos == -1)
	return 0;

      m = Integer.parseInt(buffer.substring(0, slash1Pos));
      d = Integer.parseInt(buffer.substring(slash1Pos + 1, slash2Pos));
      if (endDateSpacePos == -1)
	y = Integer.parseInt(buffer.substring(slash2Pos + 1));
      else
	y = Integer.parseInt(buffer.substring(slash2Pos + 1,endDateSpacePos));
      
      if (y < _pivotYear)
	y += 2000;
      else if (y >= _pivotYear && y < 100)
	y += 1900;
      break;
	      
    case FMT_ISO :
      int dash1Pos = buffer.indexOf('-', 1);
      int dash2Pos = buffer.indexOf('-', dash1Pos + 1);
      endDateSpacePos = buffer.indexOf(' ', dash2Pos + 1);

  
      if (dash1Pos == -1 || dash2Pos == -2)
	return 0;

      y = Integer.parseInt(buffer.substring(0, dash1Pos));
      m = Integer.parseInt(buffer.substring(dash1Pos + 1, dash2Pos));
      if (endDateSpacePos == -1)
	d = Integer.parseInt(buffer.substring(dash2Pos + 1));
      else
	d = Integer.parseInt(buffer.substring(dash2Pos + 1, endDateSpacePos));
      
      if (y < _pivotYear)
	y += 2000;
      else if (y >= _pivotYear && y < 100)
	y += 1900;
      break;
	      
    case FMT_YYMMDD :
      endDateSpacePos = buffer.indexOf(' ', 5);
      y = Integer.parseInt(buffer.substring(0, 2));
      m = Integer.parseInt(buffer.substring(2, 4));
      if (endDateSpacePos == -1)
	d = Integer.parseInt(buffer.substring(4));
      else
	d = Integer.parseInt(buffer.substring(4, endDateSpacePos));
	    
      if (y < _pivotYear)
	y += 2000;
      else if (y >= _pivotYear && y < 100)
	y += 1900;
      break;
	    
    case FMT_YYYYMMDD :
      endDateSpacePos = buffer.indexOf(' ', 7);
      y = Integer.parseInt(buffer.substring(0, 4));
      m = Integer.parseInt(buffer.substring(4, 6));
      if (endDateSpacePos == -1)
	d = Integer.parseInt(buffer.substring(6));
      else
	d = Integer.parseInt(buffer.substring(6, endDateSpacePos));
      break;
      
    case FMT_Long :
      //
      // This is like "Monday, August 23, 1997"
      //
      int comma1Pos = buffer.indexOf(',', 1);
      int comma2Pos = buffer.indexOf(',', comma1Pos + 1);
  
      if (comma1Pos == -1 || comma2Pos == -1)
	return 0;

      int spaceAfterMonth = buffer.indexOf(' ', comma1Pos+2);
      monthName = buffer.substring(comma1Pos+2, spaceAfterMonth);
      endDateSpacePos = buffer.indexOf(' ', comma2Pos + 2);


      m = matchMonth(monthName, false);
      d = Integer.parseInt(buffer.substring(spaceAfterMonth+1, comma2Pos));
      if (endDateSpacePos == -1)
	y = Integer.parseInt(buffer.substring(comma2Pos+2));
      else
	y = Integer.parseInt(buffer.substring(comma2Pos+2), endDateSpacePos);
      break;

    case FMT_Short :
      //
      // This is like "Aug 23, 1997"
      //
      int commaPos = buffer.indexOf(',', 1);
      int space1Pos = buffer.indexOf(' ', 1);
      endDateSpacePos = buffer.indexOf(' ', commaPos + 2);
  
      if (commaPos == -1)
	return 0;

      monthName = buffer.substring(0, space1Pos);
      
      m = matchMonth(monthName, true);
      d = Integer.parseInt(buffer.substring(space1Pos+1, commaPos));
      if (endDateSpacePos == -1)
	y = Integer.parseInt(buffer.substring(commaPos+2));
      else
	y = Integer.parseInt(buffer.substring(commaPos+2), endDateSpacePos);
      break;
    
    case FMT_Sybase :
      /* FALL THROUGH */
    case FMT_SmSybase :
      //
      // This is like "Jul  1 2003 12:00:00:000AM" (normal)
      // or "Jul  1 2003 12:00AM" (small).  We can use the same
      // parsing code because we ignore the time component.
      // the first three components are the date, 
      // the last component is the time
      //
      StringTokenizer tok = new StringTokenizer(buffer);
      int num_fields = tok.countTokens();
      if (num_fields < 4)
	return 0;
      String month_tok = tok.nextToken();
      m = (new BAMDate(true)).matchMonth(month_tok, true);
      String day_tok = tok.nextToken();
      d = Integer.parseInt(day_tok);
      String year_tok = tok.nextToken();
      y = Integer.parseInt(year_tok);
      //time_tok = tok.nextToken();
      break;

    case FMT_MonthDay :
      /* FALL THROUGH */
    case FMT_MonthYear :
      /* FALL THROUGH */
    case FMT_MonthYear2 :
      return 0;
    default :
      break;    
    }

    //
    // Check that we have valid values.
    //
    if (y < _julianBaseYear)
      return 0;
    else if (m < 1 || m > 12)
      return 0;
    else if (d < 1 || d > daysInMonth(m, y))
      return 0;
    else
      return y*10000 + m*100 + d;
  }

  // ---------------------------------------------------------------
    
  public int matchMonth(String month, boolean format) {
    //
    // Returns the integer value of the month ("Jan" = 1).
    //
    for (int i = 0; i <= 11; i++) {
	if (format) {
	    if (sMonthNames[i].compareTo(month) == 0)
		return i+1;
	} else {
	    if (monthNames[i].compareTo(month) == 0)
		return i+1;
	}
    }
    return 0;
  }
    
  // ---------------------------------------------------------------
    	
  public String getMonthName(int format) {
    //
    // Returns the month name ("January" or "Jan", etc) of this date.
    //
    if (format == FMT_Long)
      return monthNames[month()-1];
    else
      return sMonthNames[month()-1];
  }
 
  // ----------------------------------------------------------------

  public String getDayName(int format) {
    //
    // Return the day name ("Monday", etc) of this date.
    //
    if (format == FMT_Long)
      return dayNames[dayOfWeek()];
    else
      return sDayNames[dayOfWeek()];
  }
 
  // -----------------------------------------------------------------

  public static int daysInMonth(int month, int year) {
    //
    // Just get the days in the month from the static array.
    // If we're in February, add one if it's a leap year.
    //
    int d = monthDays[month - 1];
    if (month == 2 && isLeapYear(year))
      d += 1;
    return d;
  }
 
  // -----------------------------------------------------------------

  public static int nthOfMonth(int dow, int n, int month, int year) {
    //
    // Returns the day of the month that is the nth weekday
    // of that month in a given year.  For instance, the 2nd
    // Tuesday of 4/97 is the 8th.
    //
    // How to do?  Find the first day of the month that is the
    // particular weekday, then add (n-1)*7 to it.
    //
    // If n == 0 or n == -1, this is a special case which indicates that we
    // want the last day of this dow in the month, eg, the
    // last Monday in May (which happens to be Labor Day).
    //
    // If you ask for a date that doesn't exist, ie, the 7th Tuesday
    // in July, 0 is returned.
    //
    BAMDate fake_date = new BAMDate(month, 1, year);
    int first_dow = fake_date.dayOfWeek();
    int day = 1;
    int i_dow =dow;
    
    if (i_dow > first_dow)
      day += (i_dow - first_dow);
    else if (i_dow < first_dow)
      day += (7 - first_dow + i_dow);


    if (n > 1) {
      day += (n-1)*7;
    } else if (n == 0 || n == -1) {
      //
      // Special case.  They want last one of this day in the month.
      //
      int days_in_m = daysInMonth(month, year);
      while ((day + 7) <= days_in_m)
	day += 7;
    }

    if (day > daysInMonth(month, year))
      day = 0;
    
    return day;
  }

  // -----------------------------------------------------------------

  public BAMDate nextDow(int dow, int n) {
    //
    // Find the next dow after this date.  For example, find the
    // first Friday after today.  If today is 7/13/98, the next
    // Friday is 7/17/98.
    //
    // If today is the same DOW as asked, you get the next one.
    // Example, asking for the next Friday on 7/17/98 gives you
    // 7/24/98.
    //
    // N = 1 means the next.  N > 1 means the next one, or the next
    // one, etc.
    //
    // N < 0 means the previous one, or the one before that, etc.
    //
    // N == 0 is not allowed.
    //
    // N has a default value of 1, meaning the next one.
    //

    //
    // Compute the next dow no matter what we're doing.
    //
    int this_dow = (int)dayOfWeek();
    int days_to_next = dow - this_dow;
    boolean same_dow = (days_to_next == 0);
    
    if (days_to_next <= 0)
      days_to_next += 7;

    _julian = julianDate() + days_to_next;

    //
    // Now if N < 0 or N > 1 correct.
    //
    if (n > 1)
      _julian += ((n-1) * 7);
    else if (n < 0) {
      _julian += (n * 7);
      if (same_dow)
	_julian -= 7;
    }

    _date = _julianToYyyymmdd(_julian);

    return this;
  }

  // -----------------------------------------------------------------

  private static long _yyyymmdd_to_julian(long yyyymmdd) {
    //
    // Julian date rooted at
    // 		julian_base_day/julian_base_month/julian_base_year.
    //
    long day = yyyymmdd % 100;
    long month = (yyyymmdd / 100) % 100;
    long year = yyyymmdd / 10000;

    long julian = day;			// start with day of month
    int i;

    //
    // Add days in the months for the rest of the year.
    //
    for (i = 1; i < month; ++i) {
      julian += daysInMonth(i, (int)year);
    }

    //
    // We have to add the days in all the years from julian_base_year
    // to the date.
    //
    // However, a large majority of our dates are in or after 1997, so
    // we can short-circuit that much of the loop and start in 1997
    // if we can.
    //
    int base_year = _julianBaseYear;

    if (year >= 1997) {
      base_year = 1997;
      julian += _julian_of_12_31_1996;
    }

    for (i = base_year; i < year; ++i) {
      julian += daysInYear(i);
    }
    return julian;
  }

  // ------------------------------------------------------------------

  private static long _julianToYyyymmdd(long julian) {
    //
    // Julian date rooted at 
    // 		julian_base_day/julian_base_month/julian_base_year.
    //
    int month, year;

    //
    // Another possible short-circuit.  If the julian date is after
    // 1/1/1997, we can start from there instead of the base
    // date.
    //
    if (julian > (_julian_of_12_31_1996+1)) {
      month = 1;
      year = 1997;
      julian -= _julian_of_12_31_1996;
    } else {
      month = _julian_base_month;
      year = _julianBaseYear;
    }
    
    long days = daysInYear(year);

    while (julian > days) {
      julian -= days;
      days = daysInYear(++year);
    }
    days = daysInMonth(month, year);
    while (julian > days) {
      julian -= days;
      days = daysInMonth(++month, year);
    }

    return year*10000L + 100L*month + julian;
  }

  // ------------------------------------------------------------------

  public void set(int yyyymmdd) {
    _date = yyyymmdd;
    _julian = INVALID_JULIAN;
  }

  // ----------------------------------------------------------------

  public void set(BAMDate d) {
    _date = d._date;
    _julian = d._julian;
  }

  // --------------------------------------------------------------

  public void set(int month, int day, int year) {      

    // Convert 2 digit year to 4 digit
    if (year < _pivotYear) {
	year += 2000;
    }
    else if (year >= _pivotYear && year < 100) {
	year += 1900;
    }

    //
    // If the m, d and y values are no good, set the _date variable
    // to 0, which will cause is_valid() to fail.
    //
    if (month >= 1 && month <= 12 && year >= _julianBaseYear
	&& day >= 1 && day <= daysInMonth(month, year))
      _date = year*10000L + 100L*month +day;
    else
      _date = 0;

    _julian = INVALID_JULIAN;
  }

  // ---------------------------------------------------------------

  public void set(String buffer, int format) throws NumberFormatException {
    //
    // The user specifies the string format to parse.
    //
    // If you want the constructor to figure out which format the string
    // is in, use the constructor below.
    //
    _date = parseFromString(buffer, format);
    _julian = INVALID_JULIAN;
  }

  // ----------------------------------------------------------------

  /**
    * Set from a Java <code>Date</code>.
    */
  public void set(Date d) {
    if (d != null) {
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(d);
      set(calendar);
    }
  }
  
  public void set(java.sql.Date d) {
	  set((java.util.Date)d);
  }

  /**
    * Set from a Java <code>Calendar</code>.
    */
  public void set(Calendar cal) {
    if (cal != null) {
      int day = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH);
      int year = cal.get(Calendar.YEAR);
      
      set(month+1, day, year);
    }
  }
  
  // ----------------------------------------------------------------

  public void set(String buffer) throws NumberFormatException {
    //
    // The format of the string is computed.
    //
    // If you want to specify the format, which is faster, use the
    // constructor above.
    //
    _date = parseFromString(buffer);
    _julian = INVALID_JULIAN;
  }

  // ----------------------------------------------------------------

  public void setFromJulian(int julian) {
    _julian = julian;
    _date = _julianToYyyymmdd(julian);
  }

  // ---------------------------------------------------------------

  public void setFromYymmdd(int yymmdd) {
    _date = yymmdd;
    _date += (_date >= _yyPivotDate ? 19000000L : 20000000L);
    _julian = INVALID_JULIAN;
  }

  // ---------------------------------------------------------------

  public void firstOfMonth() {
    set(month(), 1, year());
  }
       
  // --------------------------------------------------------------

  public int month() {
    return ((int)_date / 100) % 100;
  }
 
  // --------------------------------------------------------------

  public int day() {
    return (int)_date % 100;
  }
 
  // --------------------------------------------------------------

  public int year() {
    return (int)_date / 10000;
  }
 
  // ----------------------------------------------------------------

  public int julianDate() {
    //
    // Return the julian date, computed from the julian base date.
    //
    if (_julian == INVALID_JULIAN)
      _julian = _yyyymmdd_to_julian(_date);
    return (int)_julian;
  }
 
  // ----------------------------------------------------------------

  public long asLong() {
      return _date;
  }
 
  // ----------------------------------------------------------------

  public long hash() {
    return _date;
  }
   
  // --------------------------------------------------------------

  public int hashCode() {
    return (int)_date;
  }
   
  // --------------------------------------------------------------

  public boolean isValid() {
    //
    // Well, what's valid.  How about _date is set and has the date
    // after the julian base date but before 12/31/2222?
    //
    // Made this 2222 so that we can use 2/2/2222, which is a common
    // date in the analytics.
    //
    return !(_date < _julian_base || _date > 22221231);
  }
 
  // ----------------------------------------------------------------

  public BAMDate incr() {
    return addDays(1);
  }

  // ----------------------------------------------------------------

  public BAMDate decr() {
    return addDays(-1);
  }

  // ----------------------------------------------------------------

  public boolean equals(Object o)
    {
      if ((o == null) || !(o instanceof BAMDate))
	return false;

      return eq((BAMDate)o);
    }
/**
  public int compareTo(BAMDate o)
    {
      if (o instanceof BAMDate)
	return compareTo((BAMDate)o);

      if (o == null)
	return 1;
      
      return getClass().hashCode() - o.getClass().hashCode();
    }
**/
  public int compareTo(BAMDate d)
    {
      if (d == null)
	return 1;

      return((int)(_date - d._date));
    }
  
  public boolean lt(BAMDate other) {
    return _date < other._date;
  }

  public boolean before(BAMDate other) {
    return _date < other._date;
  }
  
  public boolean gt(BAMDate other) {
    return _date > other._date;
  }
  
  public boolean after(BAMDate other) {
    return _date > other._date;
  }

  public boolean eq(BAMDate other) {
    return _date == other._date;
  }

  public boolean ne(BAMDate other) {
    return _date != other._date;
  }
  
  public boolean le(BAMDate other) {
    return _date <= other._date;
  }
  
  public boolean onOrBefore(BAMDate other) {
    return _date <= other._date;
  }

  public boolean ge(BAMDate other) {
    return _date >= other._date;
  }

  public boolean onOrAfter(BAMDate other) {
    return _date >= other._date;
  }

  // ---------------------------------------------------------------

  public static int daysInYear(int year) {
    //
    // There are 365 days in all years except for leap years.
    //
    return (isLeapYear(year) ? 366 : 365);
  }
  
  // -----------------------------------------------------------------

  public static int dayOfWeek(int julian) {
    //
    // Returns the dow code for the julian date (0 = Sunday).
    //
    // This is based on the fact that the julian base date had
    // a day of the week = julian_base_dow.
    //
    return ((julian % 7) + ((_julian_base_dow + 6) % 7)) % 7;
  }
 
  // ---------------------------------------------------------------

  public static boolean isLeapYear(int year) {
    //
    // Every fourth year is a leap year except for century years not
    // divisible by 400.
    //
    return (year%4 == 0 && (year%400 == 0 || year%100 != 0));
  }

}
