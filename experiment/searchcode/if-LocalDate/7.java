package com.chase.sig.android.util;

import android.content.Context;
import android.content.res.Resources;
import com.google.common.a.a;
import com.google.common.a.d;
import com.google.common.a.e;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

public final class s
{
  private static final SimpleDateFormat a = new SimpleDateFormat("h:mm a z M/d/yyyy");
  private static final SimpleDateFormat b = new SimpleDateFormat("M/d/yyyy");
  private static final SimpleDateFormat c = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
  private static final SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private static final SimpleDateFormat e = new SimpleDateFormat("h:mm a' ET on 'MMMM dd, yyyy");
  private static final Pattern f = Pattern.compile("\\d{5}(-\\d{4})?");
  private static final Pattern g = Pattern.compile("^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$");
  private static final NumberFormat h = NumberFormat.getCurrencyInstance(Locale.US);

  static
  {
    a.setTimeZone(l.a());
    d.setTimeZone(l.a());
    e.setTimeZone(l.a());
  }

  public static boolean A(String paramString)
  {
    return Pattern.compile(".*\\<[^>]+>.*", 8).matcher(paramString).find();
  }

  public static boolean B(String paramString)
  {
    return Integer.parseInt(paramString) == 0;
  }

  public static String C(String paramString)
  {
    return new BigDecimal(paramString).setScale(2, 5).toString();
  }

  public static String D(String paramString)
  {
    try
    {
      URL localURL = new URL(paramString);
      String str = new URI(localURL.getProtocol(), localURL.getUserInfo(), localURL.getHost(), localURL.getPort(), localURL.getPath(), localURL.getQuery(), localURL.getRef()).toASCIIString();
      return str;
    }
    catch (MalformedURLException localMalformedURLException)
    {
      return "";
    }
    catch (URISyntaxException localURISyntaxException)
    {
    }
    return "";
  }

  private static Date E(String paramString)
  {
    try
    {
      Date localDate = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZZ").parse(paramString);
      return localDate;
    }
    catch (ParseException localParseException)
    {
    }
    return null;
  }

  public static String a()
  {
    Date localDate = new Date();
    return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(localDate);
  }

  public static String a(Context paramContext, int paramInt, Object[] paramArrayOfObject)
  {
    return String.format(paramContext.getResources().getString(paramInt), paramArrayOfObject);
  }

  public static String a(Dollar paramDollar)
  {
    if (paramDollar.b() == null)
      return "--";
    return paramDollar.h();
  }

  private static String a(CharSequence paramCharSequence)
  {
    if (paramCharSequence == null);
    for (String str = null; l(str); str = paramCharSequence.toString())
      return "--";
    return paramCharSequence.toString();
  }

  public static String a(Double paramDouble)
  {
    return a(paramDouble, null);
  }

  public static String a(Double paramDouble, String paramString)
  {
    if (paramString == null)
      paramString = "0.000000";
    if ((paramDouble == null) || (paramDouble.isNaN()))
      return "--";
    return new DecimalFormat(paramString).format(paramDouble);
  }

  public static String a(Object paramObject)
  {
    if (paramObject == null);
    for (Object localObject = null; ; localObject = paramObject.toString())
      return a((CharSequence)localObject);
  }

  public static String a(String paramString1, String paramString2)
  {
    if ((e.b(paramString1)) || (e.b(paramString2)));
    while (!paramString1.endsWith(paramString2))
      return paramString1;
    return paramString1.substring(0, paramString1.lastIndexOf(paramString2));
  }

  public static String a(String paramString, String[] paramArrayOfString)
  {
    List localList = Arrays.asList(paramArrayOfString);
    a locala = new a((byte)0);
    com.google.common.a.c.a(localList);
    com.google.common.a.c.a(locala);
    com.google.common.b.c localc = new com.google.common.b.c(localList, locala);
    return a.a(paramString).a(localc);
  }

  public static String a(Date paramDate)
  {
    if (paramDate == null)
      return "";
    return new SimpleDateFormat("MMM dd, yyyy").format(paramDate);
  }

  public static String a(JSONObject paramJSONObject, String paramString)
  {
    String str1 = "";
    if ((paramJSONObject != null) && (paramString != null))
    {
      String str2 = paramJSONObject.getString(paramString);
      if ((str2 != null) && (!str2.trim().equalsIgnoreCase("null")))
        str1 = str2.trim();
    }
    return str1;
  }

  public static Date a(String paramString)
  {
    try
    {
      Date localDate = new SimpleDateFormat("yyyyMMdd").parse(paramString);
      return localDate;
    }
    catch (ParseException localParseException)
    {
    }
    return null;
  }

  public static String b()
  {
    Date localDate = new Date();
    return new SimpleDateFormat("MM/dd/yyyy").format(localDate);
  }

  public static String b(String paramString1, String paramString2)
  {
    if ((e.b(paramString1)) || (e.b(paramString2)));
    while (!paramString1.startsWith(paramString2))
      return paramString1;
    return paramString1.substring(1 + paramString1.indexOf(paramString2));
  }

  public static String b(Date paramDate)
  {
    return new SimpleDateFormat("yyyyMMdd").format(paramDate);
  }

  public static Date b(String paramString)
  {
    try
    {
      Date localDate = c.parse(paramString);
      return localDate;
    }
    catch (ParseException localParseException)
    {
    }
    return null;
  }

