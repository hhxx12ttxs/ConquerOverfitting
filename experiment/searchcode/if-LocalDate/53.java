package com.inmobi.androidsdk.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdRequest.EducationType;
import com.inmobi.androidsdk.IMAdRequest.EthnicityType;
import com.inmobi.androidsdk.IMAdRequest.GenderType;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class UserInfo
{
  private String A;
  private Context B;
  private String C;
  private String D = null;
  private String E = null;
  private String F = null;
  private String G = null;
  private String H = null;
  private IMAdRequest I;
  private String J;
  private Random K;
  private String L;
  private int M;
  boolean a;
  boolean b;
  private String c;
  private String d;
  private String e;
  private String f;
  private String g;
  private String h = "1";
  private String i;
  private String j;
  private String k;
  private String l;
  private String m;
  private String n;
  private String o;
  private String p;
  private String q;
  private String r;
  private String s;
  private LocationManager t;
  private double u;
  private double v;
  private double w;
  private boolean x;
  private long y = 0L;
  private boolean z;

  public UserInfo(Context paramContext)
  {
    this.B = paramContext;
    this.K = new Random();
  }

  private void a(Location paramLocation)
  {
    if (paramLocation != null)
    {
      setValidGeoInfo(true);
      a(paramLocation.getLatitude());
      b(paramLocation.getLongitude());
      c(paramLocation.getAccuracy());
      a(paramLocation.getTime());
    }
  }

  private String h()
  {
    return this.g;
  }

  private void i()
  {
    try
    {
      if (b() == null)
        a((LocationManager)a().getSystemService("location"));
      LocationManager localLocationManager;
      Criteria localCriteria;
      if (b() != null)
      {
        localLocationManager = b();
        localCriteria = new Criteria();
        if (a().checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") != 0)
          break label180;
        localCriteria.setAccuracy(1);
      }
      while (true)
      {
        localCriteria.setCostAllowed(false);
        String str = localLocationManager.getBestProvider(localCriteria, true);
        if ((!isValidGeoInfo()) && (str != null))
        {
          Location localLocation = localLocationManager.getLastKnownLocation(str);
          if (Constants.DEBUG)
            Log.d("InMobiAndroidSDK_3.5.2", "lastBestKnownLocation: " + localLocation);
          if (localLocation == null)
          {
            localLocation = j();
            if (Constants.DEBUG)
              Log.d("InMobiAndroidSDK_3.5.2", "lastKnownLocation: " + localLocation);
          }
          a(localLocation);
        }
        return;
        label180: if (a().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0)
          localCriteria.setAccuracy(2);
      }
    }
    catch (Exception localException)
    {
      while (true)
        if (Constants.DEBUG)
          Log.w("InMobiAndroidSDK_3.5.2", "Error getting the Location Info", localException);
    }
    finally
    {
    }
  }

  private Location j()
  {
    if (b() == null)
      a((LocationManager)a().getSystemService("location"));
    LocationManager localLocationManager;
    List localList;
    if (b() != null)
    {
      localLocationManager = b();
      localList = localLocationManager.getProviders(true);
    }
    for (int i1 = -1 + localList.size(); ; i1--)
    {
      Location localLocation;
      if (i1 < 0)
        localLocation = null;
      do
      {
        return localLocation;
        String str = (String)localList.get(i1);
        if (!localLocationManager.isProviderEnabled(str))
          break;
        localLocation = localLocationManager.getLastKnownLocation(str);
      }
      while (localLocation != null);
    }
  }

  private void k()
  {
    int i1 = a().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION");
    int i2 = a().checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION");
    if ((i1 != 0) && (i2 != 0))
    {
      b(true);
      return;
    }
    b(false);
  }

  // ERROR //
  private void l()
  {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual 226	com/inmobi/androidsdk/impl/UserInfo:getDeviceName	()Ljava/lang/String;
    //   4: ifnonnull +598 -> 602
    //   7: aload_0
    //   8: getstatic 231	android/os/Build:BRAND	Ljava/lang/String;
    //   11: invokevirtual 233	com/inmobi/androidsdk/impl/UserInfo:h	(Ljava/lang/String;)V
    //   14: aload_0
    //   15: getstatic 236	android/os/Build:MODEL	Ljava/lang/String;
    //   18: invokevirtual 238	com/inmobi/androidsdk/impl/UserInfo:i	(Ljava/lang/String;)V
    //   21: getstatic 241	android/os/Build:ID	Ljava/lang/String;
    //   24: invokevirtual 244	java/lang/String:trim	()Ljava/lang/String;
    //   27: invokestatic 250	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   30: ifeq +730 -> 760
    //   33: getstatic 241	android/os/Build:ID	Ljava/lang/String;
    //   36: astore 11
    //   38: aload_0
    //   39: aload 11
    //   41: invokevirtual 252	com/inmobi/androidsdk/impl/UserInfo:j	(Ljava/lang/String;)V
    //   44: getstatic 257	android/os/Build$VERSION:RELEASE	Ljava/lang/String;
    //   47: invokevirtual 244	java/lang/String:trim	()Ljava/lang/String;
    //   50: invokestatic 250	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   53: ifeq +715 -> 768
    //   56: getstatic 257	android/os/Build$VERSION:RELEASE	Ljava/lang/String;
    //   59: astore 12
    //   61: aload_0
    //   62: aload 12
    //   64: invokevirtual 259	com/inmobi/androidsdk/impl/UserInfo:k	(Ljava/lang/String;)V
    //   67: invokestatic 263	com/inmobi/androidsdk/impl/b:b	()J
    //   70: lstore 13
    //   72: invokestatic 265	com/inmobi/androidsdk/impl/b:c	()J
    //   75: lstore 15
    //   77: ldc_w 267
    //   80: astore 17
    //   82: lload 13
    //   84: lconst_0
    //   85: lcmp
    //   86: ifle +28 -> 114
    //   89: new 170	java/lang/StringBuilder
    //   92: dup
    //   93: aload 17
    //   95: invokestatic 271	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   98: invokespecial 175	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   101: lload 13
    //   103: invokestatic 274	com/inmobi/androidsdk/impl/b:a	(J)Ljava/lang/String;
    //   106: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   109: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   112: astore 17
    //   114: lload 15
    //   116: lconst_0
    //   117: lcmp
    //   118: ifle +34 -> 152
    //   121: new 170	java/lang/StringBuilder
    //   124: dup
    //   125: aload 17
    //   127: invokestatic 271	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   130: invokespecial 175	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   133: ldc_w 279
    //   136: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   139: lload 15
    //   141: invokestatic 274	com/inmobi/androidsdk/impl/b:a	(J)Ljava/lang/String;
    //   144: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   147: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   150: astore 17
    //   152: aload_0
    //   153: aload 17
    //   155: invokevirtual 281	com/inmobi/androidsdk/impl/UserInfo:m	(Ljava/lang/String;)V
    //   158: invokestatic 287	java/lang/System:getProperties	()Ljava/util/Properties;
    //   161: ldc_w 289
    //   164: invokevirtual 294	java/util/Properties:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   167: checkcast 217	java/lang/String
    //   170: astore 18
    //   172: invokestatic 287	java/lang/System:getProperties	()Ljava/util/Properties;
    //   175: ldc_w 296
    //   178: invokevirtual 294	java/util/Properties:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   181: checkcast 217	java/lang/String
    //   184: astore 19
    //   186: aload 18
    //   188: ifnull +44 -> 232
    //   191: aload 19
    //   193: ifnull +39 -> 232
    //   196: aload_0
    //   197: new 170	java/lang/StringBuilder
    //   200: dup
    //   201: aload 18
    //   203: invokestatic 271	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   206: invokespecial 175	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   209: ldc_w 298
    //   212: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   215: aload 19
    //   217: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   220: ldc_w 300
    //   223: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   226: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   229: invokevirtual 302	com/inmobi/androidsdk/impl/UserInfo:n	(Ljava/lang/String;)V
    //   232: invokestatic 307	android/os/SystemClock:elapsedRealtime	()J
    //   235: lstore 20
    //   237: invokestatic 313	java/util/Calendar:getInstance	()Ljava/util/Calendar;
    //   240: astore 22
    //   242: aload 22
    //   244: invokestatic 316	java/lang/System:currentTimeMillis	()J
    //   247: lload 20
    //   249: lsub
    //   250: invokevirtual 319	java/util/Calendar:setTimeInMillis	(J)V
    //   253: aload_0
    //   254: aload 22
    //   256: invokevirtual 322	java/util/Calendar:getTime	()Ljava/util/Date;
    //   259: invokevirtual 325	java/util/Date:toString	()Ljava/lang/String;
    //   262: invokevirtual 328	com/inmobi/androidsdk/impl/UserInfo:setDeviceBTHW	(Ljava/lang/String;)V
    //   265: invokestatic 334	java/util/Locale:getDefault	()Ljava/util/Locale;
    //   268: astore 23
    //   270: aload 23
    //   272: invokevirtual 337	java/util/Locale:getLanguage	()Ljava/lang/String;
    //   275: astore 24
    //   277: aload 24
    //   279: ifnull +497 -> 776
    //   282: aload 24
    //   284: invokevirtual 340	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   287: astore 27
    //   289: aload 23
    //   291: invokevirtual 343	java/util/Locale:getCountry	()Ljava/lang/String;
    //   294: astore 46
    //   296: aload 46
    //   298: ifnull +34 -> 332
    //   301: new 170	java/lang/StringBuilder
    //   304: dup
    //   305: aload 27
    //   307: invokestatic 271	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   310: invokespecial 175	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   313: ldc_w 345
    //   316: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   319: aload 46
    //   321: invokevirtual 340	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   324: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   327: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   330: astore 27
    //   332: aload_0
    //   333: aload 27
    //   335: invokevirtual 347	com/inmobi/androidsdk/impl/UserInfo:g	(Ljava/lang/String;)V
    //   338: new 170	java/lang/StringBuilder
    //   341: dup
    //   342: invokespecial 348	java/lang/StringBuilder:<init>	()V
    //   345: astore 28
    //   347: aload 28
    //   349: ldc_w 350
    //   352: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   355: pop
    //   356: aload 28
    //   358: ldc_w 352
    //   361: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   364: pop
    //   365: aload 28
    //   367: ldc_w 354
    //   370: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   373: pop
    //   374: aload 28
    //   376: aload_0
    //   377: invokevirtual 357	com/inmobi/androidsdk/impl/UserInfo:getDeviceModel	()Ljava/lang/String;
    //   380: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   383: pop
    //   384: aload 28
    //   386: ldc_w 359
    //   389: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   392: pop
    //   393: aload 28
    //   395: aload_0
    //   396: invokevirtual 362	com/inmobi/androidsdk/impl/UserInfo:getDeviceSystemName	()Ljava/lang/String;
    //   399: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   402: pop
    //   403: aload 28
    //   405: ldc_w 364
    //   408: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   411: pop
    //   412: aload 28
    //   414: aload_0
    //   415: invokevirtual 367	com/inmobi/androidsdk/impl/UserInfo:getDeviceSystemVersion	()Ljava/lang/String;
    //   418: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   421: pop
    //   422: aload 28
    //   424: ldc_w 369
    //   427: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   430: pop
    //   431: aload 28
    //   433: aload_0
    //   434: invokevirtual 372	com/inmobi/androidsdk/impl/UserInfo:getDeviceMachineHW	()Ljava/lang/String;
    //   437: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   440: pop
    //   441: aload 28
    //   443: ldc_w 300
    //   446: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   449: pop
    //   450: aload_0
    //   451: aload 28
    //   453: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   456: invokevirtual 374	com/inmobi/androidsdk/impl/UserInfo:l	(Ljava/lang/String;)V
    //   459: aload_0
    //   460: invokevirtual 120	com/inmobi/androidsdk/impl/UserInfo:a	()Landroid/content/Context;
    //   463: astore 41
    //   465: aload 41
    //   467: invokevirtual 378	android/content/Context:getPackageManager	()Landroid/content/pm/PackageManager;
    //   470: astore 42
    //   472: aload 42
    //   474: aload 41
    //   476: invokevirtual 381	android/content/Context:getPackageName	()Ljava/lang/String;
    //   479: sipush 128
    //   482: invokevirtual 387	android/content/pm/PackageManager:getApplicationInfo	(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;
    //   485: astore 43
    //   487: aload 43
    //   489: ifnull +28 -> 517
    //   492: aload_0
    //   493: aload 43
    //   495: getfield 392	android/content/pm/ApplicationInfo:packageName	Ljava/lang/String;
    //   498: invokevirtual 394	com/inmobi/androidsdk/impl/UserInfo:a	(Ljava/lang/String;)V
    //   501: aload_0
    //   502: aload 43
    //   504: aload 42
    //   506: invokevirtual 398	android/content/pm/ApplicationInfo:loadLabel	(Landroid/content/pm/PackageManager;)Ljava/lang/CharSequence;
    //   509: invokeinterface 401 1 0
    //   514: invokevirtual 403	com/inmobi/androidsdk/impl/UserInfo:b	(Ljava/lang/String;)V
    //   517: aload 42
    //   519: aload 41
    //   521: invokevirtual 381	android/content/Context:getPackageName	()Ljava/lang/String;
    //   524: sipush 128
    //   527: invokevirtual 407	android/content/pm/PackageManager:getPackageInfo	(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;
    //   530: astore 44
    //   532: aload 44
    //   534: ifnull +419 -> 953
    //   537: aload 44
    //   539: getfield 412	android/content/pm/PackageInfo:versionName	Ljava/lang/String;
    //   542: astore 45
    //   544: aload 45
    //   546: ifnull +14 -> 560
    //   549: aload 45
    //   551: ldc_w 414
    //   554: invokevirtual 418	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   557: ifeq +23 -> 580
    //   560: new 170	java/lang/StringBuilder
    //   563: dup
    //   564: aload 44
    //   566: getfield 421	android/content/pm/PackageInfo:versionCode	I
    //   569: invokestatic 424	java/lang/String:valueOf	(I)Ljava/lang/String;
    //   572: invokespecial 175	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   575: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   578: astore 45
    //   580: aload 45
    //   582: ifnull +20 -> 602
    //   585: aload 45
    //   587: ldc_w 414
    //   590: invokevirtual 418	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   593: ifne +9 -> 602
    //   596: aload_0
    //   597: aload 45
    //   599: invokevirtual 426	com/inmobi/androidsdk/impl/UserInfo:c	(Ljava/lang/String;)V
    //   602: aload_0
    //   603: aload_0
    //   604: invokespecial 428	com/inmobi/androidsdk/impl/UserInfo:m	()Ljava/lang/String;
    //   607: invokestatic 434	com/inmobi/androidsdk/ai/controller/util/Utils:getODIN1	(Ljava/lang/String;)Ljava/lang/String;
    //   610: invokevirtual 436	com/inmobi/androidsdk/impl/UserInfo:e	(Ljava/lang/String;)V
    //   613: aload_0
    //   614: aload_0
    //   615: getfield 77	com/inmobi/androidsdk/impl/UserInfo:K	Ljava/util/Random;
    //   618: invokevirtual 439	java/util/Random:nextInt	()I
    //   621: invokevirtual 441	com/inmobi/androidsdk/impl/UserInfo:a	(I)V
    //   624: aload_0
    //   625: aload_0
    //   626: getfield 443	com/inmobi/androidsdk/impl/UserInfo:I	Lcom/inmobi/androidsdk/IMAdRequest;
    //   629: aload_0
    //   630: invokespecial 445	com/inmobi/androidsdk/impl/UserInfo:h	()Ljava/lang/String;
    //   633: aload_0
    //   634: invokevirtual 448	com/inmobi/androidsdk/impl/UserInfo:getRandomKey	()Ljava/lang/String;
    //   637: iconst_0
    //   638: invokestatic 452	com/inmobi/androidsdk/ai/controller/util/Utils:getUIDMap	(Lcom/inmobi/androidsdk/IMAdRequest;Ljava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;
    //   641: invokevirtual 454	com/inmobi/androidsdk/impl/UserInfo:f	(Ljava/lang/String;)V
    //   644: aload_0
    //   645: getfield 72	com/inmobi/androidsdk/impl/UserInfo:B	Landroid/content/Context;
    //   648: ifnull +14 -> 662
    //   651: aload_0
    //   652: aload_0
    //   653: getfield 72	com/inmobi/androidsdk/impl/UserInfo:B	Landroid/content/Context;
    //   656: invokevirtual 457	android/content/Context:getApplicationContext	()Landroid/content/Context;
    //   659: invokevirtual 459	com/inmobi/androidsdk/impl/UserInfo:b	(Landroid/content/Context;)V
    //   662: aload_0
    //   663: getfield 72	com/inmobi/androidsdk/impl/UserInfo:B	Landroid/content/Context;
    //   666: ldc_w 461
    //   669: invokevirtual 142	android/content/Context:checkCallingOrSelfPermission	(Ljava/lang/String;)I
    //   672: ifne +61 -> 733
    //   675: aload_0
    //   676: invokevirtual 120	com/inmobi/androidsdk/impl/UserInfo:a	()Landroid/content/Context;
    //   679: ldc_w 463
    //   682: invokevirtual 128	android/content/Context:getSystemService	(Ljava/lang/String;)Ljava/lang/Object;
    //   685: checkcast 465	android/net/ConnectivityManager
    //   688: astore 6
    //   690: aload 6
    //   692: ifnull +41 -> 733
    //   695: aload 6
    //   697: invokevirtual 469	android/net/ConnectivityManager:getActiveNetworkInfo	()Landroid/net/NetworkInfo;
    //   700: astore 7
    //   702: aload 7
    //   704: invokevirtual 474	android/net/NetworkInfo:getType	()I
    //   707: istore 8
    //   709: aload 7
    //   711: invokevirtual 477	android/net/NetworkInfo:getSubtype	()I
    //   714: istore 9
    //   716: iload 8
    //   718: iconst_1
    //   719: if_icmpne +136 -> 855
    //   722: ldc_w 479
    //   725: astore 10
    //   727: aload_0
    //   728: aload 10
    //   730: invokevirtual 481	com/inmobi/androidsdk/impl/UserInfo:d	(Ljava/lang/String;)V
    //   733: aload_0
    //   734: invokevirtual 120	com/inmobi/androidsdk/impl/UserInfo:a	()Landroid/content/Context;
    //   737: invokevirtual 485	android/content/Context:getResources	()Landroid/content/res/Resources;
    //   740: invokevirtual 491	android/content/res/Resources:getConfiguration	()Landroid/content/res/Configuration;
    //   743: getfield 496	android/content/res/Configuration:orientation	I
    //   746: istore 5
    //   748: iload 5
    //   750: iconst_2
    //   751: if_icmpne +161 -> 912
    //   754: aload_0
    //   755: iconst_3
    //   756: invokevirtual 499	com/inmobi/androidsdk/impl/UserInfo:setOrientation	(I)V
    //   759: return
    //   760: ldc_w 501
    //   763: astore 11
    //   765: goto -727 -> 38
    //   768: ldc_w 503
    //   771: astore 12
    //   773: goto -712 -> 61
    //   776: invokestatic 287	java/lang/System:getProperties	()Ljava/util/Properties;
    //   779: ldc_w 505
    //   782: invokevirtual 294	java/util/Properties:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   785: checkcast 217	java/lang/String
    //   788: astore 25
    //   790: invokestatic 287	java/lang/System:getProperties	()Ljava/util/Properties;
    //   793: ldc_w 507
    //   796: invokevirtual 294	java/util/Properties:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   799: checkcast 217	java/lang/String
    //   802: astore 26
    //   804: aload 25
    //   806: ifnull +153 -> 959
    //   809: aload 26
    //   811: ifnull +148 -> 959
    //   814: new 170	java/lang/StringBuilder
    //   817: dup
    //   818: aload 25
    //   820: invokestatic 271	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
    //   823: invokespecial 175	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   826: ldc_w 345
    //   829: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   832: aload 26
    //   834: invokevirtual 277	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   837: invokevirtual 182	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   840: astore 27
    //   842: aload 27
    //   844: ifnonnull -512 -> 332
    //   847: ldc_w 509
    //   850: astore 27
    //   852: goto -520 -> 332
    //   855: iload 8
    //   857: ifne +90 -> 947
    //   860: ldc_w 511
    //   863: astore 10
    //   865: iload 9
    //   867: iconst_1
    //   868: if_icmpne +98 -> 966
    //   871: ldc_w 513
    //   874: astore 10
    //   876: goto -149 -> 727
    //   879: iload 9
    //   881: ifne -154 -> 727
    //   884: ldc_w 511
    //   887: astore 10
    //   889: goto -162 -> 727
    //   892: astore_1
    //   893: getstatic 166	com/inmobi/androidsdk/impl/Constants:DEBUG	Z
    //   896: ifeq -163 -> 733
    //   899: ldc 168
    //   901: ldc_w 515
    //   904: aload_1
    //   905: invokestatic 201	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   908: pop
    //   909: goto -176 -> 733
    //   912: iload 5
    //   914: iconst_1
    //   915: if_icmpne -156 -> 759
    //   918: aload_0
    //   919: iconst_1
    //   920: invokevirtual 499	com/inmobi/androidsdk/impl/UserInfo:setOrientation	(I)V
    //   923: return
    //   924: astore_3
    //   925: getstatic 166	com/inmobi/androidsdk/impl/Constants:DEBUG	Z
    //   928: ifeq -169 -> 759
    //   931: ldc 168
    //   933: ldc_w 517
    //   936: aload_3
    //   937: invokestatic 201	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   940: pop
    //   941: return
    //   942: astore 40
    //   944: goto -342 -> 602
    //   947: aconst_null
    //   948: astore 10
    //   950: goto -223 -> 727
    //   953: aconst_null
    //   954: astore 45
    //   956: goto -376 -> 580
    //   959: aload 24
    //   961: astore 27
    //   963: goto -121 -> 842
    //   966: iload 9
    //   968: iconst_2
    //   969: if_icmpne +11 -> 980
    //   972: ldc_w 519
    //   975: astore 10
    //   977: goto -250 -> 727
    //   980: iload 9
    //   982: iconst_3
    //   983: if_icmpne -104 -> 879
    //   986: ldc_w 521
    //   989: astore 10
    //   991: goto -264 -> 727
    //
    // Exception table:
    //   from	to	target	type
    //   662	690	892	java/lang/Exception
    //   695	716	892	java/lang/Exception
    //   727	733	892	java/lang/Exception
    //   733	748	924	java/lang/Exception
    //   754	759	924	java/lang/Exception
    //   918	923	924	java/lang/Exception
    //   459	487	942	java/lang/Exception
    //   492	517	942	java/lang/Exception
    //   517	532	942	java/lang/Exception
    //   537	544	942	java/lang/Exception
    //   549	560	942	java/lang/Exception
    //   560	580	942	java/lang/Exception
    //   585	602	942	java/lang/Exception
  }

  private String m()
  {
    if (this.J == null);
    try
    {
      this.J = Settings.Secure.getString(a().getContentResolver(), "android_id");
      label24: if (this.J == null);
      try
      {
        this.J = Settings.System.getString(a().getContentResolver(), "android_id");
        label48: return this.J;
      }
      catch (Exception localException2)
      {
        break label48;
      }
    }
    catch (Exception localException1)
    {
      break label24;
    }
  }

  final Context a()
  {
    return this.B;
  }

  final void a(double paramDouble)
  {
    this.u = paramDouble;
  }

  final void a(int paramInt)
  {
    this.i = Integer.toString(paramInt);
  }

  final void a(long paramLong)
  {
    this.y = paramLong;
  }

  final void a(Context paramContext)
  {
    this.B = paramContext;
  }

  final void a(LocationManager paramLocationManager)
  {
    try
    {
      this.t = paramLocationManager;
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  final void a(String paramString)
  {
    this.c = paramString;
  }

  final void a(boolean paramBoolean)
  {
    this.a = paramBoolean;
  }

  final LocationManager b()
  {
    try
    {
      LocationManager localLocationManager = this.t;
      return localLocationManager;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  final void b(double paramDouble)
  {
    this.v = paramDouble;
  }

  final void b(Context paramContext)
  {
    try
    {
      if (this.L == null)
        this.L = paramContext.getSharedPreferences("inmobisdkaid", 0).getString("A_ID", null);
      if (this.L == null)
      {
        this.L = UUID.randomUUID().toString();
        SharedPreferences.Editor localEditor = paramContext.getSharedPreferences("inmobisdkaid", 0).edit();
        localEditor.putString("A_ID", this.L);
        localEditor.commit();
      }
      return;
    }
    catch (Exception localException)
    {
    }
  }

  final void b(String paramString)
  {
    this.d = paramString;
  }

  final void b(boolean paramBoolean)
  {
    this.b = paramBoolean;
  }

  final void c(double paramDouble)
  {
    this.w = paramDouble;
  }

  final void c(String paramString)
  {
    this.e = paramString;
  }

  final void c(boolean paramBoolean)
  {
    this.z = paramBoolean;
  }

  final boolean c()
  {
    if (this.I != null)
      return this.I.isLocationInquiryAllowed();
    return true;
  }

  final void d(String paramString)
  {
    this.f = paramString;
  }

  final boolean d()
  {
    return this.a;
  }

  final void e(String paramString)
  {
    this.g = paramString;
  }

  final boolean e()
  {
    return this.b;
  }

  final long f()
  {
    return this.y;
  }

  final void f(String paramString)
  {
    this.j = paramString;
  }

  final void g(String paramString)
  {
    this.k = paramString;
  }

  final boolean g()
  {
    return this.z;
  }

  public final String getAdUnitSlot()
  {
    return this.F;
  }

  public final int getAge()
  {
    if (this.I != null)
      return this.I.getAge();
    return 0;
  }

  public final String getAid()
  {
    return this.L;
  }

  public final String getAppBId()
  {
    return this.c;
  }

  public final String getAppDisplayName()
  {
    return this.d;
  }

  public final String getAppVer()
  {
    return this.e;
  }

  public final String getAreaCode()
  {
    if (this.I != null)
      return this.I.getAreaCode();
    return null;
  }

  public final String getDateOfBirth()
  {
    IMAdRequest localIMAdRequest = this.I;
    String str = null;
    if (localIMAdRequest != null)
    {
      Date localDate = this.I.getDateOfBirth();
      str = null;
      if (localDate != null)
      {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.setTimeInMillis(this.I.getDateOfBirth().getTime());
        str = localCalendar.get(1) + "-" + (1 + localCalendar.get(2)) + "-" + localCalendar.get(5);
      }
    }
    return str;
  }

  public final String getDeviceBTHW()
  {
    return this.r;
  }

  public final String getDeviceMachineHW()
  {
    return this.q;
  }

  public final String getDeviceModel()
  {
    return this.m;
  }

  public final String getDeviceName()
  {
    return this.l;
  }

  public final String getDeviceStorageSize()
  {
    return this.p;
  }

  public final String getDeviceSystemName()
  {
    return this.n;
  }

  public final String getDeviceSystemVersion()
  {
    return this.o;
  }

  public final IMAdRequest.EducationType getEducation()
  {
    if (this.I != null)
      return this.I.getEducation();
    return null;
  }

  public final IMAdRequest.EthnicityType getEthnicity()
  {
    if (this.I != null)
      return this.I.getEthnicity();
    return null;
  }

  public final IMAdRequest.GenderType getGender()
  {
    if (this.I != null)
      return this.I.getGender();
    return null;
  }

  public final int getIncome()
  {
    if (this.I != null)
      return this.I.getIncome();
    return 0;
  }

  public final String getInterests()
  {
    if (this.I != null)
      return this.I.getInterests();
    return null;
  }

  public final String getKeywords()
  {
    if (this.I != null)
      return this.I.getKeywords();
    return null;
  }

  public final double getLat()
  {
    return this.u;
  }

  public final double getLocAccuracy()
  {
    return this.w;
  }

  public final String getLocalization()
  {
    return this.k;
  }

  public final String getLocationWithCityStateCountry()
  {
    if (this.I != null)
      return this.I.getLocationWithCityStateCountry();
    return null;
  }

  public final double getLon()
  {
    return this.v;
  }

  public final String getNetworkType()
  {
    return this.f;
  }

  public final int getOrientation()
  {
    return this.M;
  }

  public final String getPhoneDefaultUserAgent()
  {
    if (this.C == null)
      return "";
    return this.C;
  }

  public final String getPostalCode()
  {
    if (this.I != null)
      return this.I.getPostalCode();
    return null;
  }

  public final String getRandomKey()
  {
    return this.i;
  }

  public final String getRefTagKey()
  {
    return this.D;
  }

  public final String getRefTagValue()
  {
    return this.E;
  }

  public final Map<String, String> getRequestParams()
  {
    if (this.I != null)
      return this.I.getRequestParams();
    return null;
  }

  public final String getRsakeyVersion()
  {
    return this.h;
  }

  public final String getScreenDensity()
  {
    return this.H;
  }

  public final String getScreenSize()
  {
    return this.G;
  }

  public final String getSearchString()
  {
    if (this.I != null)
      return this.I.getSearchString();
    return null;
  }

  public final String getSiteId()
  {
    return this.s;
  }

  public final String getTestModeAdActionType()
  {
    return this.A;
  }

  public final String getUIDMapEncrypted()
  {
    return this.j;
  }

  public final String getUserAgent()
  {
    return "inmobi_androidsdk=3.5.2";
  }

  final void h(String paramString)
  {
    this.l = paramString;
  }

  final void i(String paramString)
  {
    this.m = paramString;
  }

  public final boolean isTestMode()
  {
    if (this.I != null)
      return this.I.isTestMode();
    return false;
  }

  public final boolean isValidGeoInfo()
  {
    return this.x;
  }

  final void j(String paramString)
  {
    this.n = paramString;
  }

  final void k(String paramString)
  {
    this.o = paramString;
  }

  final void l(String paramString)
  {
  }

  final void m(String paramString)
  {
    this.p = paramString;
  }

  final void n(String paramString)
  {
    this.q = paramString;
  }

  final void o(String paramString)
  {
    this.s = paramString;
  }

  final void p(String paramString)
  {
    this.A = paramString;
  }

  public final void setAdUnitSlot(String paramString)
  {
    this.F = paramString;
  }

  public final void setDeviceBTHW(String paramString)
  {
    this.r = paramString;
  }

  public final void setIMAdRequest(IMAdRequest paramIMAdRequest)
  {
    this.I = paramIMAdRequest;
  }

  public final void setOrientation(int paramInt)
  {
    this.M = paramInt;
  }

  public final void setPhoneDefaultUserAgent(String paramString)
  {
    this.C = paramString;
  }

  public final void setRefTagKey(String paramString)
  {
    this.D = paramString;
  }

  public final void setRefTagValue(String paramString)
  {
    this.E = paramString;
  }

  public final void setScreenDensity(String paramString)
  {
    this.H = paramString;
  }

  public final void setScreenSize(String paramString)
  {
    this.G = paramString;
  }

  public final void setValidGeoInfo(boolean paramBoolean)
  {
    this.x = paramBoolean;
  }

  public final void updateInfo(String paramString, IMAdRequest paramIMAdRequest)
  {
    while (true)
    {
      try
      {
        setIMAdRequest(paramIMAdRequest);
        l();
        o(paramString);
        if (paramIMAdRequest != null)
        {
          setValidGeoInfo(false);
          if (!c())
            break label78;
          if (paramIMAdRequest.getCurrentLocation() != null)
          {
            a(paramIMAdRequest.getCurrentLocation());
            setValidGeoInfo(true);
          }
        }
        else
        {
          return;
        }
        k();
        if (e())
          continue;
        i();
        continue;
      }
      finally
      {
      }
      label78: b(true);
    }
  }
}

/* Location:           D:\code\Research\Android\apks\gbis.gbandroid-49\gbis.gbandroid-49_dex2jar.jar
 * Qualified Name:     com.inmobi.androidsdk.impl.UserInfo
 * JD-Core Version:    0.6.2
 */
