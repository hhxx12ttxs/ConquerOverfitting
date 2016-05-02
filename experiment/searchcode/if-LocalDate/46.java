package net.flixster.android.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;
import com.flixster.android.activity.ImageViewHandler;
import com.flixster.android.model.Image;
import com.flixster.android.utils.DateTimeHelper;
import com.flixster.android.utils.Logger;
import com.flixster.android.utils.Properties;
import com.flixster.android.utils.Translator;
import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import net.flixster.android.FlixUtils;
import net.flixster.android.FlixsterApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Movie extends VideoAsset
{
  private static final String[] BASIC_PROPERTY_ARRAY = { "title", "mpaa", "runningTime", "synopsis", "status" };
  public static final String FRIENDS_AVERAGE = "FRIENDS_AVERAGE";
  public static final String FRIENDS_NOT_INTERESTED_COUNT = "FRIENDS_NOT_INTERESTED_COUNT";
  public static final String FRIENDS_RATED_COUNT = "FRIENDS_RATED_COUNT";
  public static final String FRIENDS_WANT_TO_SEE_COUNT = "FRIENDS_WTS_COUNT";
  private static final String[] FULL_PROPERTY_ARRAY = { "title", "mpaa", "directors", "runningTime", "meta", "MOVIE_ACTORS", "MOVIE_ACTORS_SHORT", "MOVIE_ACTORS_SHORT", "status", "genre", "thumbnail", "flixster", "imdb", "rottentomatoes", "profile", "theaterReleaseDate", "dvdReleaseDate", "high", "med", "low" };
  private static final String[] FULL_PROPERTY_INT_ARRAY = { "boxOffice", "popcornScore", "rottenTomatoes", "numWantToSee", "THUMBNAIL_STATUS_INT", "featuredId" };
  public static final String MOVIE_ACTORS = "MOVIE_ACTORS";
  public static final String MOVIE_ACTORS_SHORT = "MOVIE_ACTORS_SHORT";
  public static final String MOVIE_DETAILED_URL = "detailed";
  public static final String MOVIE_DIRECTORS = "directors";
  public static final String MOVIE_DVD_RELEASE_DATE_STRING = "dvdReleaseDate";
  public static final String MOVIE_FEATUREID = "featuredId";
  public static final String MOVIE_FLIXSTER_URL = "flixster";
  public static final String MOVIE_GENRE = "genre";
  public static final String MOVIE_GROSS_INT = "boxOffice";
  public static final String MOVIE_ID = "MOVIE_ID";
  public static final String MOVIE_IMDB_URL = "imdb";
  public static final String MOVIE_META = "meta";
  public static final String MOVIE_MPAA = "mpaa";
  public static final String MOVIE_NETFLIX_URL = "netflix";
  private static final String MOVIE_NONE_MOB = "movie.none.mob.gif";
  public static final String MOVIE_NUM_REVIEWS = "criticsNumReviews";
  public static final String MOVIE_PHOTO_URLS = "photoUrls";
  public static final String MOVIE_POPCORN_SCORE_INT = "popcornScore";
  public static final String MOVIE_POPCORN_SCORE_STRING = "MOVIE_POPCORN_SCORE_STRING";
  public static final String MOVIE_PROFILE_URL = "profile";
  public static final String MOVIE_RT_SCORE_INT = "rottenTomatoes";
  public static final String MOVIE_RT_SCORE_STRING = "MOVIE_RT_SCORE_STRING";
  public static final String MOVIE_RT_URL = "rottentomatoes";
  public static final String MOVIE_RUNNINGTIME = "runningTime";
  public static final String MOVIE_STATUS = "status";
  public static final String MOVIE_SYNOPSIS = "synopsis";
  public static final String MOVIE_THEATER_RELEASE_DATE_STRING = "theaterReleaseDate";
  public static final String MOVIE_THUMBNAIL_URL = "thumbnail";
  public static final String MOVIE_TITLE = "title";
  public static final String MOVIE_TRAILER_HIGH_URL = "high";
  public static final String MOVIE_TRAILER_LOW_URL = "low";
  public static final String MOVIE_TRAILER_MED_URL = "med";
  public static final String MOVIE_WTS_INT = "numWantToSee";
  private static final String[] NETFLIX_BASIC_PROPERTY_ARRAY = { "title", "mpaa", "runningTime", "status" };
  public static final String RATING_PG = "PG";
  public static final String RATING_PG13 = "PG-13";
  public static final String RATING_R = "R";
  public static final String RATING_UNRATED = "Unrated";
  public static final String SQL_TABLE = "movie_cache";
  public static final String STRING_ROTTEN_TOMATOES = "Rotten Tomatoes";
  public static final String TAG_DRAMA = "Drama";
  public static final String TAG_INACTIVE = "Inactive";
  public static final String TAG_LIVE = "Live";
  public static final String TAG_OPENING_THIS_WEEK = "Opening This Week";
  public static final String TAG_PENDINGCONTENTSOURCE = "Pending Content Source";
  public static final String TAG_ROMANCE = "Romance";
  public static final String TAG_TOP_BOX_OFFICE = "Top Box Office";
  public static final String THUMBNAIL_STATUS_INT = "THUMBNAIL_STATUS_INT";
  public static final String TYPE_ROTTENTOMATOES = "rottentomatoes";
  private HashMap<Long, Actor> actorsHash;
  private HashSet<String> criticReviewsHash;
  private ArrayList<Review> criticReviewsList;
  private int criticsNumReviews;
  private Image detailBitmap;
  private HashMap<Long, Actor> directorsHash;
  private Date dvdReleaseDate;
  private HashSet<String> friendReviewsHash;
  private String[] genres;
  private boolean isCertifiedFresh;
  public boolean isMIT;
  private boolean isMovieDetailsApiParsed;
  public boolean isUpcoming;
  public String mActorPageChars;
  public ArrayList<Actor> mActors;
  public ArrayList<Actor> mDirectors;
  private ArrayList<Review> mFriendRatedReviews;
  private ArrayList<Review> mFriendWantToSeeReviews;
  private HashMap<String, Integer> mIntPropertyMap;
  public ArrayList<Photo> mPhotos;
  private HashMap<String, String> mPropertyMap;
  private long mThumbOrderStamp = 0L;
  public int photoCount;
  private Image profileBitmapNew;
  private boolean tagsParsed;
  private Date theaterReleaseDate;
  public SoftReference<Bitmap> thumbnailSoftBitmap = new SoftReference(null);
  private int userNumRatings;
  private HashSet<String> userReviewsHash;
  private ArrayList<Review> userReviewsList;

  public Movie(long paramLong)
  {
    this.id = paramLong;
    this.mPropertyMap = new HashMap();
    this.mIntPropertyMap = new HashMap();
  }

  private void arrayParseJSONObject(JSONObject paramJSONObject, String[] paramArrayOfString)
  {
    while (true)
    {
      int j;
      try
      {
        int i = paramArrayOfString.length;
        j = 0;
        if (j >= i)
        {
          buildMeta();
          return;
        }
        str1 = paramArrayOfString[j];
        if ((!this.mPropertyMap.containsKey(str1)) && (paramJSONObject.has(str1)))
          if (str1.equals("synopsis"))
          {
            String str3 = paramJSONObject.getString(str1);
            this.mPropertyMap.put(str1, str3.replaceAll("&quot;", "\""));
          }
          else
          {
            localObject = paramJSONObject.getString(str1);
            if (str1.equals("status"))
              if (((String)localObject).contentEquals("Live"))
              {
                localObject = "Live";
                if (((String)localObject).length() <= 0)
                  break label311;
                this.mPropertyMap.put(str1, localObject);
              }
          }
      }
      catch (JSONException localJSONException)
      {
        String str1;
        Logger.e("FlxMain", "arrayParseError ", localJSONException);
        continue;
        if (((String)localObject).contentEquals("Pending Content Source"))
        {
          localObject = "Pending Content Source";
          continue;
        }
        if (((String)localObject).contentEquals("Inactive"))
        {
          localObject = "Inactive";
          continue;
        }
        Object localObject = FlixUtils.copyString((String)localObject);
        continue;
        if (str1.equals("mpaa"))
        {
          if (((String)localObject).contentEquals("R"))
          {
            localObject = "R";
            continue;
          }
          if (((String)localObject).contentEquals("Unrated"))
          {
            localObject = "Unrated";
            continue;
          }
          if (((String)localObject).contentEquals("PG-13"))
          {
            localObject = "PG-13";
            continue;
          }
          if (((String)localObject).contentEquals("PG"))
          {
            localObject = "PG";
            continue;
          }
          localObject = FlixUtils.copyString((String)localObject);
          continue;
        }
        if (((String)localObject).length() <= 0)
          continue;
        String str2 = FlixUtils.copyString((String)localObject);
        localObject = str2;
        continue;
      }
      label311: j++;
    }
  }

  private void netflixParseReviews(JSONObject paramJSONObject)
    throws JSONException
  {
    if (paramJSONObject.has("reviews"))
    {
      JSONObject localJSONObject1 = paramJSONObject.getJSONObject("reviews");
      if (localJSONObject1.has("flixster"))
      {
        JSONObject localJSONObject2 = localJSONObject1.getJSONObject("flixster");
        int j = localJSONObject2.getInt("popcornScore");
        if (j != 0)
          this.mIntPropertyMap.put("popcornScore", Integer.valueOf(j));
        this.mIntPropertyMap.put("numWantToSee", Integer.valueOf(localJSONObject2.getInt("numWantToSee")));
      }
      if (localJSONObject1.has("rottenTomatoes"))
      {
        int i = localJSONObject1.getJSONObject("rottenTomatoes").getInt("rating");
        if (i != 0)
          this.mIntPropertyMap.put("rottenTomatoes", Integer.valueOf(i));
      }
    }
  }

  private void parseDirectors(JSONObject paramJSONObject)
    throws JSONException
  {
    JSONArray localJSONArray;
    int i;
    StringBuilder localStringBuilder;
    if ((!this.mPropertyMap.containsKey("directors")) && (paramJSONObject.has("directors")))
    {
      localJSONArray = paramJSONObject.getJSONArray("directors");
      i = localJSONArray.length();
      localStringBuilder = new StringBuilder();
    }
    for (int j = 0; ; j++)
    {
      if (j >= i)
      {
        this.mPropertyMap.put("directors", localStringBuilder.toString());
        return;
      }
      localStringBuilder.append(localJSONArray.getJSONObject(j).getString("name"));
      if (j + 1 < i)
        localStringBuilder.append(", ");
    }
  }

  private void parseDvdReleaseDateString(JSONObject paramJSONObject)
    throws JSONException
  {
    if (((!this.mPropertyMap.containsKey("dvdReleaseDate")) || (this.dvdReleaseDate == null)) && (paramJSONObject.has("dvdReleaseDate")))
    {
      JSONObject localJSONObject = paramJSONObject.getJSONObject("dvdReleaseDate");
      String str1 = localJSONObject.getString("year");
      String str2 = localJSONObject.getString("month");
      String str3 = localJSONObject.getString("day");
      if (str3.length() > 0)
      {
        this.dvdReleaseDate = new GregorianCalendar(Integer.parseInt(str1), -1 + Integer.parseInt(str2), Integer.parseInt(str3)).getTime();
        this.mPropertyMap.put("dvdReleaseDate", DateTimeHelper.mediumDateFormatter().format(this.dvdReleaseDate));
      }
    }
  }

  private void parseFeatureId(JSONObject paramJSONObject)
    throws JSONException
  {
    if ((!this.mIntPropertyMap.containsKey("featuredId")) && (paramJSONObject.has("featuredId")))
      this.mIntPropertyMap.put("featuredId", Integer.valueOf(paramJSONObject.getInt("featuredId")));
  }

  private void parseFriends(JSONObject paramJSONObject)
    throws JSONException
  {
    JSONArray localJSONArray;
    int i;
    if (paramJSONObject.has("friends"))
    {
      JSONObject localJSONObject1 = paramJSONObject.getJSONObject("friends");
      if (localJSONObject1 != null)
      {
        if (localJSONObject1.has("stats"))
        {
          JSONObject localJSONObject5 = localJSONObject1.getJSONObject("stats");
          this.mPropertyMap.put("FRIENDS_AVERAGE", Double.toString(localJSONObject5.getDouble("average")));
          this.mIntPropertyMap.put("FRIENDS_RATED_COUNT", Integer.valueOf(localJSONObject5.getInt("numRated")));
          this.mIntPropertyMap.put("FRIENDS_WTS_COUNT", Integer.valueOf(localJSONObject5.getInt("numWantToSee")));
          this.mIntPropertyMap.put("FRIENDS_NOT_INTERESTED_COUNT", Integer.valueOf(localJSONObject5.getInt("numNotInterested")));
        }
        if (localJSONObject1.has("ratings"))
        {
          localJSONArray = localJSONObject1.optJSONArray("ratings");
          if ((this.mFriendWantToSeeReviews == null) || (this.mFriendRatedReviews == null))
          {
            this.mFriendWantToSeeReviews = new ArrayList();
            this.mFriendRatedReviews = new ArrayList();
            this.friendReviewsHash = new HashSet();
          }
          i = 0;
          if (i < localJSONArray.length())
            break label202;
        }
      }
    }
    return;
    label202: JSONObject localJSONObject2 = localJSONArray.getJSONObject(i);
    String str1 = FlixUtils.copyString(localJSONObject2.getString("id"));
    Review localReview;
    String str2;
    if (!this.friendReviewsHash.contains(str1))
    {
      localReview = new Review();
      localReview.type = 2;
      localReview.id = str1;
      if (localJSONObject2.has("user"))
      {
        JSONObject localJSONObject3 = localJSONObject2.getJSONObject("user");
        localReview.userId = localJSONObject3.getLong("id");
        localReview.userName = FlixUtils.copyString(localJSONObject3.getString("userName"));
        localReview.name = FlixUtils.copyString(localJSONObject3.getString("firstName"));
        if (localJSONObject3.has("lastName"))
          localReview.name += FlixUtils.copyString(new StringBuilder(" ").append(localJSONObject3.getString("lastName")).toString());
        if (localJSONObject3.has("images"))
        {
          JSONObject localJSONObject4 = localJSONObject3.getJSONObject("images");
          if (localJSONObject4.has("thumbnail"))
            localReview.mugUrl = FlixUtils.copyString(localJSONObject4.getString("thumbnail"));
        }
      }
      if (localJSONObject2.has("review"))
        localReview.comment = FlixUtils.copyString(localJSONObject2.getString("review"));
      str2 = FlixUtils.copyString(localJSONObject2.getString("score"));
      if (!str2.contentEquals("+"))
        break label518;
      localReview.stars = 5.5D;
      this.mFriendWantToSeeReviews.add(localReview);
    }
    while (true)
    {
      this.friendReviewsHash.add(str1);
      i++;
      break;
      label518: if (str2.contentEquals("-"))
      {
        localReview.stars = 6.0D;
      }
      else if (str2.length() > 0)
      {
        localReview.stars = Double.valueOf(str2).doubleValue();
        this.mFriendRatedReviews.add(localReview);
      }
      else
      {
        localReview.stars = 0.0D;
        this.mFriendRatedReviews.add(localReview);
      }
    }
  }

  private void parseGenre(JSONObject paramJSONObject)
    throws JSONException
  {
    JSONArray localJSONArray;
    StringBuilder localStringBuilder;
    int j;
    int k;
    if ((!this.tagsParsed) && (paramJSONObject.has("tags")))
    {
      localJSONArray = paramJSONObject.getJSONArray("tags");
      int i = localJSONArray.length();
      this.genres = new String[i];
      localStringBuilder = new StringBuilder();
      j = 0;
      k = 0;
      if (j >= i)
        this.tagsParsed = true;
    }
    else
    {
      return;
    }
    String str1 = localJSONArray.getString(j);
    int m;
    if (str1.equals("Top Box Office"))
    {
      this.mPropertyMap.put("Top Box Office", "Top Box Office");
      m = k;
    }
    while (true)
    {
      this.mPropertyMap.put("genre", localStringBuilder.toString());
      j++;
      k = m;
      break;
      if (str1.equals("Opening This Week"))
      {
        this.mPropertyMap.put("Opening This Week", "Opening This Week");
        m = k;
      }
      else
      {
        if (localStringBuilder.length() > 0)
          localStringBuilder.append(", ");
        String str2 = Translator.instance().translateGenre(str1);
        localStringBuilder.append(str2);
        String[] arrayOfString = this.genres;
        m = k + 1;
        arrayOfString[k] = str2;
      }
    }
  }

  private void parsePosterUrls(JSONObject paramJSONObject)
  {
    if (paramJSONObject != null)
    {
      this.mPropertyMap.put("thumbnail", FlixUtils.copyString(paramJSONObject.optString("thumbnail", null)));
      this.mPropertyMap.put("profile", FlixUtils.copyString(paramJSONObject.optString("profile", null)));
      if (Properties.instance().isGoogleTv())
        this.mPropertyMap.put("detailed", FlixUtils.copyString(paramJSONObject.optString("detailed", null)));
    }
  }

  private void parseReviews(JSONObject paramJSONObject)
    throws JSONException
  {
    JSONArray localJSONArray2;
    int m;
    JSONArray localJSONArray1;
    int j;
    if (paramJSONObject.has("reviews"))
    {
      JSONObject localJSONObject1 = paramJSONObject.getJSONObject("reviews");
      if (localJSONObject1.has("flixster"))
      {
        JSONObject localJSONObject8 = localJSONObject1.getJSONObject("flixster");
        int i2 = localJSONObject8.getInt("popcornScore");
        if (i2 != 0)
          this.mIntPropertyMap.put("popcornScore", Integer.valueOf(i2));
        this.mIntPropertyMap.put("numWantToSee", Integer.valueOf(localJSONObject8.getInt("numWantToSee")));
        this.userNumRatings = (localJSONObject8.optInt("numScores") + localJSONObject8.optInt("numWantToSee") + localJSONObject8.optInt("numNotInterested"));
      }
      if (localJSONObject1.has("rottenTomatoes"))
      {
        JSONObject localJSONObject7 = localJSONObject1.getJSONObject("rottenTomatoes");
        int i1 = localJSONObject7.getInt("rating");
        if (i1 != 0)
          this.mIntPropertyMap.put("rottenTomatoes", Integer.valueOf(i1));
        this.isCertifiedFresh = localJSONObject7.optBoolean("certifiedFresh");
      }
      this.criticsNumReviews = localJSONObject1.optInt("criticsNumReviews");
      if (localJSONObject1.has("critics"))
      {
        localJSONArray2 = localJSONObject1.getJSONArray("critics");
        int k = localJSONArray2.length();
        if (this.criticReviewsList == null)
          this.criticReviewsList = new ArrayList();
        if ((this.criticReviewsList == null) || (this.criticReviewsHash == null))
          this.criticReviewsHash = new HashSet();
        m = 0;
        if (m < k)
          break label325;
      }
      if (localJSONObject1.has("recent"))
      {
        localJSONArray1 = localJSONObject1.getJSONArray("recent");
        int i = localJSONArray1.length();
        if (this.userReviewsList == null)
        {
          this.userReviewsList = new ArrayList();
          this.userReviewsHash = new HashSet();
        }
        j = 0;
        if (j < i)
          break label615;
      }
    }
    return;
    label325: JSONObject localJSONObject5 = localJSONArray2.getJSONObject(m);
    String str3 = localJSONObject5.getString("name");
    String str4;
    label358: Review localReview2;
    String str5;
    if (str3.contentEquals("Rotten Tomatoes"))
    {
      str4 = "Rotten Tomatoes";
      if ((!this.criticReviewsHash.contains(str4)) && (!str4.contentEquals("Rotten Tomatoes")))
      {
        localReview2 = new Review();
        str5 = localJSONObject5.getString("source");
        if (!str5.contentEquals("Rotten Tomatoes"))
          break label605;
      }
    }
    label605: for (String str6 = "Rotten Tomatoes"; ; str6 = FlixUtils.copyString(str5))
    {
      String str7 = localJSONObject5.getString("review").replaceAll("<em>", "").replaceAll("</em>", "");
      boolean bool = localJSONObject5.has("url");
      String str8 = null;
      if (bool)
        str8 = FlixUtils.copyString(localJSONObject5.getString("url"));
      if (localJSONObject5.has("images"))
      {
        JSONObject localJSONObject6 = localJSONObject5.getJSONObject("images");
        if (localJSONObject6.has("thumbnail"))
          localReview2.mugUrl = FlixUtils.copyString(localJSONObject6.getString("thumbnail"));
      }
      int n = localJSONObject5.getInt("rating");
      localReview2.type = 0;
      localReview2.name = str4;
      localReview2.source = str6;
      localReview2.comment = str7;
      localReview2.score = n;
      localReview2.url = str8;
      this.criticReviewsHash.add(str4);
      this.criticReviewsList.add(localReview2);
      m++;
      break;
      str4 = FlixUtils.copyString(str3);
      break label358;
    }
    label615: JSONObject localJSONObject2 = localJSONArray1.getJSONObject(j);
    String str1 = FlixUtils.copyString(localJSONObject2.getString("id"));
    Review localReview1;
    String str2;
    if (!this.userReviewsHash.contains(str1))
    {
      JSONObject localJSONObject3 = localJSONObject2.getJSONObject("user");
      localReview1 = new Review();
      localReview1.type = 1;
      if (localJSONObject3.has("id"))
        localReview1.userId = localJSONObject3.getLong("id");
      localReview1.userName = FlixUtils.copyString(localJSONObject3.getString("userName"));
      Object[] arrayOfObject = new Object[2];
      arrayOfObject[0] = localJSONObject3.getString("firstName");
      arrayOfObject[1] = localJSONObject3.getString("lastName");
      localReview1.name = String.format("%s %s", arrayOfObject);
      if (localJSONObject3.has("images"))
      {
        JSONObject localJSONObject4 = localJSONObject3.getJSONObject("images");
        if (localJSONObject4.has("thumbnail"))
          localReview1.mugUrl = FlixUtils.copyString(localJSONObject4.getString("thumbnail"));
      }
      if (localJSONObject2.has("review"))
        localReview1.comment = FlixUtils.copyString(localJSONObject2.getString("review")).replace("&quot;", "\"");
      if (localJSONObject2.has("score"))
      {
        str2 = localJSONObject2.getString("score");
        if (!str2.contentEquals("+"))
          break label904;
        localReview1.stars = 5.5D;
      }
    }
    while (true)
    {
      this.userReviewsHash.add(str1);
      this.userReviewsList.add(localReview1);
      j++;
      break;
      label904: if (str2.contentEquals("-"))
        localReview1.stars = 6.0D;
      else if (str2.length() > 0)
        localReview1.stars = Double.valueOf(str2).doubleValue();
      else
        localReview1.stars = 0.0D;
    }
  }

  private void parseTheaterReleaseDateString(JSONObject paramJSONObject)
    throws JSONException
  {
    if (((!this.mPropertyMap.containsKey("theaterReleaseDate")) || (this.theaterReleaseDate == null)) && (paramJSONObject.has("theaterReleaseDate")))
    {
      JSONObject localJSONObject = paramJSONObject.getJSONObject("theaterReleaseDate");
      String str1 = localJSONObject.getString("year");
      String str2 = localJSONObject.getString("month");
      String str3 = localJSONObject.getString("day");
      if (str3.length() > 0)
      {
        this.theaterReleaseDate = new GregorianCalendar(Integer.parseInt(str1), -1 + Integer.parseInt(str2), Integer.parseInt(str3)).getTime();
        this.mPropertyMap.put("theaterReleaseDate", DateTimeHelper.mediumDateFormatter().format(this.theaterReleaseDate));
      }
    }
  }

  private void parseTrailer(JSONObject paramJSONObject)
    throws JSONException
  {
    if (paramJSONObject.has("trailer"))
    {
      JSONObject localJSONObject = paramJSONObject.getJSONObject("trailer");
      if (localJSONObject.has("high"))
        this.mPropertyMap.put("high", FlixUtils.copyString(localJSONObject.getString("high")));
      if (localJSONObject.has("med"))
        this.mPropertyMap.put("med", FlixUtils.copyString(localJSONObject.getString("med")));
      if (localJSONObject.has("low"))
        this.mPropertyMap.put("low", FlixUtils.copyString(localJSONObject.getString("low")));
    }
  }

  public boolean IsDoubleclickDvd()
  {
    Date localDate1 = getDvdReleaseDate();
    Date localDate2 = getTheaterReleaseDate();
    if ((localDate1 != null) && (FlixsterApplication.sToday.after(localDate1)));
    for (boolean bool = true; ; bool = false)
    {
      if ((!bool) && (localDate2 != null))
      {
        GregorianCalendar localGregorianCalendar = new GregorianCalendar();
        localGregorianCalendar.setTime(localDate2);
        localGregorianCalendar.add(2, 3);
        bool = localGregorianCalendar.before(FlixsterApplication.sToday);
      }
      return bool;
    }
  }

  public void buildMeta()
  {
    ArrayList localArrayList = new ArrayList();
    String str1 = getProperty("mpaa");
    if ((str1 != null) && (str1.length() > 0))
      localArrayList.add(str1);
    String str2 = getProperty("runningTime");
    if ((str2 != null) && (str2.length() > 0))
      localArrayList.add(str2);
    String str3 = TextUtils.join(", ", localArrayList);
    this.mPropertyMap.put("meta", str3);
  }

  public boolean checkIntProperty(String paramString)
  {
    return this.mIntPropertyMap.containsKey(paramString);
  }

  public boolean checkProperty(String paramString)
  {
    return this.mPropertyMap.containsKey(paramString);
  }

  public int getAudienceScore()
  {
    Integer localInteger = getIntProperty("popcornScore");
    if (localInteger == null)
      return 0;
    return localInteger.intValue();
  }

  public int getAudienceScoreIconId()
  {
    if (!isReleased())
      return 2130837750;
    if (isSpilled())
      return 2130837743;
    return 2130837738;
  }

  public String getCastShort()
  {
    return getProperty("MOVIE_ACTORS_SHORT");
  }

  public ArrayList<Review> getCriticReviewsList()
  {
    return this.criticReviewsList;
  }

  public <T extends ImageView> Bitmap getDetailBitmap(T paramT)
  {
    if (this.detailBitmap == null)
      this.detailBitmap = new Image(getDetailPoster());
    return this.detailBitmap.getBitmap(paramT);
  }

  public Bitmap getDetailBitmap(ImageViewHandler paramImageViewHandler)
  {
    if (this.detailBitmap == null)
      this.detailBitmap = new Image(getDetailPoster());
    return this.detailBitmap.getBitmap(paramImageViewHandler);
  }

  public String getDetailPoster()
  {
    return getProperty("detailed");
  }

  public Date getDvdReleaseDate()
  {
    return this.dvdReleaseDate;
  }

  public Integer getFeaturedId()
  {
    return getIntProperty("featuredId");
  }

  public ArrayList<Review> getFriendRatedReviewsList()
  {
    return this.mFriendRatedReviews;
  }

  public int getFriendRatedReviewsListSize()
  {
    if (this.mFriendRatedReviews == null)
      return 0;
    return this.mFriendRatedReviews.size();
  }

  public String getFriendStat(Resources paramResources)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    Integer localInteger1 = getIntProperty("FRIENDS_RATED_COUNT");
    int j;
    if ((localInteger1 != null) && (localInteger1.intValue() > 0))
    {
      localStringBuilder.append(localInteger1).append(" ");
      if (localInteger1.intValue() > 1)
      {
        j = 2131493008;
        localStringBuilder.append(paramResources.getString(j));
      }
    }
    while (true)
      if (localStringBuilder.length() > 0)
      {
        return localStringBuilder.toString();
        j = 2131493007;
        break;
        Integer localInteger2 = getIntProperty("FRIENDS_WTS_COUNT");
        if ((localInteger2 != null) && (localInteger2.intValue() > 0))
        {
          localStringBuilder.append(localInteger2).append(" ");
          if (localInteger2.intValue() > 1);
          for (int i = 2131493012; ; i = 2131493011)
          {
            localStringBuilder.append(paramResources.getString(i));
            break;
          }
        }
      }
    return null;
  }

  public ArrayList<Review> getFriendWantToSeeList()
  {
    return this.mFriendWantToSeeReviews;
  }

  public int getFriendWantToSeeListSize()
  {
    if (this.mFriendWantToSeeReviews == null)
      return 0;
    return this.mFriendWantToSeeReviews.size();
  }

  public String[] getGenres()
  {
    return this.genres;
  }

  public Integer getIntProperty(String paramString)
  {
    return (Integer)this.mIntPropertyMap.get(paramString);
  }

  public String getMpaa()
  {
    return getProperty("mpaa");
  }

  public String getMpaaRuntime()
  {
    return getProperty("meta");
  }

  public int getNumCriticReviews()
  {
    return this.criticsNumReviews;
  }

  public int getNumUserRatings()
  {
    return this.userNumRatings;
  }

  public <T extends ImageView> Bitmap getProfileBitmap(T paramT)
  {
    if (this.profileBitmapNew == null)
      this.profileBitmapNew = new Image(getProfilePoster());
    return this.profileBitmapNew.getBitmap(paramT);
  }

  public String getProfilePoster()
  {
    return getProperty("profile");
  }

  public String getProperty(String paramString)
  {
    return (String)this.mPropertyMap.get(paramString);
  }

  public String getReleaseYear()
  {
    Date localDate = getTheaterReleaseDate();
    String str = null;
    if (localDate != null)
      str = String.valueOf(1900 + localDate.getYear());
    return str;
  }

  public Bitmap getSoftThumbnail()
  {
    return (Bitmap)this.thumbnailSoftBitmap.get();
  }

  public String getSynopsis()
  {
    return getProperty("synopsis");
  }

  public Date getTheaterReleaseDate()
  {
    return this.theaterReleaseDate;
  }

  public long getThumbOrderStamp()
  {
    return this.mThumbOrderStamp;
  }

  public <T extends ImageView> Bitmap getThumbnailBackedProfileBitmap(T paramT)
  {
    if (this.profileBitmapNew == null)
      this.profileBitmapNew = new Image(getProfilePoster());
    Bitmap localBitmap = this.profileBitmapNew.getBitmap(paramT);
    if (localBitmap == null)
      localBitmap = (Bitmap)this.thumbnailSoftBitmap.get();
    return localBitmap;
  }

  public String getThumbnailPoster()
  {
    String str = getProperty("thumbnail");
    if ((str != null) && (str.endsWith("movie.none.mob.gif")))
      str = null;
    return str;
  }

  public String getTitle()
  {
    return getProperty("title");
  }

  public int getTomatometer()
  {
    Integer localInteger = getIntProperty("rottenTomatoes");
    if (localInteger == null)
      return -1;
    return localInteger.intValue();
  }

  public int getTomatometerIconId()
  {
    if (getTomatometer() < 60)
      return 2130837741;
    return 2130837726;
  }

  public String getTrailerHigh()
  {
    return getProperty("high");
  }

  public String getTrailerLow()
  {
    return getProperty("low");
  }

  public String getTrailerMed()
  {
    return getProperty("med");
  }

  public ArrayList<Review> getUserReviewsList()
  {
    return this.userReviewsList;
  }

  public boolean hasTrailer()
  {
    return (getTrailerHigh() != null) || (getTrailerMed() != null) || (getTrailerLow() != null);
  }

  public void hintTitle(String paramString)
  {
    if ((paramString != null) && (!this.mPropertyMap.containsKey("title")))
      setProperty("title", FlixUtils.copyString(paramString));
  }

  public boolean isCertifiedFresh()
  {
    return this.isCertifiedFresh;
  }

  public boolean isDetailsApiParsed()
  {
    return this.isMovieDetailsApiParsed;
  }

  public boolean isFeatured()
  {
    return getIntProperty("featuredId") != null;
  }

  public boolean isFresh()
  {
    return getTomatometer() >= 60;
  }

  public boolean isReleased()
  {
    return (getTheaterReleaseDate() == null) || (FlixsterApplication.sToday.after(getTheaterReleaseDate()));
  }

  public boolean isSpilled()
  {
    return getAudienceScore() < 60;
  }

  public void merge(Movie paramMovie)
  {
    int i = 0;
    if (this == paramMovie)
      return;
    String[] arrayOfString1 = FULL_PROPERTY_ARRAY;
    int j = arrayOfString1.length;
    int k = 0;
    label19: String[] arrayOfString2;
    int m;
    if (k >= j)
    {
      arrayOfString2 = FULL_PROPERTY_INT_ARRAY;
      m = arrayOfString2.length;
    }
    while (true)
    {
      if (i >= m)
      {
        if ((this.thumbnailSoftBitmap.get() != null) || (paramMovie.thumbnailSoftBitmap.get() == null))
          break;
        this.thumbnailSoftBitmap = new SoftReference((Bitmap)paramMovie.thumbnailSoftBitmap.get());
        return;
        String str1 = arrayOfString1[k];
        if ((!checkProperty(str1)) && (paramMovie.checkProperty(str1)))
          setProperty(str1, paramMovie.getProperty(str1));
        k++;
        break label19;
      }
      String str2 = arrayOfString2[i];
      if ((!checkIntProperty(str2)) && (paramMovie.checkIntProperty(str2)))
        setIntProperty(str2, paramMovie.getIntProperty(str2));
      i++;
    }
  }

  public Movie netflixParseFromJSON(JSONObject paramJSONObject)
    throws JSONException
  {
    Long localLong = Long.valueOf(paramJSONObject.getLong("id"));
    if (localLong != null)
      this.id = localLong.longValue();
    arrayParseJSONObject(paramJSONObject, NETFLIX_BASIC_PROPERTY_ARRAY);
    if (paramJSONObject.has("playing"))
      this.isMIT = paramJSONObject.getBoolean("playing");
    parsePosterUrls(paramJSONObject.optJSONObject("poster"));
    JSONArray localJSONArray2;
    int k;
    StringBuilder localStringBuilder;
    int m;
    if ((!this.mPropertyMap.containsKey("MOVIE_ACTORS")) && (paramJSONObject.has("actors")))
    {
      localJSONArray2 = paramJSONObject.getJSONArray("actors");
      k = Math.min(localJSONArray2.length(), 2);
      localStringBuilder = new StringBuilder();
      m = 0;
    }
    while (true)
    {
      if (m >= k)
      {
        this.mPropertyMap.put("MOVIE_ACTORS_SHORT", localStringBuilder.toString());
        netflixParseReviews(paramJSONObject);
        if (!paramJSONObject.has("urls"));
      }
      try
      {
        JSONArray localJSONArray1 = paramJSONObject.getJSONArray("urls");
        int i = localJSONArray1.length();
        for (int j = 0; ; j++)
        {
          if (j >= i)
          {
            if ((!this.mIntPropertyMap.containsKey("boxOffice")) && (paramJSONObject.has("boxOffice")))
            {
              String str = paramJSONObject.getString("boxOffice");
              if ((str != null) && (str.length() > 0))
                this.mIntPropertyMap.put("boxOffice", Integer.valueOf(Integer.parseInt(str)));
            }
            return this;
            JSONObject localJSONObject2 = localJSONArray2.getJSONObject(m);
            if (m != 0)
              localStringBuilder.append(", ");
            localStringBuilder.append(localJSONObject2.getString("name"));
            m++;
            break;
          }
          JSONObject localJSONObject1 = localJSONArray1.getJSONObject(j);
          if (localJSONObject1.getString("type").contentEquals("netflix"))
            this.mPropertyMap.put("netflix", FlixUtils.copyString(localJSONObject1.getString("url")));
        }
      }
      catch (JSONException localJSONException)
      {
        while (true)
          Logger.e("FlxMain", "flixsterUrl JSON parse", localJSONException);
      }
    }
  }

  // ERROR //
  public Movie parseFromJSON(JSONObject paramJSONObject)
    throws JSONException
  {
    // Byte code:
    //   0: aload_1
    //   1: ldc_w 411
    //   4: invokevirtual 428	org/json/JSONObject:getLong	(Ljava/lang/String;)J
    //   7: invokestatic 763	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   10: astore_2
    //   11: aload_2
    //   12: ifnull +11 -> 23
    //   15: aload_0
    //   16: aload_2
    //   17: invokevirtual 766	java/lang/Long:longValue	()J
    //   20: putfield 227	net/flixster/android/model/Movie:id	J
    //   23: aload_0
    //   24: aload_1
    //   25: getstatic 209	net/flixster/android/model/Movie:BASIC_PROPERTY_ARRAY	[Ljava/lang/String;
    //   28: invokespecial 768	net/flixster/android/model/Movie:arrayParseJSONObject	(Lorg/json/JSONObject;[Ljava/lang/String;)V
    //   31: aload_1
    //   32: ldc_w 770
    //   35: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   38: ifeq +14 -> 52
    //   41: aload_0
    //   42: aload_1
    //   43: ldc_w 770
    //   46: invokevirtual 773	org/json/JSONObject:getBoolean	(Ljava/lang/String;)Z
    //   49: putfield 775	net/flixster/android/model/Movie:isMIT	Z
    //   52: aload_0
    //   53: aload_1
    //   54: invokespecial 800	net/flixster/android/model/Movie:parseFeatureId	(Lorg/json/JSONObject;)V
    //   57: aload_0
    //   58: aload_1
    //   59: ldc_w 777
    //   62: invokevirtual 780	org/json/JSONObject:optJSONObject	(Ljava/lang/String;)Lorg/json/JSONObject;
    //   65: invokespecial 782	net/flixster/android/model/Movie:parsePosterUrls	(Lorg/json/JSONObject;)V
    //   68: aload_0
    //   69: aload_1
    //   70: invokespecial 802	net/flixster/android/model/Movie:parseTrailer	(Lorg/json/JSONObject;)V
    //   73: aload_0
    //   74: aload_1
    //   75: invokespecial 804	net/flixster/android/model/Movie:parseDirectors	(Lorg/json/JSONObject;)V
    //   78: aload_0
    //   79: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   82: ldc 20
    //   84: invokevirtual 245	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   87: ifne +84 -> 171
    //   90: aload_1
    //   91: ldc_w 784
    //   94: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   97: ifeq +74 -> 171
    //   100: aload_1
    //   101: ldc_w 784
    //   104: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   107: astore 60
    //   109: aload 60
    //   111: invokevirtual 321	org/json/JSONArray:length	()I
    //   114: istore 61
    //   116: ldc_w 552
    //   119: astore 62
    //   121: new 323	java/lang/StringBuilder
    //   124: dup
    //   125: invokespecial 324	java/lang/StringBuilder:<init>	()V
    //   128: astore 63
    //   130: iconst_0
    //   131: istore 64
    //   133: iload 64
    //   135: iload 61
    //   137: if_icmplt +438 -> 575
    //   140: aload 63
    //   142: invokevirtual 328	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   145: astore 67
    //   147: aload_0
    //   148: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   151: ldc 20
    //   153: aload 67
    //   155: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   158: pop
    //   159: aload_0
    //   160: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   163: ldc 22
    //   165: aload 62
    //   167: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   170: pop
    //   171: aload_0
    //   172: aload_1
    //   173: invokespecial 806	net/flixster/android/model/Movie:parseTheaterReleaseDateString	(Lorg/json/JSONObject;)V
    //   176: aload_0
    //   177: aload_1
    //   178: invokespecial 808	net/flixster/android/model/Movie:parseDvdReleaseDateString	(Lorg/json/JSONObject;)V
    //   181: aload_0
    //   182: aload_1
    //   183: invokespecial 810	net/flixster/android/model/Movie:parseReviews	(Lorg/json/JSONObject;)V
    //   186: aload_0
    //   187: aload_1
    //   188: invokespecial 812	net/flixster/android/model/Movie:parseFriends	(Lorg/json/JSONObject;)V
    //   191: aload_1
    //   192: ldc_w 794
    //   195: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   198: ifeq +29 -> 227
    //   201: aload_1
    //   202: ldc_w 794
    //   205: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   208: astore 51
    //   210: aload 51
    //   212: invokevirtual 321	org/json/JSONArray:length	()I
    //   215: istore 52
    //   217: iconst_0
    //   218: istore 53
    //   220: iload 53
    //   222: iload 52
    //   224: if_icmplt +415 -> 639
    //   227: aload_1
    //   228: ldc_w 813
    //   231: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   234: ifeq +14 -> 248
    //   237: aload_0
    //   238: aload_1
    //   239: ldc_w 813
    //   242: invokevirtual 305	org/json/JSONObject:getInt	(Ljava/lang/String;)I
    //   245: putfield 815	net/flixster/android/model/Movie:photoCount	I
    //   248: aload_1
    //   249: ldc_w 817
    //   252: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   255: ifeq +47 -> 302
    //   258: aload_0
    //   259: getfield 819	net/flixster/android/model/Movie:mPhotos	Ljava/util/ArrayList;
    //   262: ifnonnull +596 -> 858
    //   265: aload_0
    //   266: new 404	java/util/ArrayList
    //   269: dup
    //   270: invokespecial 405	java/util/ArrayList:<init>	()V
    //   273: putfield 819	net/flixster/android/model/Movie:mPhotos	Ljava/util/ArrayList;
    //   276: aload_1
    //   277: ldc_w 817
    //   280: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   283: astore 44
    //   285: iconst_0
    //   286: istore 45
    //   288: aload 44
    //   290: invokevirtual 321	org/json/JSONArray:length	()I
    //   293: istore 46
    //   295: iload 45
    //   297: iload 46
    //   299: if_icmplt +569 -> 868
    //   302: aload_1
    //   303: ldc 28
    //   305: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   308: ifeq +71 -> 379
    //   311: aload_1
    //   312: ldc 28
    //   314: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   317: astore 31
    //   319: aload_0
    //   320: getfield 821	net/flixster/android/model/Movie:mDirectors	Ljava/util/ArrayList;
    //   323: ifnonnull +14 -> 337
    //   326: aload_0
    //   327: new 404	java/util/ArrayList
    //   330: dup
    //   331: invokespecial 405	java/util/ArrayList:<init>	()V
    //   334: putfield 821	net/flixster/android/model/Movie:mDirectors	Ljava/util/ArrayList;
    //   337: aload_0
    //   338: getfield 821	net/flixster/android/model/Movie:mDirectors	Ljava/util/ArrayList;
    //   341: ifnull +10 -> 351
    //   344: aload_0
    //   345: getfield 823	net/flixster/android/model/Movie:directorsHash	Ljava/util/HashMap;
    //   348: ifnonnull +14 -> 362
    //   351: aload_0
    //   352: new 229	java/util/HashMap
    //   355: dup
    //   356: invokespecial 230	java/util/HashMap:<init>	()V
    //   359: putfield 823	net/flixster/android/model/Movie:directorsHash	Ljava/util/HashMap;
    //   362: iconst_0
    //   363: istore 32
    //   365: aload 31
    //   367: invokevirtual 321	org/json/JSONArray:length	()I
    //   370: istore 34
    //   372: iload 32
    //   374: iload 34
    //   376: if_icmplt +558 -> 934
    //   379: aload_1
    //   380: ldc_w 784
    //   383: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   386: ifeq +72 -> 458
    //   389: aload_1
    //   390: ldc_w 784
    //   393: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   396: astore 12
    //   398: aload 12
    //   400: invokevirtual 321	org/json/JSONArray:length	()I
    //   403: istore 13
    //   405: aload_0
    //   406: getfield 825	net/flixster/android/model/Movie:mActors	Ljava/util/ArrayList;
    //   409: ifnonnull +14 -> 423
    //   412: aload_0
    //   413: new 404	java/util/ArrayList
    //   416: dup
    //   417: invokespecial 405	java/util/ArrayList:<init>	()V
    //   420: putfield 825	net/flixster/android/model/Movie:mActors	Ljava/util/ArrayList;
    //   423: aload_0
    //   424: getfield 825	net/flixster/android/model/Movie:mActors	Ljava/util/ArrayList;
    //   427: ifnull +10 -> 437
    //   430: aload_0
    //   431: getfield 827	net/flixster/android/model/Movie:actorsHash	Ljava/util/HashMap;
    //   434: ifnonnull +14 -> 448
    //   437: aload_0
    //   438: new 229	java/util/HashMap
    //   441: dup
    //   442: invokespecial 230	java/util/HashMap:<init>	()V
    //   445: putfield 827	net/flixster/android/model/Movie:actorsHash	Ljava/util/HashMap;
    //   448: iconst_0
    //   449: istore 14
    //   451: iload 14
    //   453: iload 13
    //   455: if_icmplt +691 -> 1146
    //   458: aload_1
    //   459: ldc_w 829
    //   462: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   465: ifeq +47 -> 512
    //   468: aload_1
    //   469: ldc_w 829
    //   472: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   475: astore 5
    //   477: aload 5
    //   479: invokevirtual 321	org/json/JSONArray:length	()I
    //   482: istore 6
    //   484: new 323	java/lang/StringBuilder
    //   487: dup
    //   488: invokespecial 324	java/lang/StringBuilder:<init>	()V
    //   491: astore 7
    //   493: iconst_0
    //   494: istore 8
    //   496: iload 8
    //   498: iload 6
    //   500: if_icmplt +968 -> 1468
    //   503: aload_0
    //   504: aload 7
    //   506: invokevirtual 328	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   509: putfield 831	net/flixster/android/model/Movie:mActorPageChars	Ljava/lang/String;
    //   512: aload_0
    //   513: aload_1
    //   514: invokespecial 833	net/flixster/android/model/Movie:parseGenre	(Lorg/json/JSONObject;)V
    //   517: aload_0
    //   518: getfield 234	net/flixster/android/model/Movie:mIntPropertyMap	Ljava/util/HashMap;
    //   521: ldc 43
    //   523: invokevirtual 245	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   526: ifne +47 -> 573
    //   529: aload_1
    //   530: ldc 43
    //   532: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   535: ifeq +38 -> 573
    //   538: aload_1
    //   539: ldc 43
    //   541: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   544: astore_3
    //   545: aload_3
    //   546: ifnull +27 -> 573
    //   549: aload_3
    //   550: invokevirtual 278	java/lang/String:length	()I
    //   553: ifle +20 -> 573
    //   556: aload_0
    //   557: getfield 234	net/flixster/android/model/Movie:mIntPropertyMap	Ljava/util/HashMap;
    //   560: ldc 43
    //   562: aload_3
    //   563: invokestatic 353	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   566: invokestatic 311	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   569: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   572: pop
    //   573: aload_0
    //   574: areturn
    //   575: aload 63
    //   577: aload 60
    //   579: iload 64
    //   581: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   584: ldc_w 333
    //   587: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   590: bipush 32
    //   592: sipush 160
    //   595: invokevirtual 836	java/lang/String:replace	(CC)Ljava/lang/String;
    //   598: invokevirtual 337	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   601: pop
    //   602: iload 64
    //   604: iconst_2
    //   605: if_icmpge +10 -> 615
    //   608: aload 63
    //   610: invokevirtual 328	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   613: astore 62
    //   615: iload 64
    //   617: iconst_1
    //   618: iadd
    //   619: iload 61
    //   621: if_icmpge +12 -> 633
    //   624: aload 63
    //   626: ldc_w 339
    //   629: invokevirtual 337	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   632: pop
    //   633: iinc 64 1
    //   636: goto -503 -> 133
    //   639: aload 51
    //   641: iload 53
    //   643: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   646: astore 54
    //   648: aload 54
    //   650: ldc_w 795
    //   653: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   656: astore 55
    //   658: aload 55
    //   660: ldc 37
    //   662: invokevirtual 274	java/lang/String:contentEquals	(Ljava/lang/CharSequence;)Z
    //   665: ifeq +39 -> 704
    //   668: aload_0
    //   669: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   672: ldc 37
    //   674: invokevirtual 245	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   677: ifne +845 -> 1522
    //   680: aload_0
    //   681: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   684: ldc 37
    //   686: aload 54
    //   688: ldc_w 556
    //   691: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   694: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   697: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   700: pop
    //   701: goto +821 -> 1522
    //   704: aload 55
    //   706: ldc 48
    //   708: invokevirtual 274	java/lang/String:contentEquals	(Ljava/lang/CharSequence;)Z
    //   711: ifeq +55 -> 766
    //   714: aload_0
    //   715: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   718: ldc 48
    //   720: invokevirtual 245	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   723: ifne +799 -> 1522
    //   726: aload_0
    //   727: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   730: ldc 48
    //   732: aload 54
    //   734: ldc_w 556
    //   737: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   740: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   743: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   746: pop
    //   747: goto +775 -> 1522
    //   750: astore 50
    //   752: ldc_w 280
    //   755: ldc_w 797
    //   758: aload 50
    //   760: invokestatic 288	com/flixster/android/utils/Logger:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   763: goto -536 -> 227
    //   766: aload 55
    //   768: ldc 82
    //   770: invokevirtual 274	java/lang/String:contentEquals	(Ljava/lang/CharSequence;)Z
    //   773: ifeq +39 -> 812
    //   776: aload_0
    //   777: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   780: ldc 82
    //   782: invokevirtual 245	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   785: ifne +737 -> 1522
    //   788: aload_0
    //   789: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   792: ldc 82
    //   794: aload 54
    //   796: ldc_w 556
    //   799: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   802: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   805: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   808: pop
    //   809: goto +713 -> 1522
    //   812: aload 55
    //   814: ldc 57
    //   816: invokevirtual 274	java/lang/String:contentEquals	(Ljava/lang/CharSequence;)Z
    //   819: ifeq +703 -> 1522
    //   822: aload_0
    //   823: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   826: ldc 57
    //   828: invokevirtual 245	java/util/HashMap:containsKey	(Ljava/lang/Object;)Z
    //   831: ifne +691 -> 1522
    //   834: aload_0
    //   835: getfield 232	net/flixster/android/model/Movie:mPropertyMap	Ljava/util/HashMap;
    //   838: ldc 57
    //   840: aload 54
    //   842: ldc_w 556
    //   845: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   848: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   851: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   854: pop
    //   855: goto +667 -> 1522
    //   858: aload_0
    //   859: getfield 819	net/flixster/android/model/Movie:mPhotos	Ljava/util/ArrayList;
    //   862: invokevirtual 839	java/util/ArrayList:clear	()V
    //   865: goto -589 -> 276
    //   868: aload 44
    //   870: iload 45
    //   872: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   875: astore 47
    //   877: new 841	net/flixster/android/model/Photo
    //   880: dup
    //   881: invokespecial 842	net/flixster/android/model/Photo:<init>	()V
    //   884: astore 48
    //   886: aload 48
    //   888: aload 47
    //   890: ldc_w 556
    //   893: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   896: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   899: putfield 845	net/flixster/android/model/Photo:thumbnailUrl	Ljava/lang/String;
    //   902: aload_0
    //   903: getfield 819	net/flixster/android/model/Movie:mPhotos	Ljava/util/ArrayList;
    //   906: aload 48
    //   908: invokevirtual 472	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   911: pop
    //   912: iinc 45 1
    //   915: goto -627 -> 288
    //   918: astore 43
    //   920: ldc_w 280
    //   923: ldc_w 847
    //   926: aload 43
    //   928: invokestatic 288	com/flixster/android/utils/Logger:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   931: goto -629 -> 302
    //   934: aload 31
    //   936: iload 32
    //   938: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   941: astore 35
    //   943: aload 35
    //   945: ldc_w 411
    //   948: invokevirtual 428	org/json/JSONObject:getLong	(Ljava/lang/String;)J
    //   951: lstore 36
    //   953: aload_0
    //   954: getfield 823	net/flixster/android/model/Movie:directorsHash	Ljava/util/HashMap;
    //   957: lload 36
    //   959: invokestatic 763	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   962: invokevirtual 685	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   965: checkcast 849	net/flixster/android/model/Actor
    //   968: astore 38
    //   970: aload 38
    //   972: ifnonnull +152 -> 1124
    //   975: iconst_1
    //   976: istore 39
    //   978: iload 39
    //   980: ifeq +12 -> 992
    //   983: new 849	net/flixster/android/model/Actor
    //   986: dup
    //   987: invokespecial 850	net/flixster/android/model/Actor:<init>	()V
    //   990: astore 38
    //   992: aload 38
    //   994: lload 36
    //   996: putfield 851	net/flixster/android/model/Actor:id	J
    //   999: aload 38
    //   1001: aload 35
    //   1003: ldc_w 333
    //   1006: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1009: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   1012: putfield 852	net/flixster/android/model/Actor:name	Ljava/lang/String;
    //   1015: aload 35
    //   1017: ldc_w 854
    //   1020: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1023: ifeq +65 -> 1088
    //   1026: aload 35
    //   1028: ldc_w 854
    //   1031: invokevirtual 301	org/json/JSONObject:getJSONObject	(Ljava/lang/String;)Lorg/json/JSONObject;
    //   1034: astore 42
    //   1036: aload 42
    //   1038: ldc 97
    //   1040: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1043: ifeq +18 -> 1061
    //   1046: aload 38
    //   1048: aload 42
    //   1050: ldc 97
    //   1052: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1055: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   1058: putfield 855	net/flixster/android/model/Actor:thumbnailUrl	Ljava/lang/String;
    //   1061: aload 42
    //   1063: ldc_w 857
    //   1066: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1069: ifeq +19 -> 1088
    //   1072: aload 38
    //   1074: aload 42
    //   1076: ldc_w 857
    //   1079: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1082: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   1085: putfield 860	net/flixster/android/model/Actor:largeUrl	Ljava/lang/String;
    //   1088: iload 39
    //   1090: ifeq +28 -> 1118
    //   1093: aload_0
    //   1094: getfield 821	net/flixster/android/model/Movie:mDirectors	Ljava/util/ArrayList;
    //   1097: aload 38
    //   1099: invokevirtual 472	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   1102: pop
    //   1103: aload_0
    //   1104: getfield 823	net/flixster/android/model/Movie:directorsHash	Ljava/util/HashMap;
    //   1107: lload 36
    //   1109: invokestatic 763	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   1112: aload 38
    //   1114: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   1117: pop
    //   1118: iinc 32 1
    //   1121: goto -756 -> 365
    //   1124: iconst_0
    //   1125: istore 39
    //   1127: goto -149 -> 978
    //   1130: astore 33
    //   1132: ldc_w 280
    //   1135: ldc_w 862
    //   1138: aload 33
    //   1140: invokestatic 288	com/flixster/android/utils/Logger:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   1143: goto -764 -> 379
    //   1146: aload 12
    //   1148: iload 14
    //   1150: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   1153: astore 16
    //   1155: aload 16
    //   1157: ldc_w 411
    //   1160: invokevirtual 428	org/json/JSONObject:getLong	(Ljava/lang/String;)J
    //   1163: lstore 17
    //   1165: aload_0
    //   1166: getfield 827	net/flixster/android/model/Movie:actorsHash	Ljava/util/HashMap;
    //   1169: lload 17
    //   1171: invokestatic 763	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   1174: invokevirtual 685	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   1177: checkcast 849	net/flixster/android/model/Actor
    //   1180: astore 19
    //   1182: aload 19
    //   1184: ifnonnull +350 -> 1534
    //   1187: iconst_1
    //   1188: istore 20
    //   1190: iload 20
    //   1192: ifeq +12 -> 1204
    //   1195: new 849	net/flixster/android/model/Actor
    //   1198: dup
    //   1199: invokespecial 850	net/flixster/android/model/Actor:<init>	()V
    //   1202: astore 19
    //   1204: aload 19
    //   1206: lload 17
    //   1208: putfield 851	net/flixster/android/model/Actor:id	J
    //   1211: aload 19
    //   1213: aload 16
    //   1215: ldc_w 333
    //   1218: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1221: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   1224: putfield 852	net/flixster/android/model/Actor:name	Ljava/lang/String;
    //   1227: aload 16
    //   1229: ldc_w 829
    //   1232: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1235: ifeq +57 -> 1292
    //   1238: new 323	java/lang/StringBuilder
    //   1241: dup
    //   1242: invokespecial 324	java/lang/StringBuilder:<init>	()V
    //   1245: astore 21
    //   1247: aload 16
    //   1249: ldc_w 829
    //   1252: invokevirtual 318	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   1255: astore 22
    //   1257: aload 22
    //   1259: invokevirtual 321	org/json/JSONArray:length	()I
    //   1262: ifle +30 -> 1292
    //   1265: aload 22
    //   1267: invokevirtual 321	org/json/JSONArray:length	()I
    //   1270: istore 26
    //   1272: iconst_0
    //   1273: istore 27
    //   1275: iload 27
    //   1277: iload 26
    //   1279: if_icmplt +119 -> 1398
    //   1282: aload 19
    //   1284: aload 21
    //   1286: invokevirtual 328	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1289: putfield 865	net/flixster/android/model/Actor:chars	Ljava/lang/String;
    //   1292: aload 16
    //   1294: ldc_w 854
    //   1297: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1300: ifeq +65 -> 1365
    //   1303: aload 16
    //   1305: ldc_w 854
    //   1308: invokevirtual 301	org/json/JSONObject:getJSONObject	(Ljava/lang/String;)Lorg/json/JSONObject;
    //   1311: astore 25
    //   1313: aload 25
    //   1315: ldc 97
    //   1317: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1320: ifeq +18 -> 1338
    //   1323: aload 19
    //   1325: aload 25
    //   1327: ldc 97
    //   1329: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1332: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   1335: putfield 855	net/flixster/android/model/Actor:thumbnailUrl	Ljava/lang/String;
    //   1338: aload 25
    //   1340: ldc_w 857
    //   1343: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1346: ifeq +19 -> 1365
    //   1349: aload 19
    //   1351: aload 25
    //   1353: ldc_w 857
    //   1356: invokevirtual 258	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1359: invokestatic 293	net/flixster/android/FlixUtils:copyString	(Ljava/lang/String;)Ljava/lang/String;
    //   1362: putfield 860	net/flixster/android/model/Actor:largeUrl	Ljava/lang/String;
    //   1365: iload 20
    //   1367: ifeq +161 -> 1528
    //   1370: aload_0
    //   1371: getfield 825	net/flixster/android/model/Movie:mActors	Ljava/util/ArrayList;
    //   1374: aload 19
    //   1376: invokevirtual 472	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   1379: pop
    //   1380: aload_0
    //   1381: getfield 827	net/flixster/android/model/Movie:actorsHash	Ljava/util/HashMap;
    //   1384: lload 17
    //   1386: invokestatic 763	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   1389: aload 19
    //   1391: invokevirtual 270	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   1394: pop
    //   1395: goto +133 -> 1528
    //   1398: aload 22
    //   1400: iload 27
    //   1402: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   1405: astore 28
    //   1407: aload 28
    //   1409: ldc_w 867
    //   1412: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1415: ifeq +31 -> 1446
    //   1418: iload 27
    //   1420: ifeq +12 -> 1432
    //   1423: aload 21
    //   1425: ldc_w 339
    //   1428: invokevirtual 337	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1431: pop
    //   1432: aload 21
    //   1434: aload 28
    //   1436: ldc_w 867
    //   1439: invokevirtual 870	org/json/JSONObject:get	(Ljava/lang/String;)Ljava/lang/Object;
    //   1442: invokevirtual 670	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   1445: pop
    //   1446: iinc 27 1
    //   1449: goto -174 -> 1275
    //   1452: astore 15
    //   1454: ldc_w 280
    //   1457: ldc_w 872
    //   1460: aload 15
    //   1462: invokestatic 288	com/flixster/android/utils/Logger:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   1465: goto -1007 -> 458
    //   1468: aload 5
    //   1470: iload 8
    //   1472: invokevirtual 331	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   1475: astore 9
    //   1477: aload 9
    //   1479: ldc_w 867
    //   1482: invokevirtual 251	org/json/JSONObject:has	(Ljava/lang/String;)Z
    //   1485: ifeq +31 -> 1516
    //   1488: iload 8
    //   1490: ifeq +12 -> 1502
    //   1493: aload 7
    //   1495: ldc_w 339
    //   1498: invokevirtual 337	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1501: pop
    //   1502: aload 7
    //   1504: aload 9
    //   1506: ldc_w 867
    //   1509: invokevirtual 870	org/json/JSONObject:get	(Ljava/lang/String;)Ljava/lang/Object;
    //   1512: invokevirtual 670	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   1515: pop
    //   1516: iinc 8 1
    //   1519: goto -1023 -> 496
    //   1522: iinc 53 1
    //   1525: goto -1305 -> 220
    //   1528: iinc 14 1
    //   1531: goto -1080 -> 451
    //   1534: iconst_0
    //   1535: istore 20
    //   1537: goto -347 -> 1190
    //
    // Exception table:
    //   from	to	target	type
    //   201	217	750	org/json/JSONException
    //   639	701	750	org/json/JSONException
    //   704	747	750	org/json/JSONException
    //   766	809	750	org/json/JSONException
    //   812	855	750	org/json/JSONException
    //   276	285	918	org/json/JSONException
    //   288	295	918	org/json/JSONException
    //   868	912	918	org/json/JSONException
    //   365	372	1130	org/json/JSONException
    //   934	970	1130	org/json/JSONException
    //   983	992	1130	org/json/JSONException
    //   992	1061	1130	org/json/JSONException
    //   1061	1088	1130	org/json/JSONException
    //   1093	1118	1130	org/json/JSONException
    //   1146	1182	1452	org/json/JSONException
    //   1195	1204	1452	org/json/JSONException
    //   1204	1272	1452	org/json/JSONException
    //   1282	1292	1452	org/json/JSONException
    //   1292	1338	1452	org/json/JSONException
    //   1338	1365	1452	org/json/JSONException
    //   1370	1395	1452	org/json/JSONException
    //   1398	1418	1452	org/json/JSONException
    //   1423	1432	1452	org/json/JSONException
    //   1432	1446	1452	org/json/JSONException
  }

  public void setDetailsApiParsed()
  {
    this.isMovieDetailsApiParsed = true;
  }

  public void setIntProperty(String paramString, Integer paramInteger)
  {
    this.mIntPropertyMap.put(paramString, paramInteger);
  }

  public void setProperty(String paramString1, String paramString2)
  {
    this.mPropertyMap.put(paramString1, paramString2);
  }

  public void setThumbOrderStamp(long paramLong)
  {
    this.mThumbOrderStamp = paramLong;
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     net.flixster.android.model.Movie
 * JD-Core Version:    0.6.2
 */
