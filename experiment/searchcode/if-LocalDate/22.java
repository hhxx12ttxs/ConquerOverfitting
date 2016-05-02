package com.flixster.android.activity.hc;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.flixster.android.analytics.Tracker;
import com.flixster.android.analytics.Trackers;
import com.flixster.android.utils.Logger;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import net.flixster.android.ActorGalleryPage;
import net.flixster.android.FlixsterApplication;
import net.flixster.android.ScrollGalleryPage;
import net.flixster.android.Starter;
import net.flixster.android.data.ActorDao;
import net.flixster.android.data.DaoException;
import net.flixster.android.model.Actor;
import net.flixster.android.model.ImageOrder;
import net.flixster.android.model.Movie;
import net.flixster.android.model.Photo;

@TargetApi(11)
public class ActorFragment extends LviFragment
  implements View.OnClickListener
{
  private static final int DIALOG_ACTOR = 2;
  private static final int DIALOG_NETWORK_FAIL = 1;
  public static final String KEY_ACTOR_ID = "ACTOR_ID";
  public static final String KEY_ACTOR_NAME = "ACTOR_NAME";
  private static final int MAX_MOVIES = 5;
  private static final int MAX_PHOTOS = 15;
  private static final int MAX_PHOTO_COUNT = 50;
  private Actor actor;
  private View.OnClickListener actorBiographyClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Intent localIntent = new Intent("TOP_ACTOR_BIOGRPHY", null, ActorFragment.this.getActivity(), ActorBiographyFragment.class);
      localIntent.putExtra("ACTOR_ID", ActorFragment.this.actor.id);
      localIntent.putExtra("ACTOR_NAME", ActorFragment.this.actor.name);
      ((Main)ActorFragment.this.getActivity()).startFragment(localIntent, ActorBiographyFragment.class, 0, ActorFragment.class);
    }
  };
  private final Handler actorThumbnailHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      if (ActorFragment.this.isRemoving());
      ImageView localImageView;
      Actor localActor;
      do
      {
        do
        {
          return;
          localImageView = (ImageView)paramAnonymousMessage.obj;
        }
        while (localImageView == null);
        localActor = (Actor)localImageView.getTag();
      }
      while (localActor == null);
      localImageView.setImageBitmap(localActor.bitmap);
      localImageView.invalidate();
    }
  };
  private LinearLayout filmographyLayout;
  private long mActorId;
  private LayoutInflater mLayoutInflater;
  private RelativeLayout moreFilmographyLayout;
  private View.OnClickListener moreFilmographyListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      View localView;
      if ((ActorFragment.this.filmographyLayout != null) && (ActorFragment.this.actor != null) && (ActorFragment.this.actor.movies != null) && (ActorFragment.this.actor.movies.size() > 5))
        localView = null;
      for (int i = 5; ; i++)
      {
        if (i >= ActorFragment.this.actor.movies.size())
        {
          if (ActorFragment.this.moreFilmographyLayout != null)
            ActorFragment.this.moreFilmographyLayout.setVisibility(8);
          return;
        }
        Movie localMovie = (Movie)ActorFragment.this.actor.movies.get(i);
        localView = ActorFragment.this.getMovieView(localMovie, localView);
        ActorFragment.this.filmographyLayout.addView(localView);
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
        localIntent = new Intent("DETAILS", null, ActorFragment.this.getActivity(), MovieDetailsFragment.class);
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
          ((Main)ActorFragment.this.getActivity()).startFragment(localIntent, MovieDetailsFragment.class, 0, ActorFragment.class);
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
      if (ActorFragment.this.isRemoving());
      ImageView localImageView;
      Movie localMovie;
      do
      {
        do
        {
          return;
          localImageView = (ImageView)paramAnonymousMessage.obj;
        }
        while (localImageView == null);
        localMovie = (Movie)localImageView.getTag();
      }
      while (localMovie == null);
      localImageView.setImageBitmap((Bitmap)localMovie.thumbnailSoftBitmap.get());
      localImageView.invalidate();
    }
  };
  private View.OnClickListener photoClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Photo localPhoto = (Photo)paramAnonymousView.getTag();
      if (localPhoto != null)
      {
        int i = ActorFragment.this.actor.photos.indexOf(localPhoto);
        if (i < 0)
          i = 0;
        Trackers.instance().track("/actor/photo", "Actor Photo Page for photo:" + localPhoto.thumbnailUrl);
        Intent localIntent = new Intent("PHOTO", null, ActorFragment.this.getActivity(), ScrollGalleryPage.class);
        localIntent.putExtra("PHOTO_INDEX", i);
        localIntent.putExtra("ACTOR_ID", ActorFragment.this.actor.id);
        localIntent.putExtra("ACTOR_NAME", ActorFragment.this.actor.name);
        ActorFragment.this.startActivity(localIntent);
      }
    }
  };
  private Handler photoHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      if (ActorFragment.this.isRemoving());
      ImageView localImageView;
      Photo localPhoto;
      do
      {
        do
        {
          return;
          localImageView = (ImageView)paramAnonymousMessage.obj;
        }
        while (localImageView == null);
        localPhoto = (Photo)localImageView.getTag();
      }
      while ((localPhoto == null) || (localPhoto.bitmap == null));
      localImageView.setImageBitmap(localPhoto.bitmap);
      localImageView.invalidate();
    }
  };
  private Resources resources;
  private View.OnClickListener trailerClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Movie localMovie = (Movie)paramAnonymousView.getTag();
      if ((localMovie != null) && (localMovie.hasTrailer()))
        Starter.launchTrailer(localMovie, ActorFragment.this.getActivity());
    }
  };
  private Handler updateHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      Main localMain = (Main)ActorFragment.this.getActivity();
      if ((localMain == null) || (ActorFragment.this.isRemoving()));
      do
      {
        return;
        Logger.d("FlxMain", "ActorPage.updateHandler");
        if (ActorFragment.this.actor != null)
        {
          ActorFragment.this.updatePage();
          return;
        }
      }
      while (localMain.isFinishing());
      localMain.showDialog(1);
    }
  };

  private void addScore(int paramInt, Drawable paramDrawable, TextView paramTextView)
  {
    paramDrawable.setBounds(0, 0, paramDrawable.getIntrinsicWidth(), paramDrawable.getIntrinsicHeight());
    paramTextView.setCompoundDrawables(paramDrawable, null, null, null);
    paramTextView.setText(paramInt + "%");
    paramTextView.setVisibility(0);
  }

  public static int getFlixFragId()
  {
    return 103;
  }

  private View getMovieView(Movie paramMovie, View paramView)
  {
    View localView = this.mLayoutInflater.inflate(2130903123, null);
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
    label231: int i;
    label315: int j;
    if (paramMovie.thumbnailSoftBitmap.get() != null)
    {
      localMovieViewHolder.thumbnailView.setImageBitmap((Bitmap)paramMovie.thumbnailSoftBitmap.get());
      if ((paramMovie.mActorPageChars == null) || (paramMovie.mActorPageChars.length() <= 0))
        break label578;
      localMovieViewHolder.charView.setText(paramMovie.mActorPageChars);
      localMovieViewHolder.charView.setVisibility(0);
      String str3 = paramMovie.getProperty("high");
      if ((str3 != null) && (str3.length() > 0))
      {
        localMovieViewHolder.trailerView.setTag(paramMovie);
        localMovieViewHolder.trailerView.setClickable(true);
        localMovieViewHolder.trailerView.setOnClickListener(this.trailerClickListener);
        localMovieViewHolder.trailerView.setVisibility(0);
      }
      if ((paramMovie.getTheaterReleaseDate() == null) || (FlixsterApplication.sToday.after(paramMovie.getTheaterReleaseDate())))
        break label600;
      i = 0;
      j = FlixsterApplication.getMovieRatingType();
      switch (j)
      {
      default:
        label344: if (FlixsterApplication.getPlatformUsername() == null)
          localMovieViewHolder.friendScore.setVisibility(8);
        break;
      case 0:
      case 1:
      }
    }
    while (true)
    {
      String str4 = paramMovie.getProperty("meta");
      if (str4 != null)
      {
        localMovieViewHolder.metaView.setText(str4);
        localMovieViewHolder.metaView.setVisibility(0);
      }
      Date localDate = paramMovie.getTheaterReleaseDate();
      if (localDate == null)
        localDate = paramMovie.getDvdReleaseDate();
      if (localDate != null)
      {
        String str5 = String.valueOf(1900 + localDate.getYear());
        String str6 = paramMovie.getProperty("title") + " (" + str5 + ")";
        localMovieViewHolder.titleView.setText(str6);
      }
      localMovieViewHolder.releaseView.setVisibility(8);
      localMovieViewHolder.movieLayout.setTag(paramMovie);
      localMovieViewHolder.movieLayout.setOnClickListener(this.movieClickListener);
      return localView;
      String str2 = paramMovie.getProperty("thumbnail");
      if ((str2 == null) || (str2.length() <= 0))
        break;
      localMovieViewHolder.thumbnailView.setTag(paramMovie);
      ImageOrder localImageOrder = new ImageOrder(0, paramMovie, str2, localMovieViewHolder.thumbnailView, this.movieThumbnailHandler);
      ((Main)getActivity()).orderImageFifo(localImageOrder);
      break;
      label578: localMovieViewHolder.charView.setText(null);
      localMovieViewHolder.charView.setVisibility(8);
      break label231;
      label600: i = 1;
      break label315;
      if (!paramMovie.checkIntProperty("popcornScore"))
        break label344;
      int i1 = paramMovie.getIntProperty("popcornScore").intValue();
      if (i1 <= 0)
        break label344;
      int i2;
      label643: int i3;
      if (i1 < 60)
      {
        i2 = 1;
        if (i != 0)
          break label682;
        i3 = 2130837750;
      }
      while (true)
      {
        addScore(i1, this.resources.getDrawable(i3), localMovieViewHolder.scoreView);
        break;
        i2 = 0;
        break label643;
        label682: if (i2 != 0)
          i3 = 2130837743;
        else
          i3 = 2130837738;
      }
      if (!paramMovie.checkIntProperty("rottenTomatoes"))
        break label344;
      int k = paramMovie.getIntProperty("rottenTomatoes").intValue();
      if (k <= 0)
        break label344;
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
            label1058: localStringBuilder1.append(")");
          }
        }
        while (true)
        {
          localMovieViewHolder.friendScore.setText(localStringBuilder1.toString());
          localMovieViewHolder.friendScore.setVisibility(0);
          break;
          localStringBuilder1.append(this.resources.getString(2131493005));
          break label1058;
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

  public static ActorFragment newInstance(Bundle paramBundle)
  {
    ActorFragment localActorFragment = new ActorFragment();
    localActorFragment.setArguments(paramBundle);
    return localActorFragment;
  }

  private void scheduleUpdatePageTask()
  {
    TimerTask local11 = new TimerTask()
    {
      public void run()
      {
        if (((Main)ActorFragment.this.getActivity() == null) || (ActorFragment.this.isRemoving()))
          return;
        if (ActorFragment.this.actor == null)
          Logger.d("FlxMain", "ActorPage.scheduleUpdatePageTask.run actorId:" + ActorFragment.this.mActorId);
        try
        {
          ActorFragment.this.actor = ActorDao.getActor(ActorFragment.this.mActorId);
          ActorFragment.this.updateHandler.sendEmptyMessage(0);
          return;
        }
        catch (DaoException localDaoException)
        {
          while (true)
          {
            Logger.e("FlxMain", "ActorPage.scheduleUpdatePageTask DaoException", localDaoException);
            ActorFragment.this.actor = null;
          }
        }
      }
    };
    if (((Main)getActivity()).mPageTimer != null)
      ((Main)getActivity()).mPageTimer.schedule(local11, 100L);
  }

  private void updatePage()
  {
    LinearLayout localLinearLayout1;
    ImageView localImageView1;
    TextView localTextView1;
    label137: TextView localTextView2;
    label209: TextView localTextView3;
    TextView localTextView4;
    label304: TextView localTextView5;
    TextView localTextView6;
    LinearLayout localLinearLayout2;
    ArrayList localArrayList;
    int k;
    label491: View localView;
    if (this.actor != null)
    {
      this.resources = getResources();
      localLinearLayout1 = (LinearLayout)getView().findViewById(2131165224);
      localImageView1 = (ImageView)localLinearLayout1.findViewById(2131165227);
      if (this.actor.bitmap == null)
        break label713;
      localImageView1.setImageBitmap(this.actor.bitmap);
      ((TextView)localLinearLayout1.findViewById(2131165228)).setText(this.actor.name);
      localTextView1 = (TextView)localLinearLayout1.findViewById(2131165229);
      if (this.actor.birthDay == null)
        break label785;
      localTextView1.setText("Birthday: " + this.actor.birthDay);
      localTextView1.setVisibility(0);
      localTextView2 = (TextView)localLinearLayout1.findViewById(2131165230);
      if ((this.actor.birthplace == null) || ("".equals(this.actor.birthplace)))
        break label795;
      localTextView2.setText("Birthplace: " + this.actor.birthplace);
      localTextView2.setVisibility(0);
      localTextView3 = (TextView)localLinearLayout1.findViewById(2131165231);
      localTextView4 = (TextView)localLinearLayout1.findViewById(2131165232);
      if ((this.actor.biography == null) || ("".equals(this.actor.biography)))
        break label805;
      localTextView3.setVisibility(0);
      localTextView4.setVisibility(0);
      localTextView4.setText(this.actor.biography);
      localTextView4.setFocusable(true);
      localTextView4.setClickable(true);
      localTextView4.setOnClickListener(this.actorBiographyClickListener);
      localTextView5 = (TextView)localLinearLayout1.findViewById(2131165233);
      localTextView6 = (TextView)localLinearLayout1.findViewById(2131165235);
      localLinearLayout2 = (LinearLayout)localLinearLayout1.findViewById(2131165234);
      localArrayList = this.actor.photos;
      if ((localArrayList == null) || (localArrayList.isEmpty()))
        break label1018;
      localLinearLayout2.removeAllViews();
      k = 0;
      int m = localArrayList.size();
      if ((k < m) && (k < 15))
        break label822;
      localTextView5.setVisibility(0);
      localLinearLayout2.setVisibility(0);
      StringBuilder localStringBuilder2 = new StringBuilder(getResources().getString(2131492928));
      int n = this.actor.photoCount;
      if (n > 50)
        n = 50;
      localStringBuilder2.append(" (").append(n).append(")");
      localTextView6.setText(localStringBuilder2.toString());
      localTextView6.setVisibility(8);
      localTextView6.setFocusable(true);
      localTextView6.setOnClickListener(this);
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
      if ((i >= j) || (i >= 5))
      {
        if (this.actor.movies.size() > 5)
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
        trackPage();
        return;
        label713: String str = this.actor.largeUrl;
        if (str != null)
        {
          localImageView1.setImageResource(2130837841);
          localImageView1.setTag(this.actor);
          ImageOrder localImageOrder2 = new ImageOrder(4, this.actor, str, localImageView1, this.actorThumbnailHandler);
          ((Main)getActivity()).orderImage(localImageOrder2);
          break;
        }
        localImageView1.setImageResource(2130837978);
        break;
        label785: localTextView1.setVisibility(8);
        break label137;
        label795: localTextView2.setVisibility(8);
        break label209;
        label805: localTextView3.setVisibility(8);
        localTextView4.setVisibility(8);
        break label304;
        label822: Photo localPhoto = (Photo)localArrayList.get(k);
        ImageView localImageView2 = new ImageView(getActivity());
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
            ImageOrder localImageOrder1 = new ImageOrder(3, localPhoto, localPhoto.thumbnailUrl, localImageView2, this.photoHandler);
            ((Main)getActivity()).orderImageFifo(localImageOrder1);
          }
        }
        label1018: localTextView5.setVisibility(8);
        localLinearLayout2.setVisibility(8);
        localTextView6.setVisibility(8);
        break label491;
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
    Intent localIntent = new Intent("PHOTOS", null, getActivity(), ActorGalleryPage.class);
    localIntent.putExtra("ACTOR_ID", this.actor.id);
    localIntent.putExtra("ACTOR_NAME", this.actor.name);
    startActivity(localIntent);
  }

  public Dialog onCreateDialog(int paramInt)
  {
    switch (paramInt)
    {
    default:
      ProgressDialog localProgressDialog2 = new ProgressDialog(getActivity());
      localProgressDialog2.setMessage(getResources().getString(2131493173));
      localProgressDialog2.setIndeterminate(true);
      localProgressDialog2.setCancelable(true);
      localProgressDialog2.setCanceledOnTouchOutside(true);
      return localProgressDialog2;
    case 2:
      ProgressDialog localProgressDialog1 = new ProgressDialog(getActivity());
      localProgressDialog1.setMessage(getResources().getString(2131493173));
      localProgressDialog1.setIndeterminate(true);
      localProgressDialog1.setCancelable(true);
      localProgressDialog1.setCanceledOnTouchOutside(true);
      return localProgressDialog1;
    case 1:
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getActivity());
    localBuilder.setMessage("The network connection failed. Press Retry to make another attempt.");
    localBuilder.setTitle("Network Error");
    localBuilder.setCancelable(false);
    localBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        ActorFragment.this.scheduleUpdatePageTask();
      }
    });
    localBuilder.setNegativeButton(getResources().getString(2131492938), null);
    return localBuilder.create();
  }

  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
  {
    this.mLayoutInflater = paramLayoutInflater;
    View localView = this.mLayoutInflater.inflate(2130903064, null);
    if (this.mActorId == 0L)
    {
      Bundle localBundle = getArguments();
      this.mActorId = localBundle.getLong("ACTOR_ID");
      if (localBundle.getString("ACTOR_NAME") != null)
      {
        String str = localBundle.getString("ACTOR_NAME");
        ((TextView)localView.findViewById(2131165228)).setText(str);
      }
    }
    return localView;
  }

  public void onResume()
  {
    super.onResume();
    scheduleUpdatePageTask();
  }

  public void trackPage()
  {
    Trackers.instance().track("/actor", "Actor Actor Page for actor:" + this.actor.name);
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
 * Qualified Name:     com.flixster.android.activity.hc.ActorFragment
 * JD-Core Version:    0.6.2
 */
