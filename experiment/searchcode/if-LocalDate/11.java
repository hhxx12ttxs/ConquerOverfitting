package net.flixster.android.model;

import com.flixster.android.utils.Logger;
import com.flixster.android.utils.UrlHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import net.flixster.android.FlixsterApplication;
import org.json.JSONObject;

public class Listing
{
  private static final String ASTERISK = "*";

  @Deprecated
  public static final String CINEMASOURCE_LISTING = "listing";

  @Deprecated
  public static final String CINEMASOURCE_THEATER = "theater";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  private static final SimpleDateFormat DAY_IN_WEEK_FORMAT = new SimpleDateFormat("EEEE");
  private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("EEEE, MMM d");
  private Date date;
  public final String dateString;
  private Date dateTime;
  public final String displayTime;

  @Deprecated
  public HashMap<String, String> paramMap;
  public final String ticketUrl;
  public final String timeString;

  public Listing(JSONObject paramJSONObject)
  {
    this.displayTime = paramJSONObject.optString("time");
    this.dateString = paramJSONObject.optString("date");
    this.ticketUrl = paramJSONObject.optString("ticket", null);
    if (this.ticketUrl == null);
    for (String str = ""; ; str = UrlHelper.getSingleQueryValue(this.ticketUrl, "perft").replaceAll(":", ""))
    {
      this.timeString = str;
      return;
    }
  }

  private Date getDate()
  {
    if (this.date == null)
      try
      {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.setTime(DATE_FORMAT.parse(this.dateString));
        Date localDate = localCalendar.getTime();
        this.date = localDate;
        return localDate;
      }
      catch (ParseException localParseException)
      {
        Logger.e("FlxMain", "Listing.getDate ParseException", localParseException);
        return null;
      }
    return this.date;
  }

  private Date getDateTime()
  {
    if (this.dateTime == null)
    {
      Date localDate1 = getDate();
      if (localDate1 == null)
        return null;
      Calendar localCalendar = Calendar.getInstance();
      localCalendar.setTime(localDate1);
      int i = Integer.valueOf(this.timeString.substring(0, 2)).intValue();
      int j = Integer.valueOf(this.timeString.substring(2, 4)).intValue();
      localCalendar.set(11, i);
      localCalendar.set(12, j);
      Date localDate2 = localCalendar.getTime();
      this.dateTime = localDate2;
      return localDate2;
    }
    return this.dateTime;
  }

  private String getDisplayDate()
  {
    Date localDate = getDate();
    if (localDate == null)
      return "";
    return DISPLAY_DATE_FORMAT.format(localDate);
  }

  private String getDisplayTime()
  {
    if (isMidnight())
      return this.displayTime + "*";
    return this.displayTime;
  }

  private boolean isMidnight()
  {
    return (this.ticketUrl != null) && (Integer.parseInt(this.timeString) >= 2400);
  }

  public String getDisplayTimeDate()
  {
    return getDisplayTime() + " " + getDisplayDate();
  }

  public String getMidnightDisclaimer()
  {
    if ((isMidnight()) && (getDateTime() != null))
    {
      StringBuilder localStringBuilder = new StringBuilder("*");
      localStringBuilder.append(" ");
      localStringBuilder.append(DAY_IN_WEEK_FORMAT.format(getDate()));
      localStringBuilder.append(" night / ");
      localStringBuilder.append(DAY_IN_WEEK_FORMAT.format(getDateTime()));
      localStringBuilder.append(" morning");
      return localStringBuilder.toString();
    }
    return null;
  }

  public boolean hasElapsed()
  {
    Date localDate = getDateTime();
    return (localDate == null) || (FlixsterApplication.sToday.after(localDate));
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     net.flixster.android.model.Listing
 * JD-Core Version:    0.6.2
 */