  public static String c(String paramString)
  {
    Date localDate = E(paramString);
    if (localDate == null)
      return "";
    return a.format(localDate);
  }

  public static String c(Date paramDate)
  {
    return new SimpleDateFormat("MM/dd/yyyy").format(paramDate);
  }

  private static Date c(String paramString1, String paramString2)
  {
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString2);
    try
    {
      Date localDate = localSimpleDateFormat.parse(paramString1);
      return localDate;
    }
    catch (ParseException localParseException)
    {
      new Object[] { paramString1, paramString2 };
    }
    return null;
  }

  public static String d(String paramString)
  {
    Date localDate = a(paramString);
    if (localDate == null)
      return "";
    return b.format(localDate);
  }

  public static String d(Date paramDate)
  {
    return new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZZ").format(paramDate);
  }

  public static Date e(String paramString)
  {
    return c(paramString, "MMM dd, yyyy");
  }

  public static String f(String paramString)
  {
    if (paramString == null)
      paramString = "";
    while (paramString.length() != 10)
      return paramString;
    Formatter localFormatter = new Formatter();
    Object[] arrayOfObject = new Object[3];
    arrayOfObject[0] = paramString.substring(0, 3);
    arrayOfObject[1] = paramString.substring(3, 6);
    arrayOfObject[2] = paramString.substring(6);
    return localFormatter.format("(%s) %s-%s", arrayOfObject).toString();
  }

  public static Date g(String paramString)
  {
    return c(paramString, "yyyyMMdd");
  }

  public static String h(String paramString)
  {
    return a(c(paramString, "yyyyMMdd"));
  }

  public static String i(String paramString)
  {
    SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat("MMM dd, yyyy");
    if ((paramString != null) && (!paramString.equals("")))
      try
      {
        String str = localSimpleDateFormat2.format(localSimpleDateFormat1.parse(paramString));
        return str;
      }
      catch (ParseException localParseException)
      {
        new Object[] { paramString };
      }
    return null;
  }

  public static String j(String paramString)
  {
    if ((paramString != null) && (!paramString.equals("")))
      try
      {
        Date localDate = new SimpleDateFormat("yyyyMMdd").parse(paramString);
        String str = new SimpleDateFormat("MM/dd/yyyy").format(localDate);
        return str;
      }
      catch (ParseException localParseException)
      {
        new Object[] { paramString };
      }
    return null;
  }

  public static String k(String paramString)
  {
    if (paramString == null)
      return "--";
    try
    {
      String str = new SimpleDateFormat("M/d/yyyy").format(new SimpleDateFormat("yyyyMMdd").parse(paramString));
      return str;
    }
    catch (ParseException localParseException)
    {
    }
    return "--";
  }

  public static boolean l(String paramString)
  {
    return (e.b(paramString)) || ("null".equalsIgnoreCase(paramString.trim()));
  }

  public static boolean m(String paramString)
  {
    return !l(paramString);
  }

  public static boolean n(String paramString)
  {
    return e.a(paramString).trim().equals("");
  }

  public static boolean o(String paramString)
  {
    return !n(paramString);
  }

  public static String p(String paramString)
  {
    String str = paramString.replaceAll("<br>", "").replaceAll("</br>", "").replaceAll("<p>", "").replaceAll("</p>", "").replace("<em>", "").replace("</em>", "").replace("\t", "").replace("\n", "");
    return str + " ";
  }

  public static String q(String paramString)
  {
    return paramString.replaceFirst("^", "<b>").replaceFirst("$", "</b>").replace("<i>", "</b><i>").replace("</i>", "</i><b>");
  }

  public static String r(String paramString)
  {
    return paramString.replace("<sup>", "<small><sup><small>").replace("<\\/sup>", "<\\/small><\\/sup><\\/small>").replace("</sup>", "</small></sup></small>").replace("<sub>", "<sub><small>").replace("<\\/sub>", "<\\/small><\\/sub>").replace("</sub>", "</small></sub>");
  }

  public static String s(String paramString)
  {
    if (l(paramString))
      return "";
    return paramString.trim();
  }

  public static Date t(String paramString)
  {
    try
    {
      Date localDate = new Date(paramString);
      return localDate;
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    return null;
  }

  public static boolean u(String paramString)
  {
    return (m(paramString)) && (f.matcher(paramString).matches());
  }

  public static boolean v(String paramString)
  {
    return (m(paramString)) && (g.matcher(paramString).matches());
  }

  public static String w(String paramString)
  {
    return a(c(paramString));
  }

  public static String x(String paramString)
  {
    if (paramString.length() == 9)
    {
      Object[] arrayOfObject = new Object[2];
      arrayOfObject[0] = paramString.substring(0, 5);
      arrayOfObject[1] = paramString.substring(5);
      paramString = String.format("%s-%s", arrayOfObject);
    }
    return paramString;
  }

  public static String y(String paramString)
  {
    if (m(paramString))
      paramString = C(paramString);
    return paramString;
  }

  public static String z(String paramString)
  {
    return String.format("<sup><small>%s</small></sup>", new Object[] { paramString });
  }

  private static final class a
    implements d<String>
  {
  }
}

/* Location:           D:\code\Research\Android\apks\com.chase.sig.android-14\com.chase.sig.android-14_dex2jar.jar
 * Qualified Name:     com.chase.sig.android.util.s
 * JD-Core Version:    0.6.2
 */
