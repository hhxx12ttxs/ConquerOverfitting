package com.millennialmedia.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class AdDatabaseHelper extends SQLiteOpenHelper
{
  private static final String ACID = "acid";
  private static final String ACTIVITY = "activity";
  private static final String ADS_TABLE_NAME = "ads";
  private static final String AD_ID = "adid";
  private static final String ANDROID_ANCHOR = "anchor";
  private static final String ANDROID_ANCHOR_2 = "anchor2";
  private static final String ANDROID_POSITION = "position";
  private static final String ANDROID_POSITION_2 = "position2";
  private static final String APPEARANCE_DELAY = "appearancedelay";
  private static final String BUTTONS_TABLE_NAME = "buttons";
  private static final String CACHE_COMPLETE = "cachecomplete";
  private static final String CACHE_FAILED = "cachefailed";
  private static final String CONTENT_LENGTH = "contentlength";
  private static final String CONTENT_URL = "contenturl";
  private static final String DB_NAME = "millennialmedia.db";
  private static final int DB_VERSION = 36;
  private static final String DEFERRED_VIEW_START = "deferredviewstart";
  private static final String DURATION = "duration";
  private static final String END_ACTIVITY = "endactivity";
  private static final String END_OPACITY = "endopacity";
  private static final String EXPIRATION = "expiration";
  private static final String FADE_DURATION = "fadeduration";
  private static final String IMAGE_URL = "imageurl";
  private static final String INACTIVITY_TIMEOUT = "inactivitytimeout";
  private static final String LINK_URL = "linkurl";
  private static final String LOG = "log";
  private static final String NAME = "name";
  private static final String ON_COMPLETION = "oncompletion";
  private static final String OVERLAY_ORIENTATION = "overlayorientation";
  private static final String PADDING_BOTTOM = "paddingbottom";
  private static final String PADDING_LEFT = "paddingleft";
  private static final String PADDING_RIGHT = "paddingright";
  private static final String PADDING_TOP = "paddingtop";
  private static final String SD_CARD = "sdcard";
  private static final String SHOW_CONTROLS = "showcontrols";
  private static final String SHOW_COUNTDOWN = "showcountdown";
  private static final String START_ACTIVITY = "startactivity";
  private static final String START_OPACITY = "startopacity";
  private static final String STAY_IN_PLAYER = "stayInPlayer";
  private static final String TYPE = "type";
  private static final String VIDEO_ERROR = "videoError";
  private static final String _ID = "id";
  private SQLiteDatabase db = getReadableDatabase();

  AdDatabaseHelper(Context paramContext)
  {
    super(paramContext, "millennialmedia.db", null, 36);
  }

  private <T> T[] getArray(Cursor paramCursor, int paramInt, T[] paramArrayOfT)
  {
    try
    {
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramCursor.getBlob(paramInt));
      ObjectInputStream localObjectInputStream = new ObjectInputStream(localByteArrayInputStream);
      ArrayList localArrayList = (ArrayList)localObjectInputStream.readObject();
      localObjectInputStream.close();
      localByteArrayInputStream.close();
      if (localArrayList != null)
      {
        Object[] arrayOfObject = (Object[])localArrayList.toArray(paramArrayOfT);
        return arrayOfObject;
      }
    }
    catch (StreamCorruptedException localStreamCorruptedException)
    {
      localStreamCorruptedException.printStackTrace();
      return null;
    }
    catch (IOException localIOException)
    {
      while (true)
        localIOException.printStackTrace();
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      while (true)
        localClassNotFoundException.printStackTrace();
    }
  }

  private <T> void putArray(ContentValues paramContentValues, String paramString, T[] paramArrayOfT)
  {
    if (paramArrayOfT == null)
      return;
    try
    {
      ArrayList localArrayList = new ArrayList();
      for (int i = 0; i < paramArrayOfT.length; i++)
        localArrayList.add(paramArrayOfT[i]);
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(localByteArrayOutputStream);
      localObjectOutputStream.writeObject(localArrayList);
      byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
      localObjectOutputStream.close();
      localByteArrayOutputStream.close();
      paramContentValues.put(paramString, arrayOfByte);
      return;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  boolean checkIfAdExists(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT * FROM ads WHERE ads.name='" + paramString + "'", null);
    int i = localCursor.getCount();
    localCursor.close();
    return i > 0;
  }

  public void close()
  {
    try
    {
      if (this.db != null)
        this.db.close();
      super.close();
      return;
    }
    finally
    {
    }
  }

  List<String> getAllExpiredAds()
  {
    Cursor localCursor = this.db.rawQuery("SELECT ads.expiration,ads.name FROM ads", null);
    int i = localCursor.getCount();
    ArrayList localArrayList;
    if (i > 0)
    {
      localArrayList = new ArrayList();
      localCursor.moveToFirst();
      int j = 0;
      if (j < i)
      {
        String str1 = localCursor.getString(0);
        String str2 = localCursor.getString(1);
        if (str1 != null);
        while (true)
        {
          try
          {
            Date localDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(str1);
            if ((localDate != null) && (localDate.getTime() <= System.currentTimeMillis()))
            {
              MMAdViewSDK.Log.v(str2 + " is expired");
              if (str2 != null)
                localArrayList.add(str2);
            }
            if (!localCursor.isLast())
              localCursor.moveToNext();
            j++;
          }
          catch (ParseException localParseException)
          {
            localParseException.printStackTrace();
            continue;
          }
          if (str2 != null)
            localArrayList.add(str2);
        }
      }
    }
    else
    {
      localCursor.close();
      return null;
    }
    localCursor.close();
    return localArrayList;
  }

  ArrayList<VideoAd> getAllVideoAds()
  {
    ArrayList localArrayList = new ArrayList();
    Cursor localCursor = this.db.query("ads", new String[] { "name" }, null, null, null, null, null);
    localCursor.moveToFirst();
    while (!localCursor.isAfterLast())
    {
      localArrayList.add(getVideoAd(localCursor.getString(0)));
      localCursor.moveToNext();
    }
    return localArrayList;
  }

  int getButtonCountForAd(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT COUNT(*)  FROM ads,buttons WHERE ads.name='" + paramString + "' AND buttons." + "adid=ads." + "id", null);
    int i = localCursor.getCount();
    localCursor.close();
    return i;
  }

  List<VideoImage> getButtonsForAd(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT DISTINCT buttons.imageurl,buttons.contentlength,buttons.linkurl,buttons.overlayorientation,buttons.activity,buttons.position,buttons.anchor,buttons.position2,buttons.anchor2,buttons.paddingleft,buttons.paddingtop,buttons.paddingright,buttons.paddingbottom,buttons.appearancedelay,buttons.inactivitytimeout,buttons.startopacity,buttons.endopacity,buttons.fadeduration,buttons.id FROM ads,buttons WHERE ads.name='" + paramString + "' AND buttons." + "adid=ads." + "id ORDER BY buttons." + "id", null);
    int i = localCursor.getCount();
    localCursor.moveToFirst();
    Object localObject = null;
    if (i > 0)
    {
      ArrayList localArrayList = new ArrayList(i);
      for (int j = 0; j < i; j++)
      {
        VideoImage localVideoImage = new VideoImage();
        localVideoImage.imageUrl = localCursor.getString(0);
        localVideoImage.contentLength = localCursor.getLong(1);
        localVideoImage.linkUrl = localCursor.getString(2);
        localVideoImage.overlayOrientation = localCursor.getString(3);
        localVideoImage.activity = ((String[])getArray(localCursor, 4, new String[0]));
        if (localVideoImage.activity == null)
          localVideoImage.activity = new String[0];
        localVideoImage.position = localCursor.getInt(5);
        localVideoImage.anchor = localCursor.getInt(6);
        localVideoImage.position2 = localCursor.getInt(7);
        localVideoImage.anchor2 = localCursor.getInt(8);
        localVideoImage.paddingLeft = localCursor.getInt(9);
        localVideoImage.paddingTop = localCursor.getInt(10);
        localVideoImage.paddingRight = localCursor.getInt(11);
        localVideoImage.paddingBottom = localCursor.getInt(12);
        localVideoImage.appearanceDelay = localCursor.getLong(13);
        localVideoImage.inactivityTimeout = localCursor.getLong(14);
        localVideoImage.fromAlpha = localCursor.getFloat(15);
        localVideoImage.toAlpha = localCursor.getFloat(16);
        localVideoImage.fadeDuration = localCursor.getLong(17);
        localArrayList.add(localVideoImage);
        if (!localCursor.isLast())
          localCursor.moveToNext();
      }
      localObject = localArrayList;
    }
    localCursor.close();
    return localObject;
  }

  String getCachedAdAcid(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT acid FROM ads WHERE ads.name='" + paramString + "'", null);
    int i = localCursor.getCount();
    String str = null;
    if (i > 0)
    {
      localCursor.moveToFirst();
      str = localCursor.getString(0);
    }
    localCursor.close();
    return str;
  }

  String getCachedAdId(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT name FROM ads WHERE ads.acid='" + paramString + "'", null);
    int i = localCursor.getCount();
    String str = null;
    if (i > 0)
    {
      localCursor.moveToFirst();
      str = localCursor.getString(0);
    }
    localCursor.close();
    return str;
  }

  long getDeferredViewStart(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT ads.deferredviewstart FROM ads WHERE ads.name='" + paramString + "'", null);
    int i = localCursor.getCount();
    long l = System.currentTimeMillis();
    if (i > 0)
    {
      localCursor.moveToFirst();
      l = localCursor.getLong(0);
    }
    localCursor.close();
    return l;
  }

  int getIdForAdName(String paramString)
  {
    Cursor localCursor = this.db.query("ads", new String[] { "id" }, "ads.name= ?", new String[] { paramString }, null, null, null);
    if (localCursor.getCount() > 0)
      localCursor.moveToFirst();
    for (int i = localCursor.getInt(0); ; i = 0)
    {
      localCursor.close();
      return i;
    }
  }

  VideoAd getVideoAd(String paramString)
  {
    Cursor localCursor1 = this.db.rawQuery("SELECT DISTINCT ads.name,ads.acid,ads.contenturl,ads.expiration,ads.deferredviewstart,ads.oncompletion,ads.showcontrols,ads.startactivity,ads.endactivity,ads.duration,ads.contentlength,ads.stayInPlayer,ads.log,ads.id,ads.sdcard,ads.showcountdown,ads.cachecomplete,ads.cachefailed,ads.videoError FROM ads WHERE ads.name='" + paramString + "'", null);
    int i = localCursor1.getCount();
    label378: VideoAd localVideoAd2;
    if (i > 0)
    {
      VideoAd localVideoAd1 = new VideoAd();
      localCursor1.moveToFirst();
      localVideoAd1.id = localCursor1.getString(0);
      localVideoAd1.acid = localCursor1.getString(1);
      localVideoAd1.contentUrl = localCursor1.getString(2);
      String str = localCursor1.getString(3);
      try
      {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        if (str != null)
          localVideoAd1.expiration = localSimpleDateFormat.parse(str);
        localVideoAd1.deferredViewStart = localCursor1.getLong(4);
        localVideoAd1.onCompletionUrl = localCursor1.getString(5);
        if (localCursor1.getInt(6) == 1)
        {
          bool1 = true;
          localVideoAd1.showControls = bool1;
          localVideoAd1.startActivity = ((String[])getArray(localCursor1, 7, new String[0]));
          if (localVideoAd1.startActivity == null)
            localVideoAd1.startActivity = new String[0];
          localVideoAd1.endActivity = ((String[])getArray(localCursor1, 8, new String[0]));
          if (localVideoAd1.endActivity == null)
            localVideoAd1.endActivity = new String[0];
          localVideoAd1.duration = localCursor1.getLong(9);
          localVideoAd1.contentLength = localCursor1.getLong(10);
          if (localCursor1.getInt(11) != 1)
            break label378;
          bool2 = true;
          localVideoAd1.stayInPlayer = bool2;
          VideoLogEvent[] arrayOfVideoLogEvent = (VideoLogEvent[])getArray(localCursor1, 12, new VideoLogEvent[0]);
          localVideoAd1.activities = new ArrayList();
          if (arrayOfVideoLogEvent == null)
            break label384;
          for (int n = 0; n < arrayOfVideoLogEvent.length; n++)
            localVideoAd1.activities.add(arrayOfVideoLogEvent[n]);
        }
      }
      catch (ParseException localParseException)
      {
        while (true)
        {
          localParseException.printStackTrace();
          continue;
          boolean bool1 = false;
          continue;
          boolean bool2 = false;
        }
        label384: int j = localCursor1.getInt(13);
        boolean bool3;
        if (localCursor1.getInt(14) == 1)
        {
          bool3 = true;
          localVideoAd1.storedOnSdCard = bool3;
          if (localCursor1.getInt(15) != 1)
            break label944;
        }
        Cursor localCursor2;
        label944: for (boolean bool4 = true; ; bool4 = false)
        {
          localVideoAd1.showCountdown = bool4;
          localVideoAd1.cacheComplete = ((String[])getArray(localCursor1, 16, new String[0]));
          if (localVideoAd1.cacheComplete == null)
            localVideoAd1.cacheComplete = new String[0];
          localVideoAd1.cacheFailed = ((String[])getArray(localCursor1, 17, new String[0]));
          if (localVideoAd1.cacheFailed == null)
            localVideoAd1.cacheFailed = new String[0];
          localVideoAd1.videoError = ((String[])getArray(localCursor1, 18, new String[0]));
          if (localVideoAd1.videoError == null)
            localVideoAd1.videoError = new String[0];
          localCursor2 = this.db.rawQuery("SELECT DISTINCT buttons.imageurl,buttons.contentlength,buttons.linkurl,buttons.overlayorientation,buttons.activity,buttons.position,buttons.anchor,buttons.position2,buttons.anchor2,buttons.paddingleft,buttons.paddingtop,buttons.paddingright,buttons.paddingbottom,buttons.appearancedelay,buttons.inactivitytimeout,buttons.startopacity,buttons.endopacity,buttons.fadeduration,buttons.id FROM ads,buttons WHERE buttons.adid=" + j + " ORDER BY buttons.id", null);
          int k = localCursor2.getCount();
          if (k <= 0)
            break label950;
          localCursor2.moveToFirst();
          localVideoAd1.buttons = new ArrayList(i);
          for (int m = 0; m < k; m++)
          {
            VideoImage localVideoImage = new VideoImage();
            localVideoImage.imageUrl = localCursor2.getString(0);
            localVideoImage.contentLength = localCursor2.getLong(1);
            localVideoImage.linkUrl = localCursor2.getString(2);
            localVideoImage.overlayOrientation = localCursor2.getString(3);
            localVideoImage.activity = ((String[])getArray(localCursor2, 4, new String[0]));
            if (localVideoImage.activity == null)
              localVideoImage.activity = new String[0];
            localVideoImage.position = localCursor2.getInt(5);
            localVideoImage.anchor = localCursor2.getInt(6);
            localVideoImage.position2 = localCursor2.getInt(7);
            localVideoImage.anchor2 = localCursor2.getInt(8);
            localVideoImage.paddingLeft = localCursor2.getInt(9);
            localVideoImage.paddingTop = localCursor2.getInt(10);
            localVideoImage.paddingRight = localCursor2.getInt(11);
            localVideoImage.paddingBottom = localCursor2.getInt(12);
            localVideoImage.appearanceDelay = localCursor2.getLong(13);
            localVideoImage.inactivityTimeout = localCursor2.getLong(14);
            localVideoImage.fromAlpha = localCursor2.getFloat(15);
            localVideoImage.toAlpha = localCursor2.getFloat(16);
            localVideoImage.fadeDuration = localCursor2.getLong(17);
            localVideoAd1.buttons.add(localVideoImage);
            if (!localCursor2.isLast())
              localCursor2.moveToNext();
          }
          bool3 = false;
          break;
        }
        label950: localCursor2.close();
        localVideoAd2 = localVideoAd1;
      }
    }
    while (true)
    {
      localCursor1.close();
      return localVideoAd2;
      localVideoAd2 = null;
    }
  }

  boolean isAdExpired(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT ads.expiration FROM ads WHERE ads.name='" + paramString + "'", null);
    if (localCursor.getCount() > 0)
    {
      localCursor.moveToFirst();
      String str = localCursor.getString(0);
      Object localObject = null;
      if (str != null);
      try
      {
        Date localDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(str);
        localObject = localDate;
        localCursor.close();
        if (localObject == null)
          break label133;
        if (localObject.getTime() > System.currentTimeMillis())
          break label131;
        return true;
      }
      catch (ParseException localParseException)
      {
        while (true)
        {
          localParseException.printStackTrace();
          localObject = null;
        }
      }
    }
    else
    {
      localCursor.close();
      return false;
    }
    label131: return false;
    label133: return false;
  }

  boolean isAdOnSDCard(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT sdcard FROM ads WHERE ads.name='" + paramString + "'", null);
    if (localCursor.getCount() > 0)
      localCursor.moveToFirst();
    for (int i = localCursor.getInt(0); ; i = 0)
    {
      localCursor.close();
      boolean bool = false;
      if (i == 1)
        bool = true;
      return bool;
    }
  }

  public void onCreate(SQLiteDatabase paramSQLiteDatabase)
  {
    MMAdViewSDK.Log.d("Creating cached ad database");
    paramSQLiteDatabase.execSQL("CREATE TABLE ads (id INTEGER NOT NULL PRIMARY KEY,name TEXT,acid TEXT,type INTEGER,startactivity BLOB,endactivity BLOB,showcontrols INTEGER,contenturl TEXT,expiration TEXT,deferredviewstart BIGINT,oncompletion TEXT,duration BIGINT,contentlength BIGINT,stayInPlayer INTEGER,log BLOB,sdcard INTEGER,showcountdown INTEGER,cachecomplete BLOB,cachefailed BLOB,videoError BLOB);");
    paramSQLiteDatabase.execSQL("CREATE TABLE buttons (id INTEGER NOT NULL PRIMARY KEY,imageurl TEXT,contentlength BIGINT,linkurl TEXT,overlayorientation TEXT,activity BLOB,position INTEGER,anchor INTEGER,position2 INTEGER,anchor2 INTEGER,paddingtop INTEGER,paddingleft INTEGER,paddingbottom INTEGER,paddingright INTEGER,appearancedelay BIGINT,inactivitytimeout BIGINT,startopacity FLOAT,endopacity FLOAT,fadeduration BIGINT,adid INTEGER CONSTRAINT fk_buttons_ads_id REFERENCES ads(id) ON DELETE CASCADE);");
    paramSQLiteDatabase.execSQL("CREATE TRIGGER fk_buttons_ads_id BEFORE DELETE ON ads FOR EACH ROW BEGIN DELETE from buttons WHERE buttons.adid=OLD.id; END;");
  }

  public void onOpen(SQLiteDatabase paramSQLiteDatabase)
  {
  }

  public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
  {
    MMAdViewSDK.Log.v("Upgrading database from version " + paramInt1 + " to " + paramInt2 + ", which will destroy all old data");
    paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS ads");
    paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS buttons");
    onCreate(paramSQLiteDatabase);
  }

  boolean purgeAdFromDb(String paramString)
  {
    return this.db.delete("ads", "ads.name=?", new String[] { paramString }) > 0;
  }

  boolean shouldShowBottomBar(String paramString)
  {
    Cursor localCursor = this.db.rawQuery("SELECT DISTINCT showcontrols FROM ads WHERE ads.name='" + paramString + "'", null);
    if (localCursor.getCount() > 0)
      localCursor.moveToFirst();
    for (int i = localCursor.getInt(0); ; i = 1)
    {
      localCursor.close();
      return i == 1;
    }
  }

  void storeAd(VideoAd paramVideoAd)
  {
    ContentValues localContentValues1 = new ContentValues();
    localContentValues1.put("name", paramVideoAd.id);
    localContentValues1.put("acid", paramVideoAd.acid);
    localContentValues1.put("type", Integer.valueOf(paramVideoAd.type));
    putArray(localContentValues1, "startactivity", paramVideoAd.startActivity);
    putArray(localContentValues1, "endactivity", paramVideoAd.endActivity);
    localContentValues1.put("showcontrols", Boolean.valueOf(paramVideoAd.showControls));
    localContentValues1.put("contenturl", paramVideoAd.contentUrl);
    try
    {
      if (paramVideoAd.expiration != null)
        localContentValues1.put("expiration", new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").format(paramVideoAd.expiration).toString());
      localContentValues1.put("deferredviewstart", Long.valueOf(paramVideoAd.deferredViewStart));
      localContentValues1.put("oncompletion", paramVideoAd.onCompletionUrl);
      localContentValues1.put("duration", Long.valueOf(paramVideoAd.duration));
      localContentValues1.put("contentlength", Long.valueOf(paramVideoAd.contentLength));
      localContentValues1.put("stayInPlayer", Boolean.valueOf(paramVideoAd.stayInPlayer));
      localContentValues1.put("sdcard", Boolean.valueOf(paramVideoAd.storedOnSdCard));
      localContentValues1.put("showcountdown", Boolean.valueOf(paramVideoAd.showCountdown));
      putArray(localContentValues1, "log", paramVideoAd.activities.toArray());
      putArray(localContentValues1, "cachecomplete", paramVideoAd.cacheComplete);
      putArray(localContentValues1, "cachefailed", paramVideoAd.cacheFailed);
      putArray(localContentValues1, "videoError", paramVideoAd.videoError);
      long l = this.db.insert("ads", null, localContentValues1);
      for (int i = 0; i < paramVideoAd.buttons.size(); i++)
      {
        VideoImage localVideoImage = (VideoImage)paramVideoAd.buttons.get(i);
        ContentValues localContentValues2 = new ContentValues();
        localContentValues2.put("imageurl", localVideoImage.imageUrl);
        localContentValues2.put("contentlength", Long.valueOf(localVideoImage.contentLength));
        localContentValues2.put("linkurl", localVideoImage.linkUrl);
        localContentValues2.put("overlayorientation", localVideoImage.overlayOrientation);
        putArray(localContentValues2, "activity", localVideoImage.activity);
        localContentValues2.put("position", Integer.valueOf(localVideoImage.position));
        localContentValues2.put("anchor", Integer.valueOf(localVideoImage.anchor));
        localContentValues2.put("position2", Integer.valueOf(localVideoImage.position2));
        localContentValues2.put("anchor2", Integer.valueOf(localVideoImage.anchor2));
        localContentValues2.put("paddingtop", Integer.valueOf(localVideoImage.paddingTop));
        localContentValues2.put("paddingleft", Integer.valueOf(localVideoImage.paddingLeft));
        localContentValues2.put("paddingright", Integer.valueOf(localVideoImage.paddingRight));
        localContentValues2.put("paddingbottom", Integer.valueOf(localVideoImage.paddingBottom));
        localContentValues2.put("appearancedelay", Long.valueOf(localVideoImage.appearanceDelay));
        localContentValues2.put("inactivitytimeout", Long.valueOf(localVideoImage.inactivityTimeout));
        localContentValues2.put("startopacity", Float.valueOf(localVideoImage.fromAlpha));
        localContentValues2.put("endopacity", Float.valueOf(localVideoImage.toAlpha));
        localContentValues2.put("fadeduration", Long.valueOf(localVideoImage.fadeDuration));
        localContentValues2.put("adid", Long.valueOf(l));
        this.db.insert("buttons", null, localContentValues2);
      }
    }
    catch (Exception localException)
    {
      while (true)
        localException.printStackTrace();
    }
  }

  void updateAdData(VideoAd paramVideoAd)
  {
    if (paramVideoAd == null);
    while (true)
    {
      return;
      ContentValues localContentValues1 = new ContentValues();
      putArray(localContentValues1, "startactivity", paramVideoAd.startActivity);
      putArray(localContentValues1, "endactivity", paramVideoAd.endActivity);
      putArray(localContentValues1, "log", paramVideoAd.activities.toArray());
      localContentValues1.put("deferredviewstart", new Long(System.currentTimeMillis()));
      SQLiteDatabase localSQLiteDatabase1 = this.db;
      String[] arrayOfString1 = new String[1];
      arrayOfString1[0] = paramVideoAd.id;
      localSQLiteDatabase1.update("ads", localContentValues1, "ads.name=?", arrayOfString1);
      int i = getIdForAdName(paramVideoAd.id);
      if (i > 0)
        for (int j = 0; j < paramVideoAd.buttons.size(); j++)
        {
          VideoImage localVideoImage = (VideoImage)paramVideoAd.buttons.get(j);
          ContentValues localContentValues2 = new ContentValues();
          localContentValues2.put("linkurl", localVideoImage.linkUrl);
          localContentValues2.put("overlayorientation", localVideoImage.overlayOrientation);
          putArray(localContentValues2, "activity", localVideoImage.activity);
          localContentValues2.put("position", new Integer(localVideoImage.position));
          localContentValues2.put("anchor", new Integer(localVideoImage.anchor));
          localContentValues2.put("position2", new Integer(localVideoImage.position2));
          localContentValues2.put("anchor2", new Integer(localVideoImage.anchor2));
          localContentValues2.put("paddingtop", new Integer(localVideoImage.paddingTop));
          localContentValues2.put("paddingleft", new Integer(localVideoImage.paddingLeft));
          localContentValues2.put("paddingbottom", new Integer(localVideoImage.paddingBottom));
          localContentValues2.put("paddingright", new Integer(localVideoImage.paddingRight));
          localContentValues2.put("appearancedelay", new Long(localVideoImage.appearanceDelay));
          localContentValues2.put("inactivitytimeout", new Long(localVideoImage.inactivityTimeout));
          localContentValues2.put("startopacity", new Float(localVideoImage.fromAlpha));
          localContentValues2.put("endopacity", new Float(localVideoImage.toAlpha));
          localContentValues2.put("fadeduration", new Long(localVideoImage.fadeDuration));
          SQLiteDatabase localSQLiteDatabase2 = this.db;
          String[] arrayOfString2 = new String[2];
          arrayOfString2[0] = String.valueOf(i);
          arrayOfString2[1] = localVideoImage.imageUrl;
          localSQLiteDatabase2.update("buttons", localContentValues2, "buttons.adid=? AND buttons.imageurl=? ", arrayOfString2);
        }
    }
  }

  void updateAdOnSDCard(String paramString, int paramInt)
  {
    this.db.rawQuery("UPDATE ads SET sdcard = " + paramInt + " WHERE ads.name" + "='" + paramString + "'", null).close();
  }
}

/* Location:           D:\code\Research\Android\apks\gbis.gbandroid-49\gbis.gbandroid-49_dex2jar.jar
 * Qualified Name:     com.millennialmedia.android.AdDatabaseHelper
 * JD-Core Version:    0.6.2
 */
