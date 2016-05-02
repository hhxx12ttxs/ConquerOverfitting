package com.app.framework.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
@SuppressWarnings("unused")
public class CommonUtil
{

private static final String Content_Type_GIF = "image/gif";
  private static final String Content_Type_JPG = "image/jpeg";
  private static final String Content_Type_PNG = "image/png";
  public static int count = 0;
  public static int errorCount = 0;

  public static byte[] byteDecompress(byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte1 = null;
    try
    {
      GZIPInputStream localGZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(paramArrayOfByte));
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
      byte[] arrayOfByte2 = new byte[1024];
      while (true)
      {
        int i = localGZIPInputStream.read(arrayOfByte2, 0, arrayOfByte2.length);
        arrayOfByte1 = null;
        if (i == -1)
        {
          localGZIPInputStream.close();
          arrayOfByte1 = localByteArrayOutputStream.toByteArray();
          localByteArrayOutputStream.flush();
          localByteArrayOutputStream.close();
          return arrayOfByte1;
        }
        localByteArrayOutputStream.write(arrayOfByte2, 0, i);
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return arrayOfByte1;
  }

  public static void clearCollection(Collection<?> paramCollection)
  {
    if (paramCollection != null)
      paramCollection.clear();
  }

  public static int convertDIP2PX(Context paramContext, int paramInt)
  {
    try
    {
      float f1 = paramContext.getResources().getDisplayMetrics().density;
      float f2 = f1 * paramInt;
      if (paramInt >= 0);
      for (int i = 1; ; i = -1)
        return (int)(f2 + 0.5F * i);
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return paramInt;
  }

  public static String decrypt(String paramString1, String paramString2)
  {
    try
    {
      DESKeySpec localDESKeySpec = new DESKeySpec(paramString2.getBytes("UTF-8"));
      SecretKey localSecretKey = SecretKeyFactory.getInstance("DES").generateSecret(localDESKeySpec);
      Cipher localCipher = Cipher.getInstance("DES");
      localCipher.init(2, localSecretKey);
      String str = new String(localCipher.doFinal(Base64.decode(paramString1, 0)), "UTF-8");
      return str;
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return null;
  }

  public static String encrypt(String paramString1, String paramString2)
  {
    try
    {
      DESKeySpec localDESKeySpec = new DESKeySpec(paramString2.getBytes("UTF-8"));
      SecretKey localSecretKey = SecretKeyFactory.getInstance("DES").generateSecret(localDESKeySpec);
      byte[] arrayOfByte = paramString1.getBytes("UTF-8");
      Cipher localCipher = Cipher.getInstance("DES");
      localCipher.init(1, localSecretKey);
      String str = Base64.encodeToString(localCipher.doFinal(arrayOfByte), 0);
      return str;
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return null;
  }

  public static String formatRangeSize(int paramInt)
  {
    if (paramInt < 1000)
    {
      Object[] arrayOfObject2 = new Object[1];
      arrayOfObject2[0] = Integer.valueOf(paramInt);
      return String.format("%dm", arrayOfObject2);
    }
    Object[] arrayOfObject1 = new Object[1];
    arrayOfObject1[0] = Float.valueOf(paramInt / 1000.0F);
    return String.format("%.1fkm", arrayOfObject1);
  }

  public static String getAppVersionDate(Context paramContext)
  {
    try
    {
      String str1 = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
      if (str1 != null)
      {
        int i = str1.length();
        if (i > 0);
      }
      else
      {
        return "";
      }
      String str2 = str1.substring(1 + str1.indexOf("("), str1.indexOf(")"));
      return str2;
    }
    catch (Exception localException)
    {
      Log.e("VersionInfo", "Exception", localException);
    }
    return "";
  }

  public static String getAppVersionName(Context paramContext)
  {
    String str1 = "";
    try
    {
      str1 = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
      if (str1 != null)
      {
        int i = str1.length();
        if (i > 0);
      }
      else
      {
        return "";
      }
      String str2 = str1.substring(0, str1.indexOf("("));
      return str2;
    }
    catch (Exception localException)
    {
      Log.e("VersionInfo", "Exception", localException);
    }
    return str1;
  }

  public static String getDateString(long paramLong)
  {
    Date localDate = new Date(paramLong);
    return new SimpleDateFormat("yyyy/MM/dd").format(localDate);
  }

  public static String getDir(Context paramContext, int paramInt)
  {
    if (((paramInt >= 338) && (paramInt <= 360)) || ((paramInt >= 0) && (paramInt < 23)))
      return paramContext.getString(2131230852);
    if ((paramInt >= 23) && (paramInt < 68))
      return paramContext.getString(2131230855);
    if ((paramInt >= 68) && (paramInt < 113))
      return paramContext.getString(2131230851);
    if ((paramInt >= 113) && (paramInt < 158))
      return paramContext.getString(2131230856);
    if ((paramInt >= 158) && (paramInt < 203))
      return paramContext.getString(2131230853);
    if ((paramInt >= 203) && (paramInt < 248))
      return paramContext.getString(2131230858);
    if ((paramInt >= 248) && (paramInt < 293))
      return paramContext.getString(2131230854);
    return paramContext.getString(2131230857);
  }

  public static String getFileMimeType(String paramString)
  {
    if ((paramString.toLowerCase().endsWith(".jpg")) || (paramString.toLowerCase().endsWith(".jpeg")))
      return "image/jpeg";
    if (paramString.toLowerCase().endsWith(".png"))
      return "image/png";
    if (paramString.toLowerCase().endsWith(".gif"))
      return "image/gif";
    throw new IllegalArgumentException("not a image file");
  }

  public static final String getFileSize(long paramLong)
  {
    if (paramLong > 1073741824L)
    {
      Object[] arrayOfObject3 = new Object[1];
      arrayOfObject3[0] = Double.valueOf(paramLong / 1073741824.0D);
      return String.format("%.2f", arrayOfObject3) + " GB";
    }
    if (paramLong > 1048576L)
    {
      Object[] arrayOfObject2 = new Object[1];
      arrayOfObject2[0] = Double.valueOf(paramLong / 1048576.0D);
      return String.format("%.2f", arrayOfObject2) + " MB";
    }
    if (paramLong > 1024L)
    {
      Object[] arrayOfObject1 = new Object[1];
      arrayOfObject1[0] = Double.valueOf(paramLong / 1024.0D);
      return String.format("%.2f", arrayOfObject1) + " KB";
    }
    return paramLong + " B";
  }

  public static SpannableString getHtmlText(String paramString1, String paramString2)
  {
    SpannableString localSpannableString = new SpannableString(paramString1 + "(" + paramString2 + ")");
    localSpannableString.setSpan(new ForegroundColorSpan(-7829368), paramString1.length(), 2 + (paramString1.length() + paramString2.length()), 17);
    return localSpannableString;
  }

  @SuppressWarnings("deprecation")
public static Point getScreentDimention(Activity paramActivity)
  {
    Display localDisplay = ((WindowManager)paramActivity.getSystemService("window")).getDefaultDisplay();
    Point localPoint = new Point();
    localPoint.x = localDisplay.getWidth();
    localPoint.y = localDisplay.getHeight();
    new StringBuilder("width=").append(localPoint.x).append(", height=").append(localPoint.y).toString();
    return localPoint;
  }
/*
 
  // ERROR //
  public static Object getSerializableObject(Context paramContext, String paramString)
  {
    // Byte code:
    //   0: ldc 2
    //   2: monitorenter
    //   3: aload_0
    //   4: aload_1
    //   5: invokevirtual 380	android/content/Context:getFileStreamPath	(Ljava/lang/String;)Ljava/io/File;
    //   8: astore 7
    //   10: aload 7
    //   12: invokevirtual 386	java/io/File:exists	()Z
    //   15: istore 8
    //   17: iload 8
    //   19: ifne +10 -> 29
    //   22: aconst_null
    //   23: astore_3
    //   24: ldc 2
    //   26: monitorexit
    //   27: aload_3
    //   28: areturn
    //   29: new 388	java/io/ObjectInputStream
    //   32: dup
    //   33: new 390	java/io/FileInputStream
    //   36: dup
    //   37: aload 7
    //   39: invokespecial 393	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   42: invokespecial 394	java/io/ObjectInputStream:<init>	(Ljava/io/InputStream;)V
    //   45: invokevirtual 398	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
    //   48: astore 9
    //   50: aload 9
    //   52: astore_3
    //   53: goto -29 -> 24
    //   56: astore 6
    //   58: aload 6
    //   60: invokevirtual 399	java/io/FileNotFoundException:printStackTrace	()V
    //   63: aconst_null
    //   64: astore_3
    //   65: goto -41 -> 24
    //   68: astore 4
    //   70: ldc 2
    //   72: monitorexit
    //   73: aload 4
    //   75: athrow
    //   76: astore 5
    //   78: aload 5
    //   80: invokevirtual 400	java/io/IOException:printStackTrace	()V
    //   83: aconst_null
    //   84: astore_3
    //   85: goto -61 -> 24
    //   88: astore_2
    //   89: aload_2
    //   90: invokevirtual 401	java/lang/ClassNotFoundException:printStackTrace	()V
    //   93: aconst_null
    //   94: astore_3
    //   95: goto -71 -> 24
    //
    // Exception table:
    //   from	to	target	type
    //   3	17	56	java/io/FileNotFoundException
    //   29	50	56	java/io/FileNotFoundException
    //   3	17	68	finally
    //   29	50	68	finally
    //   58	63	68	finally
    //   78	83	68	finally
    //   89	93	68	finally
    //   3	17	76	java/io/IOException
    //   29	50	76	java/io/IOException
    //   3	17	88	java/lang/ClassNotFoundException
    //   29	50	88	java/lang/ClassNotFoundException
  }
* 
 */
  public static String getStatus(Context paramContext, int paramInt)
  {
    switch (paramInt)
    {
    default:
      return "";
    case 1:
      return paramContext.getString(2131230828);
    case 4:
      return paramContext.getString(2131230829);
    case 3:
      return paramContext.getString(2131230831);
    case 2:
    }
    return paramContext.getString(2131230827);
  }

  public static String getTimeNumStr(long paramLong)
  {
    if (paramLong < 60L)
    {
      Object[] arrayOfObject3 = new Object[1];
      arrayOfObject3[0] = Long.valueOf(paramLong);
      return String.format("%dsec", arrayOfObject3);
    }
    if (paramLong < 3600L)
    {
      Object[] arrayOfObject2 = new Object[1];
      arrayOfObject2[0] = Long.valueOf(paramLong / 60L);
      return String.format("%dmin", arrayOfObject2);
    }
    Object[] arrayOfObject1 = new Object[1];
    arrayOfObject1[0] = Float.valueOf((float)paramLong / 3600.0F);
    return String.format("%.1fh", arrayOfObject1);
  }

  public static String getTimeString()
  {
    return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
  }

  public static String getTimeString(long paramLong)
  {
    Date localDate = new Date(paramLong);
    return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(localDate);
  }

  public static String getVerticalText(String paramString)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; ; i++)
    {
      if (i >= paramString.length())
        return localStringBuilder.toString();
      localStringBuilder.append(paramString.charAt(i)).append("\n");
    }
  }

  public static boolean isAlphanumeric(String paramString)
  {
    return Pattern.compile("[a-zA-Z0-9]+").matcher(paramString).matches();
  }

  public static boolean isEmptyString(String paramString)
  {
    return (paramString == null) || (paramString.length() == 0);
  }

  public static boolean isInstalledOnSdCard(Context paramContext)
  {
    PackageManager localPackageManager = null;
    if (Build.VERSION.SDK_INT > 7)
      localPackageManager = paramContext.getPackageManager();
    while (true)
    {
      try
      {
        int i = localPackageManager.getPackageInfo(paramContext.getPackageName(), 0).applicationInfo.flags;
        return (i & 0x40000) == 262144;
      }
      catch (PackageManager.NameNotFoundException localNameNotFoundException)
      {
      }
      try
      {
        String str = paramContext.getFilesDir().getAbsolutePath();
        if (str.startsWith("/data/"))
          return false;
        if (str.contains("/mnt/"))
          continue;
        boolean bool = str.contains("/sdcard/");
        if (bool)
          continue;
        return false;
      }
      catch (Throwable localThrowable)
      {
       // break label92;
      }
    }
  }

  public static boolean isInteger(String paramString)
  {
    int i = paramString.length();
    for (int j = 0; ; j++)
    {
      boolean bool1;
      if (j >= i)
      {
        bool1 = false;
        if (i > 0)
          bool1 = true;
      }
      boolean bool2;
      do
      {
        //return bool1;
        bool2 = Character.isDigit(paramString.charAt(j));
        bool1 = false;
      }
      while (!bool2);
    }
  }

  public static boolean isNetworkConnected(Context paramContext)
  {
    NetworkInfo localNetworkInfo = ((ConnectivityManager)paramContext.getSystemService("connectivity")).getActiveNetworkInfo();
    return (localNetworkInfo == null) || (!localNetworkInfo.isConnected());
  }

  public static int isOnService(String paramString)
  {
    String[] arrayOfString1 = null;
    int i = 0;
    if (!TextUtils.isEmpty(paramString))
    {
      arrayOfString1 = paramString.split(",");
      i = arrayOfString1.length;
    }
    for (int j = 0; ; j++)
    {
      if (j >= i)
        return 0;
      String[] arrayOfString2 = arrayOfString1[j].split("-");
      if (arrayOfString2.length >= 2)
        return isOnService(arrayOfString2[0], arrayOfString2[1]);
    }
  }

  private static int isOnService(String paramString1, String paramString2)
  {
    try
    {
      SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("HH:mm");
      Date localDate1 = localSimpleDateFormat.parse(paramString1);
      Date localDate2 = localSimpleDateFormat.parse(paramString2);
      Calendar localCalendar1 = Calendar.getInstance();
      Calendar localCalendar2 = Calendar.getInstance();
      Calendar localCalendar3 = Calendar.getInstance();
      localCalendar1.setTime(localDate1);
      localCalendar2.setTime(localDate2);
      int i = localCalendar1.get(11);
      int j = localCalendar1.get(12);
      long l1 = i * 3600 + j * 60;
      int k = localCalendar2.get(11);
      int m = localCalendar2.get(12);
      long l2 = 600 + (k * 3600 + m * 60);
      if (l2 < l1)
        l2 += 86400L;
      int n = localCalendar3.get(11);
      int i1 = localCalendar3.get(12);
      long l3 = n * 3600 + i1 * 60;
      if ((l3 >= l1) && (l3 <= l2))
        return 0;
      if (l3 < l1)
        return -1;
      return 1;
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return 0;
  }

  public static String readableFileSize(long paramLong)
  {
    if (paramLong <= 0L)
      return "0";
    String[] arrayOfString = { "B", "KB", "MB", "GB", "TB" };
    int i = (int)(Math.log10(paramLong) / Math.log10(1024.0D));
    return new DecimalFormat("#,##0.#").format(paramLong / Math.pow(1024.0D, i)) + arrayOfString[i];
  }

  public static void saveErrorLog(String paramString)
  {
    try
    {
      File localFile1 = Environment.getExternalStorageDirectory();
      try
      {
        File localFile2 = new File(localFile1.getAbsolutePath() + "/Coomix");
        if (!localFile2.exists())
          localFile2.mkdirs();
        FileWriter localFileWriter = new FileWriter(new File(localFile2, "Error_Log.txt"), true);
        if (errorCount == 0)
        {
          String str = getTimeString();
          localFileWriter.append("\n" + str + "\n");
          Log.e("FDEBUG", str);
        }
        Log.e("FDEBUG", "error: " + paramString);
        localFileWriter.append(paramString + "\n\n");
        localFileWriter.flush();
        localFileWriter.close();
        errorCount = 1 + errorCount;
        return;
      }
      catch (IOException localIOException)
      {
        while (true)
          localIOException.printStackTrace();
      }
    }
    finally
    {
    }
  }

  public static void saveLog(String paramString)
  {
    try
    {
      File localFile1 = Environment.getExternalStorageDirectory();
      try
      {
        File localFile2 = new File(localFile1.getAbsolutePath() + "/Coomix");
        if (!localFile2.exists())
          localFile2.mkdirs();
        FileWriter localFileWriter = new FileWriter(new File(localFile2, "Request_Log.txt"), true);
        if (count == 0)
        {
          String str = getTimeString();
          localFileWriter.append("\n" + str + "\n");
          Log.e("FDEBUG", str);
        }
        localFileWriter.append(paramString + "\n");
        localFileWriter.flush();
        localFileWriter.close();
        count = 1 + count;
        return;
      }
      catch (IOException localIOException)
      {
        while (true)
          localIOException.printStackTrace();
      }
    }
    finally
    {
    }
  }

  // ERROR //
  public static void saveSerializableObject(java.io.Serializable paramSerializable, Context paramContext, String paramString)
  {
    // Byte code:
    //   0: ldc 2
    //   2: monitorenter
    //   3: aload_1
    //   4: aload_2
    //   5: iconst_0
    //   6: invokevirtual 635	android/content/Context:openFileOutput	(Ljava/lang/String;I)Ljava/io/FileOutputStream;
    //   9: astore 6
    //   11: new 637	java/io/ObjectOutputStream
    //   14: dup
    //   15: aload 6
    //   17: invokespecial 640	java/io/ObjectOutputStream:<init>	(Ljava/io/OutputStream;)V
    //   20: astore 7
    //   22: aload 7
    //   24: aload_0
    //   25: invokevirtual 644	java/io/ObjectOutputStream:writeObject	(Ljava/lang/Object;)V
    //   28: aload 7
    //   30: invokevirtual 645	java/io/ObjectOutputStream:close	()V
    //   33: aload 6
    //   35: invokevirtual 648	java/io/FileOutputStream:close	()V
    //   38: ldc 2
    //   40: monitorexit
    //   41: return
    //   42: astore 5
    //   44: aload 5
    //   46: invokevirtual 399	java/io/FileNotFoundException:printStackTrace	()V
    //   49: goto -11 -> 38
    //   52: astore 4
    //   54: ldc 2
    //   56: monitorexit
    //   57: aload 4
    //   59: athrow
    //   60: astore_3
    //   61: aload_3
    //   62: invokevirtual 400	java/io/IOException:printStackTrace	()V
    //   65: goto -27 -> 38
    //
    // Exception table:
    //   from	to	target	type
    //   3	38	42	java/io/FileNotFoundException
    //   3	38	52	finally
    //   44	49	52	finally
    //   61	65	52	finally
    //   3	38	60	java/io/IOException
  }
}

/* Location:           F:\ant\dex2jar-0.0.9.15\classes-dex2jar.jar
 * Qualified Name:     com.coomix.app.framework.util.CommonUtil
 * JD-Core Version:    0.6.2
 */
