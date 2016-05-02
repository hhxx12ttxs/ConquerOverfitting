package net.flixster.android;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.flixster.android.activity.decorator.TopLevelDecorator;
import com.flixster.android.analytics.Tracker;
import com.flixster.android.analytics.Trackers;
import com.flixster.android.utils.Logger;
import com.flixster.android.view.SynopsisView;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import net.flixster.android.ads.AdView;
import net.flixster.android.ads.model.DfpAd.DfpCustomTarget;
import net.flixster.android.data.ActorDao;
import net.flixster.android.data.DaoException;
import net.flixster.android.model.Actor;
import net.flixster.android.model.ImageOrder;
import net.flixster.android.model.Movie;
import net.flixster.android.model.Photo;

public class ActorPage extends FlixsterActivity
  implements View.OnClickListener
{
  private static final int DIALOG_NETWORK_FAIL = 1;
  protected static final int HIDE_THROBBER = 0;
  public static final String KEY_ACTOR_ID = "ACTOR_ID";
  public static final String KEY_ACTOR_NAME = "ACTOR_NAME";
  private static final int MAX_MOVIES = 3;
  private static final int MAX_PHOTOS = 15;
  private static final int MAX_PHOTO_COUNT = 50;
  protected static final int SHOW_THROBBER = 1;
  private Actor actor;
  private long actorId;
  private final Handler actorThumbnailHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      ImageView localImageView = (ImageView)paramAnonymousMessage.obj;
      if (localImageView != null)
      {
        Actor localActor = (Actor)localImageView.getTag();
        if (localActor != null)
        {
          localImageView.setImageBitmap(localActor.bitmap);
          localImageView.invalidate();
        }
      }
    }
  };
  private LinearLayout filmographyLayout;
  private LayoutInflater inflater;
  private AdView mAdView;
  private RelativeLayout moreFilmographyLayout;
  private View.OnClickListener moreFilmographyListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      View localView;
      if ((ActorPage.this.filmographyLayout != null) && (ActorPage.this.actor != null) && (ActorPage.this.actor.movies != null) && (ActorPage.this.actor.movies.size() > 3))
        localView = null;
      for (int i = 3; ; i++)
      {
        if (i >= ActorPage.this.actor.movies.size())
        {
          if (ActorPage.this.moreFilmographyLayout != null)
            ActorPage.this.moreFilmographyLayout.setVisibility(8);
          return;
        }
        Movie localMovie = (Movie)ActorPage.this.actor.movies.get(i);
        localView = ActorPage.this.getMovieView(localMovie, localView);
        ActorPage.this.filmographyLayout.addView(localView);
      }
    }
  };
  private View.OnClickListener movieClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Movie localMovie = (Movie)paramAnonymousView.getTag();
      Intent localIntent;
      String[] arrayOfString;
      int i;
      if (localMovie != null)
      {
        localIntent = new Intent("DETAILS", null, ActorPage.this.getApplicationContext(), MovieDetails.class);
        localIntent.putExtra("net.flixster.android.EXTRA_MOVIE_ID", localMovie.getId());
        localIntent.putExtra("MOVIE_IN_THEATER_FLAG", localMovie.isMIT);
        localIntent.putExtra("MOVIE_THUMBNAIL", (Parcelable)localMovie.thumbnailSoftBitmap.get());
        arrayOfString = new String[] { "title", "high", "mpaa", "MOVIE_ACTORS", "thumbnail", "runningTime", "directors", "theaterReleaseDate", "genre", "status", "MOVIE_ACTORS_SHORT", "meta" };
        i = arrayOfString.length;
      }
      for (int j = 0; ; j++)
      {
        if (j >= i)
        {
          localIntent.putExtra("boxOffice", localMovie.getIntProperty("boxOffice"));
          if (localMovie.checkIntProperty("popcornScore"))
            localIntent.putExtra("popcornScore", localMovie.getIntProperty("popcornScore"));
          if (localMovie.checkIntProperty("rottenTomatoes"))
            localIntent.putExtra("rottenTomatoes", localMovie.getIntProperty("rottenTomatoes"));
          ActorPage.this.startActivity(localIntent);
          return;
        }
        String str = arrayOfString[j];
        localIntent.putExtra(str, localMovie.getProperty(str));
      }
    }
  };
  private final Handler movieThumbnailHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      ImageView localImageView = (ImageView)paramAnonymousMessage.obj;
      if (localImageView != null)
      {
        Movie localMovie = (Movie)localImageView.getTag();
        if (localMovie != null)
        {
          localImageView.setImageBitmap((Bitmap)localMovie.thumbnailSoftBitmap.get());
          localImageView.invalidate();
        }
      }
    }
  };
  private View.OnClickListener photoClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Photo localPhoto = (Photo)paramAnonymousView.getTag();
      if (localPhoto != null)
      {
        int i = ActorPage.this.actor.photos.indexOf(localPhoto);
        if (i < 0)
          i = 0;
        Trackers.instance().track("/actor/photo", "Actor Photo Page for photo:" + localPhoto.thumbnailUrl);
        Intent localIntent = new Intent("PHOTO", null, ActorPage.this.getApplicationContext(), ScrollGalleryPage.class);
        localIntent.putExtra("PHOTO_INDEX", i);
        localIntent.putExtra("ACTOR_ID", ActorPage.this.actor.id);
        localIntent.putExtra("ACTOR_NAME", ActorPage.this.actor.name);
        ActorPage.this.startActivity(localIntent);
      }
    }
  };
  private Handler photoHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      ImageView localImageView = (ImageView)paramAnonymousMessage.obj;
      if (localImageView != null)
      {
        Photo localPhoto = (Photo)localImageView.getTag();
        if ((localPhoto != null) && (localPhoto.bitmap != null))
        {
          localImageView.setImageBitmap(localPhoto.bitmap);
          localImageView.invalidate();
        }
      }
    }
  };
  private Resources resources;
  private View throbber;
  protected final Handler throbberHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      switch (paramAnonymousMessage.what)
      {
      default:
        ActorPage.this.hideLoading();
        return;
      case 1:
      }
      ActorPage.this.showLoading();
    }
  };
  private Timer timer;
  private View.OnClickListener trailerClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Movie localMovie = (Movie)paramAnonymousView.getTag();
      if (localMovie != null)
        Starter.launchTrailer(localMovie, ActorPage.this);
    }
  };
  private Handler updateHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      if ((ActorPage.this.topLevelDecorator.isPausing()) || (ActorPage.this.isFinishing()));
      do
      {
        return;
        Logger.d("FlxMain", "ActorPage.updateHandler");
        ActorPage.this.throbberHandler.sendEmptyMessage(0);
        if (ActorPage.this.actor != null)
        {
          ActorPage.this.updatePage();
          return;
        }
      }
      while (ActorPage.this.isFinishing());
      ActorPage.this.showDialog(1);
    }
  };

  private void addScore(int paramInt, Drawable paramDrawable, TextView paramTextView)
  {
    paramDrawable.setBounds(0, 0, paramDrawable.getIntrinsicWidth(), paramDrawable.getIntrinsicHeight());
    paramTextView.setCompoundDrawables(paramDrawable, null, null, null);
    paramTextView.setText(paramInt + "%");
    paramTextView.setVisibility(0);
  }

  private View getMovieView(Movie paramMovie, View paramView)
  {
    View localView = this.inflater.inflate(2130903123, null);
    MovieViewHolder localMovieViewHolder = new MovieViewHolder(null);
    localMovieViewHolder.movieLayout = ((RelativeLayout)localView.findViewById(2131165485));
    localMovieViewHolder.titleView = ((TextView)localView.findViewById(2131165448));
    localMovieViewHolder.thumbnailView = ((ImageView)localView.findViewById(2131165486));
    localMovieViewHolder.trailerView = ((ImageView)localView.findViewById(2131165487));
    localMovieViewHolder.scoreView = ((TextView)localView.findViewById(2131165490));
    localMovieViewHolder.friendScore = ((TextView)localView.findViewById(2131165491));
    localMovieViewHolder.metaView = ((TextView)localView.findViewById(2131165452));
    localMovieViewHolder.charView = ((TextView)localView.findViewById(2131165489));
    localMovieViewHolder.releaseView = ((TextView)localView.findViewById(2131165453));
    String str1 = paramMovie.getProperty("title");
    localMovieViewHolder.titleView.setText(str1);
    label234: int i;
    label303: int j;
    if (paramMovie.thumbnailSoftBitmap.get() != null)
    {
      localMovieViewHolder.thumbnailView.setImageBitmap((Bitmap)paramMovie.thumbnailSoftBitmap.get());
      if ((paramMovie.mActorPageChars == null) || (paramMovie.mActorPageChars.length() <= 0))
        break label557;
      localMovieViewHolder.charView.setText(paramMovie.mActorPageChars);
      localMovieViewHolder.charView.setVisibility(0);
      if (paramMovie.hasTrailer())
      {
        localMovieViewHolder.trailerView.setTag(paramMovie);
        localMovieViewHolder.trailerView.setClickable(true);
        localMovieViewHolder.trailerView.setOnClickListener(this.trailerClickListener);
        localMovieViewHolder.trailerView.setVisibility(0);
      }
      if ((paramMovie.getTheaterReleaseDate() == null) || (FlixsterApplication.sToday.after(paramMovie.getTheaterReleaseDate())))
        break label579;
      i = 0;
      j = FlixsterApplication.getMovieRatingType();
      switch (j)
      {
      default:
        label332: if (FlixsterApplication.getPlatformUsername() == null)
          localMovieViewHolder.friendScore.setVisibility(8);
        break;
      case 0:
      case 1:
      }
    }
    while (true)
    {
      String str3 = paramMovie.getProperty("meta");
      if (str3 != null)
      {
        localMovieViewHolder.metaView.setText(str3);
        localMovieViewHolder.metaView.setVisibility(0);
      }
      Date localDate = paramMovie.getTheaterReleaseDate();
      if (localDate == null)
        localDate = paramMovie.getDvdReleaseDate();
      if (localDate != null)
      {
        String str4 = String.valueOf(1900 + localDate.getYear());
        String str5 = paramMovie.getProperty("title") + " (" + str4 + ")";
        localMovieViewHolder.titleView.setText(str5);
      }
      localMovieViewHolder.releaseView.setVisibility(8);
      localMovieViewHolder.movieLayout.setTag(paramMovie);
      localMovieViewHolder.movieLayout.setOnClickListener(this.movieClickListener);
      return localView;
      String str2 = paramMovie.getProperty("thumbnail");
      if ((str2 == null) || (str2.length() <= 0))
        break;
      localMovieViewHolder.thumbnailView.setTag(paramMovie);
      orderImageFifo(new ImageOrder(0, paramMovie, str2, localMovieViewHolder.thumbnailView, this.movieThumbnailHandler));
      break;
      label557: localMovieViewHolder.charView.setText(null);
      localMovieViewHolder.charView.setVisibility(8);
      break label234;
      label579: i = 1;
      break label303;
      if (!paramMovie.checkIntProperty("popcornScore"))
        break label332;
      int i1 = paramMovie.getIntProperty("popcornScore").intValue();
      if (i1 <= 0)
        break label332;
      int i2;
      label622: int i3;
      if (i1 < 60)
      {
        i2 = 1;
        if (i != 0)
          break label661;
        i3 = 2130837750;
      }
      while (true)
      {
        addScore(i1, this.resources.getDrawable(i3), localMovieViewHolder.scoreView);
        break;
        i2 = 0;
        break label622;
        label661: if (i2 != 0)
          i3 = 2130837743;
        else
          i3 = 2130837738;
      }
      if (!paramMovie.checkIntProperty("rottenTomatoes"))
        break label332;
      int k = paramMovie.getIntProperty("rottenTomatoes").intValue();
      if (k <= 0)
        break label332;
      if (k < 60);
      for (Drawable localDrawable1 = this.resources.getDrawable(2130837741); ; localDrawable1 = this.resources.getDrawable(2130837726))
      {
        TextView localTextView = localMovieViewHolder.scoreView;
        addScore(k, localDrawable1, localTextView);
        break;
      }
      if ((paramMovie.checkIntProperty("FRIENDS_RATED_COUNT")) && (paramMovie.getIntProperty("FRIENDS_RATED_COUNT").intValue() > 0))
      {
        Drawable localDrawable3 = this.resources.getDrawable(2130837739);
        localDrawable3.setBounds(0, 0, localDrawable3.getIntrinsicWidth(), localDrawable3.getIntrinsicHeight());
        localMovieViewHolder.friendScore.setCompoundDrawables(localDrawable3, null, null, null);
        int n = paramMovie.getIntProperty("FRIENDS_RATED_COUNT").intValue();
        StringBuilder localStringBuilder2 = new StringBuilder();
        localStringBuilder2.append(n).append(" ");
        if (n > 1)
          localStringBuilder2.append(this.resources.getString(2131493008));
        while (true)
        {
          localMovieViewHolder.friendScore.setText(localStringBuilder2.toString());
          localMovieViewHolder.friendScore.setVisibility(0);
          break;
          localStringBuilder2.append(this.resources.getString(2131493007));
        }
      }
      if ((paramMovie.checkIntProperty("FRIENDS_WTS_COUNT")) && (paramMovie.getIntProperty("FRIENDS_WTS_COUNT").intValue() > 0))
      {
        int m = paramMovie.getIntProperty("FRIENDS_WTS_COUNT").intValue();
        StringBuilder localStringBuilder1 = new StringBuilder();
        if ((i == 0) && (j == 0))
        {
          localMovieViewHolder.friendScore.setCompoundDrawables(null, null, null, null);
          localStringBuilder1.append("(").append(m).append(" ");
          if (m > 1)
          {
            localStringBuilder1.append(this.resources.getString(2131493006));
            label1037: localStringBuilder1.append(")");
          }
        }
        while (true)
        {
          localMovieViewHolder.friendScore.setText(localStringBuilder1.toString());
          localMovieViewHolder.friendScore.setVisibility(0);
          break;
          localStringBuilder1.append(this.resources.getString(2131493005));
          break label1037;
          Drawable localDrawable2 = this.resources.getDrawable(2130837751);
          localDrawable2.setBounds(0, 0, localDrawable2.getIntrinsicWidth(), localDrawable2.getIntrinsicHeight());
          localMovieViewHolder.friendScore.setCompoundDrawables(localDrawable2, null, null, null);
          localStringBuilder1.append(m).append(" ");
          if (m > 1)
            localStringBuilder1.append(this.resources.getString(2131493012));
          else
            localStringBuilder1.append(this.resources.getString(2131493011));
        }
      }
      localMovieViewHolder.friendScore.setVisibility(8);
    }
  }

  private void hideLoading()
  {
    this.throbber.setVisibility(8);
  }

  private void scheduleUpdatePageTask()
  {
    this.throbberHandler.sendEmptyMessage(1);
    TimerTask local11 = new TimerTask()
    {
      public void run()
      {
        if (ActorPage.this.actor == null)
          Logger.d("FlxMain", "ActorPage.scheduleUpdatePageTask.run actorId:" + ActorPage.this.actorId);
        try
        {
          ActorPage.this.actor = ActorDao.getActor(ActorPage.this.actorId);
          if (ActorPage.this.shouldSkipBackgroundTask(this.val$currResumeCtr))
            return;
        }
        catch (DaoException localDaoException)
        {
          while (true)
          {
            Logger.e("FlxMain", "ActorPage.scheduleUpdatePageTask DaoException", localDaoException);
            ActorPage.this.actor = null;
          }
          ActorPage.this.updateHandler.sendEmptyMessage(0);
        }
      }
    };
    if (this.timer != null)
      this.timer.schedule(local11, 100L);
  }

  private void showLoading()
  {
    this.throbber.setVisibility(0);
  }

  private void updatePage()
  {
    LinearLayout localLinearLayout1;
    ImageView localImageView1;
    TextView localTextView1;
    label142: TextView localTextView2;
    label214: TextView localTextView3;
    SynopsisView localSynopsisView;
    label292: TextView localTextView4;
    TextView localTextView5;
    LinearLayout localLinearLayout2;
    ArrayList localArrayList;
    int k;
    label475: View localView;
    if (this.actor != null)
    {
      this.inflater = LayoutInflater.from(this);
      this.resources = getResources();
      localLinearLayout1 = (LinearLayout)findViewById(2131165224);
      localImageView1 = (ImageView)localLinearLayout1.findViewById(2131165227);
      if (this.actor.bitmap == null)
        break label727;
      localImageView1.setImageBitmap(this.actor.bitmap);
      ((TextView)localLinearLayout1.findViewById(2131165228)).setText(this.actor.name);
      localTextView1 = (TextView)localLinearLayout1.findViewById(2131165229);
      if (this.actor.birthDay == null)
        break label789;
      localTextView1.setText("Birthday: " + this.actor.birthDay);
      localTextView1.setVisibility(0);
      localTextView2 = (TextView)localLinearLayout1.findViewById(2131165230);
      if ((this.actor.birthplace == null) || ("".equals(this.actor.birthplace)))
        break label799;
      localTextView2.setText("Birthplace: " + this.actor.birthplace);
      localTextView2.setVisibility(0);
      localTextView3 = (TextView)localLinearLayout1.findViewById(2131165231);
      localSynopsisView = (SynopsisView)localLinearLayout1.findViewById(2131165232);
      if ((this.actor.biography == null) || ("".equals(this.actor.biography)))
        break label809;
      localTextView3.setVisibility(0);
      localSynopsisView.setVisibility(0);
      localSynopsisView.load(this.actor.biography, 170, false);
      localTextView4 = (TextView)localLinearLayout1.findViewById(2131165233);
      localTextView5 = (TextView)localLinearLayout1.findViewById(2131165235);
      localLinearLayout2 = (LinearLayout)localLinearLayout1.findViewById(2131165234);
      localArrayList = this.actor.photos;
      if ((localArrayList == null) || (localArrayList.isEmpty()))
        break label1013;
      localLinearLayout2.removeAllViews();
      k = 0;
      int m = localArrayList.size();
      if ((k < m) && (k < 15))
        break label826;
      localTextView4.setVisibility(0);
      localLinearLayout2.setVisibility(0);
      StringBuilder localStringBuilder2 = new StringBuilder(getResourceString(2131492928));
      int n = this.actor.photoCount;
      if (n > 50)
        n = 50;
      localStringBuilder2.append(" (").append(n).append(")");
      localTextView5.setText(localStringBuilder2.toString());
      localTextView5.setVisibility(0);
      localTextView5.setFocusable(true);
      localTextView5.setOnClickListener(this);
      this.filmographyLayout = ((LinearLayout)localLinearLayout1.findViewById(2131165237));
      if ((this.actor.movies != null) && (!this.actor.movies.isEmpty()))
      {
        this.filmographyLayout.removeAllViews();
        ((TextView)localLinearLayout1.findViewById(2131165236)).setVisibility(0);
        localView = null;
      }
    }
    for (int i = 0; ; i++)
    {
      int j = this.actor.movies.size();
      if ((i >= j) || (i >= 3))
      {
        if (this.actor.movies.size() > 3)
        {
          this.moreFilmographyLayout = ((RelativeLayout)localLinearLayout1.findViewById(2131165238));
          this.moreFilmographyLayout.setVisibility(0);
          StringBuilder localStringBuilder1 = new StringBuilder();
          localStringBuilder1.append(this.actor.movies.size()).append(" ").append(this.resources.getString(2131492992));
          ((TextView)this.moreFilmographyLayout.findViewById(2131165240)).setText(localStringBuilder1.toString());
          this.moreFilmographyLayout.setFocusable(true);
          this.moreFilmographyLayout.setClickable(true);
          this.moreFilmographyLayout.setOnClickListener(this.moreFilmographyListener);
        }
        Trackers.instance().track("/actor", "Actor Actor Page for actor:" + this.actor.id);
        return;
        label727: String str = this.actor.largeUrl;
        if (str != null)
        {
          localImageView1.setImageResource(2130837841);
          localImageView1.setTag(this.actor);
          orderImage(new ImageOrder(4, this.actor, str, localImageView1, this.actorThumbnailHandler));
          break;
        }
        localImageView1.setImageResource(2130837978);
        break;
        label789: localTextView1.setVisibility(8);
        break label142;
        label799: localTextView2.setVisibility(8);
        break label214;
        label809: localTextView3.setVisibility(8);
        localSynopsisView.setVisibility(8);
        break label292;
        label826: Photo localPhoto = (Photo)localArrayList.get(k);
        ImageView localImageView2 = new ImageView(this);
        localImageView2.setLayoutParams(new ViewGroup.LayoutParams(getResources().getDimensionPixelOffset(2131361847), getResources().getDimensionPixelOffset(2131361847)));
        localImageView2.setPadding(0, 0, getResources().getDimensionPixelOffset(2131361835), 0);
        localImageView2.setTag(localPhoto);
        if (localPhoto.bitmap != null)
          localImageView2.setImageBitmap(localPhoto.bitmap);
        while (true)
        {
          localImageView2.setFocusable(true);
          localImageView2.setOnClickListener(this.photoClickListener);
          localLinearLayout2.addView(localImageView2);
          k++;
          break;
          localImageView2.setImageResource(2130837841);
          if ((localPhoto.thumbnailUrl != null) && (localPhoto.thumbnailUrl.startsWith("http://")))
          {
            ImageOrder localImageOrder = new ImageOrder(3, localPhoto, localPhoto.thumbnailUrl, localImageView2, this.photoHandler);
            orderImageFifo(localImageOrder);
          }
        }
        label1013: localTextView4.setVisibility(8);
        localLinearLayout2.setVisibility(8);
        localTextView5.setVisibility(8);
        break label475;
      }
      localView = getMovieView((Movie)this.actor.movies.get(i), localView);
      this.filmographyLayout.addView(localView);
    }
  }

  public void onClick(View paramView)
  {
    switch (paramView.getId())
    {
    default:
    case 2131165235:
    }
    do
      return;
    while (this.actor == null);
    Intent localIntent = new Intent("PHOTOS", null, this, ActorGalleryPage.class);
    localIntent.putExtra("ACTOR_ID", this.actor.id);
    localIntent.putExtra("ACTOR_NAME", this.actor.name);
    startActivity(localIntent);
  }

  public void onCreate(Bundle paramBundle)
  {
    Logger.d("FlxMain", "ActorPage.onCreate");
    super.onCreate(paramBundle);
    setContentView(2130903064);
    createActionBar();
    this.throbber = findViewById(2131165241);
    this.mAdView = ((AdView)findViewById(2131165226));
    updatePage();
  }

  public Dialog onCreateDialog(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return super.onCreateDialog(paramInt);
    case 1:
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
    localBuilder.setMessage("The network connection failed. Press Retry to make another attempt.");
    localBuilder.setTitle("Network Error");
    localBuilder.setCancelable(false);
    localBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        ActorPage.this.scheduleUpdatePageTask();
      }
    });
    localBuilder.setNegativeButton(getResources().getString(2131492938), null);
    return localBuilder.create();
  }

  public void onDestroy()
  {
    Logger.d("FlxMain", "ActorPage.onDestroy");
    super.onDestroy();
    if (this.timer != null)
    {
      this.timer.cancel();
      this.timer.purge();
    }
    this.timer = null;
  }

  public void onResume()
  {
    Logger.d("FlxMain", "ActorPage.onResume");
    super.onResume();
    if (this.actorId == 0L)
    {
      Bundle localBundle = getIntent().getExtras();
      this.actorId = localBundle.getLong("ACTOR_ID");
      if (localBundle.getString("ACTOR_NAME") != null)
        setActionBarTitle(localBundle.getString("ACTOR_NAME"));
      DfpAd.DfpCustomTarget localDfpCustomTarget = new DfpAd.DfpCustomTarget(this.actorId);
      this.mAdView.dfpCustomTarget = localDfpCustomTarget;
    }
    if (this.timer == null)
      this.timer = new Timer();
    scheduleUpdatePageTask();
    this.mAdView.refreshAds();
  }

  private static final class MovieViewHolder
  {
    TextView charView;
    TextView friendScore;
    TextView metaView;
    RelativeLayout movieLayout;
    TextView releaseView;
    TextView scoreView;
    ImageView thumbnailView;
    TextView titleView;
    ImageView trailerView;
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     net.flixster.android.ActorPage
 * JD-Core Version:    0.6.2
 */
