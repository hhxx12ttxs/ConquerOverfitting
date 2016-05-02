package net.flixster.android;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.flixster.android.activity.decorator.TopLevelDecorator;
import com.flixster.android.utils.ListHelper;
import com.flixster.android.utils.Logger;
import com.flixster.android.view.SubNavBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.flixster.android.ads.AdView;
import net.flixster.android.data.DaoException;
import net.flixster.android.data.MovieDao;
import net.flixster.android.lvi.LviAd;
import net.flixster.android.lvi.LviFooter;
import net.flixster.android.lvi.LviMovie;
import net.flixster.android.lvi.LviSubHeader;
import net.flixster.android.model.Movie;
import net.flixster.android.model.MoviePopcornComparator;
import net.flixster.android.model.MovieRottenScoreComparator;
import net.flixster.android.model.MovieTitleComparator;

public class BoxOfficePage extends LviActivityCopy
{
  public static final String LISTSTATE_SORT = "LISTSTATE_SORT";
  public static final int SORT_ALPHA = 3;
  public static final int SORT_POPULAR = 1;
  public static final int SORT_RATING = 2;
  public static final int SORT_START;
  private final ArrayList<Movie> mBoxOfficeFeatured = new ArrayList();
  private final ArrayList<Movie> mBoxOfficeOthers = new ArrayList();
  private final ArrayList<Movie> mBoxOfficeOtw = new ArrayList();
  private final ArrayList<Movie> mBoxOfficeTbo = new ArrayList();
  private View.OnClickListener mNavListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      BoxOfficePage.this.mNavSelect = paramAnonymousView.getId();
      TopLevelDecorator.incrementResumeCtr();
      BoxOfficePage.this.ScheduleLoadItemsTask(BoxOfficePage.this.mNavSelect, BoxOfficePage.this.mSortOption, 100L);
    }
  };
  private int mNavSelect;
  private int mSortOption;
  private final ArrayList<Movie> mUpcoming = new ArrayList();
  private final ArrayList<Movie> mUpcomingFeatured = new ArrayList();
  private SubNavBar navBar;

  private void ScheduleLoadItemsTask(final int paramInt1, final int paramInt2, long paramLong)
  {
    try
    {
      this.throbberHandler.sendEmptyMessage(1);
      TimerTask local2 = new TimerTask()
      {
        public void run()
        {
          boolean bool1 = true;
          Logger.d("FlxMain", "BoxOfficePage.ScheduleLoadItemsTask.run");
          while (true)
          {
            try
            {
              if (paramInt1 != 2131165784)
                break;
              if (BoxOfficePage.this.mBoxOfficeOtw.isEmpty())
              {
                ArrayList localArrayList3 = BoxOfficePage.this.mBoxOfficeFeatured;
                ArrayList localArrayList4 = BoxOfficePage.this.mBoxOfficeOtw;
                ArrayList localArrayList5 = BoxOfficePage.this.mBoxOfficeTbo;
                ArrayList localArrayList6 = BoxOfficePage.this.mBoxOfficeOthers;
                if (BoxOfficePage.this.mRetryCount == 0)
                  MovieDao.fetchBoxOffice(localArrayList3, localArrayList4, localArrayList5, localArrayList6, bool1);
              }
              else
              {
                boolean bool2 = BoxOfficePage.this.shouldSkipBackgroundTask(this.val$currResumeCtr);
                if (!bool2)
                  continue;
                return;
              }
              bool1 = false;
              continue;
              switch (paramInt2)
              {
              default:
                BoxOfficePage.this.trackPage();
                BoxOfficePage.this.mUpdateHandler.sendEmptyMessage(0);
                BoxOfficePage.this.checkAndShowLaunchAd();
                return;
              case 1:
                label156: BoxOfficePage.this.mSortOption = 1;
                BoxOfficePage.this.setPopularLviList();
                continue;
              case 2:
              case 3:
              }
            }
            catch (DaoException localDaoException)
            {
              Logger.e("FlxMain", "BoxOfficePage.ScheduleLoadItemsTask.run DaoException", localDaoException);
              BoxOfficePage.this.retryLogic(localDaoException);
              return;
              BoxOfficePage.this.mSortOption = 2;
              BoxOfficePage.this.setRatingLviList();
              continue;
            }
            finally
            {
              BoxOfficePage.this.throbberHandler.sendEmptyMessage(0);
            }
            BoxOfficePage.this.mSortOption = 3;
            BoxOfficePage.this.setAlphaLviList();
          }
          ArrayList localArrayList1;
          ArrayList localArrayList2;
          if (BoxOfficePage.this.mUpcoming.isEmpty())
          {
            localArrayList1 = BoxOfficePage.this.mUpcomingFeatured;
            localArrayList2 = BoxOfficePage.this.mUpcoming;
            if (BoxOfficePage.this.mRetryCount != 0)
              break label370;
          }
          while (true)
          {
            MovieDao.fetchUpcoming(localArrayList1, localArrayList2, bool1);
            if (BoxOfficePage.this.shouldSkipBackgroundTask(this.val$currResumeCtr))
              break;
            BoxOfficePage.this.setUpcomingLviList();
            break label156;
            label370: bool1 = false;
          }
        }
      };
      if (this.mPageTimer != null)
        this.mPageTimer.schedule(local2, paramLong);
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  private void setAlphaLviList()
  {
    Logger.d("FlxMain", this.className + ".setAlphaLviList");
    ArrayList localArrayList = new ArrayList();
    localArrayList.addAll(this.mBoxOfficeFeatured);
    localArrayList.addAll(this.mBoxOfficeOtw);
    localArrayList.addAll(this.mBoxOfficeTbo);
    localArrayList.addAll(this.mBoxOfficeOthers);
    Collections.sort(localArrayList, new MovieTitleComparator());
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "MoviesTab";
    this.mDataHolder.add(this.lviAd);
    char c1 = '\000';
    Iterator localIterator = localArrayList.iterator();
    while (true)
    {
      if (!localIterator.hasNext())
      {
        LviFooter localLviFooter = new LviFooter();
        this.mDataHolder.add(localLviFooter);
        return;
      }
      Movie localMovie = (Movie)localIterator.next();
      char c2 = localMovie.getProperty("title").charAt(0);
      if (!Character.isLetter(c2))
        c2 = '#';
      if (c1 != c2)
      {
        LviSubHeader localLviSubHeader = new LviSubHeader();
        localLviSubHeader.mTitle = String.valueOf(c2);
        this.mDataHolder.add(localLviSubHeader);
        c1 = c2;
      }
      LviMovie localLviMovie = new LviMovie();
      localLviMovie.mMovie = localMovie;
      localLviMovie.mTrailerClick = getTrailerOnClickListener();
      this.mDataHolder.add(localLviMovie);
    }
  }

  private void setPopularLviList()
  {
    Logger.d("FlxMain", this.className + ".setPopularLviList");
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "MoviesTab";
    this.mDataHolder.add(this.lviAd);
    ArrayList localArrayList1 = new ArrayList();
    localArrayList1.add(ListHelper.clone(this.mBoxOfficeFeatured));
    localArrayList1.add(ListHelper.clone(this.mBoxOfficeOtw));
    localArrayList1.add(ListHelper.clone(this.mBoxOfficeTbo));
    localArrayList1.add(ListHelper.clone(this.mBoxOfficeOthers));
    String[] arrayOfString = new String[4];
    arrayOfString[0] = getResources().getString(2131493054);
    arrayOfString[1] = getResources().getString(2131493055);
    arrayOfString[2] = getResources().getString(2131493056);
    arrayOfString[3] = getResources().getString(2131493057);
    Iterator localIterator;
    for (int i = 0; ; i++)
    {
      if (i >= 4)
      {
        LviFooter localLviFooter = new LviFooter();
        this.mDataHolder.add(localLviFooter);
        return;
      }
      ArrayList localArrayList2 = (ArrayList)localArrayList1.get(i);
      if (!localArrayList2.isEmpty())
      {
        LviSubHeader localLviSubHeader = new LviSubHeader();
        localLviSubHeader.mTitle = arrayOfString[i];
        this.mDataHolder.add(localLviSubHeader);
        localIterator = localArrayList2.iterator();
        if (localIterator.hasNext())
          break;
      }
    }
    Movie localMovie = (Movie)localIterator.next();
    LviMovie localLviMovie = new LviMovie();
    localLviMovie.mMovie = localMovie;
    localLviMovie.mTrailerClick = getTrailerOnClickListener();
    if (i == 0);
    for (boolean bool = true; ; bool = false)
    {
      localLviMovie.mIsFeatured = bool;
      this.mDataHolder.add(localLviMovie);
      break;
    }
  }

  private void setRatingLviList()
  {
    Logger.d("FlxMain", this.className + ".setRatingLviList");
    ArrayList localArrayList = new ArrayList();
    localArrayList.addAll(this.mBoxOfficeFeatured);
    localArrayList.addAll(this.mBoxOfficeOtw);
    localArrayList.addAll(this.mBoxOfficeTbo);
    localArrayList.addAll(this.mBoxOfficeOthers);
    long l;
    Iterator localIterator;
    if (FlixsterApplication.getMovieRatingType() == 1)
    {
      Collections.sort(localArrayList, new MovieRottenScoreComparator());
      this.mDataHolder.clear();
      destroyExistingLviAd();
      this.lviAd = new LviAd();
      this.lviAd.mAdSlot = "MoviesTab";
      this.mDataHolder.add(this.lviAd);
      l = 0L;
      localIterator = localArrayList.iterator();
    }
    while (true)
    {
      if (!localIterator.hasNext())
      {
        LviFooter localLviFooter = new LviFooter();
        this.mDataHolder.add(localLviFooter);
        return;
        Collections.sort(localArrayList, new MoviePopcornComparator());
        break;
      }
      Movie localMovie = (Movie)localIterator.next();
      if (l != localMovie.getId())
      {
        l = localMovie.getId();
        LviMovie localLviMovie = new LviMovie();
        localLviMovie.mMovie = localMovie;
        localLviMovie.mTrailerClick = getTrailerOnClickListener();
        this.mDataHolder.add(localLviMovie);
      }
    }
  }

  private void setUpcomingLviList()
  {
    Logger.d("FlxMain", "BoxOfficePage.setUpcomingLviList ");
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "UpcomingTab";
    this.mDataHolder.add(this.lviAd);
    Iterator localIterator1;
    Object localObject;
    Iterator localIterator2;
    if (!this.mUpcomingFeatured.isEmpty())
    {
      LviSubHeader localLviSubHeader1 = new LviSubHeader();
      localLviSubHeader1.mTitle = getResources().getString(2131493054);
      this.mDataHolder.add(localLviSubHeader1);
      localIterator1 = ListHelper.clone(this.mUpcomingFeatured).iterator();
      if (localIterator1.hasNext());
    }
    else
    {
      localObject = FlixsterApplication.sToday;
      localIterator2 = ListHelper.clone(this.mUpcoming).iterator();
    }
    while (true)
    {
      if (!localIterator2.hasNext())
      {
        LviFooter localLviFooter = new LviFooter();
        this.mDataHolder.add(localLviFooter);
        return;
        Movie localMovie1 = (Movie)localIterator1.next();
        LviMovie localLviMovie1 = new LviMovie();
        localLviMovie1.mMovie = localMovie1;
        localLviMovie1.mTrailerClick = getTrailerOnClickListener();
        this.mDataHolder.add(localLviMovie1);
        break;
      }
      Movie localMovie2 = (Movie)localIterator2.next();
      String str = localMovie2.getProperty("theaterReleaseDate");
      Date localDate = localMovie2.getTheaterReleaseDate();
      if ((localDate != null) && (((Date)localObject).before(localDate)))
      {
        LviSubHeader localLviSubHeader2 = new LviSubHeader();
        localLviSubHeader2.mTitle = str;
        this.mDataHolder.add(localLviSubHeader2);
        localObject = localDate;
      }
      LviMovie localLviMovie2 = new LviMovie();
      localLviMovie2.mMovie = localMovie2;
      localLviMovie2.mTrailerClick = getTrailerOnClickListener();
      this.mDataHolder.add(localLviMovie2);
    }
  }

  protected String getAnalyticsAction()
  {
    return "Logo";
  }

  protected String getAnalyticsCategory()
  {
    return "WidgetEntrance";
  }

  protected String getAnalyticsTag()
  {
    if (this.mNavSelect == 2131165784)
    {
      switch (this.mSortOption)
      {
      default:
        return null;
      case 1:
        return "/boxoffice/popular";
      case 2:
        return "/boxoffice/rating";
      case 3:
      }
      return "/boxoffice/title";
    }
    return "/upcoming";
  }

  protected String getAnalyticsTitle()
  {
    if (this.mNavSelect == 2131165784)
    {
      switch (this.mSortOption)
      {
      default:
        return null;
      case 1:
        return "Box Office - Popular";
      case 2:
        return "Box Office - Rating";
      case 3:
      }
      return "Box Office - Title";
    }
    return "Upcoming Movies";
  }

  protected List<Movie> getFeaturedMovies()
  {
    if (this.mNavSelect == 2131165784)
      return this.mBoxOfficeFeatured;
    return this.mUpcomingFeatured;
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    this.mListView.setOnItemClickListener(getMovieItemClickListener());
    LinearLayout localLinearLayout = (LinearLayout)findViewById(2131165427);
    this.navBar = new SubNavBar(this);
    this.navBar.load(this.mNavListener, 2131493176, 2131493177);
    this.navBar.setSelectedButton(2131165784);
    localLinearLayout.addView(this.navBar, 0);
    this.mNavSelect = 2131165784;
    this.mSortOption = 1;
    this.mStickyTopAd.setSlot("MoviesTabStickyTop");
    this.mStickyBottomAd.setSlot("MoviesTabStickyBottom");
  }

  protected Dialog onCreateDialog(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return super.onCreateDialog(paramInt);
    case 5:
    }
    return new AlertDialog.Builder(this).setTitle(2131492896).setItems(2131623940, new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        switch (paramAnonymousInt)
        {
        default:
          return;
        case 0:
          BoxOfficePage.this.ScheduleLoadItemsTask(BoxOfficePage.this.mNavSelect, 1, 100L);
          BoxOfficePage.this.mListView.setSelection(0);
          return;
        case 1:
          BoxOfficePage.this.ScheduleLoadItemsTask(BoxOfficePage.this.mNavSelect, 2, 100L);
          BoxOfficePage.this.mListView.setSelection(0);
          return;
        case 2:
        }
        BoxOfficePage.this.ScheduleLoadItemsTask(BoxOfficePage.this.mNavSelect, 3, 100L);
        BoxOfficePage.this.mListView.setSelection(0);
      }
    }).create();
  }

  public void onPause()
  {
    super.onPause();
    this.mBoxOfficeFeatured.clear();
    this.mBoxOfficeOtw.clear();
    this.mBoxOfficeTbo.clear();
    this.mBoxOfficeOthers.clear();
    this.mUpcoming.clear();
    this.mUpcomingFeatured.clear();
  }

  protected void onRestoreInstanceState(Bundle paramBundle)
  {
    super.onRestoreInstanceState(paramBundle);
    this.mSortOption = paramBundle.getInt("LISTSTATE_SORT");
    this.mNavSelect = paramBundle.getInt("LISTSTATE_NAV");
  }

  public void onResume()
  {
    super.onResume();
    this.navBar.setSelectedButton(this.mNavSelect);
    ScheduleLoadItemsTask(this.mNavSelect, this.mSortOption, 100L);
  }

  protected void onSaveInstanceState(Bundle paramBundle)
  {
    super.onSaveInstanceState(paramBundle);
    paramBundle.putInt("LISTSTATE_SORT", this.mSortOption);
    paramBundle.putInt("LISTSTATE_NAV", this.mNavSelect);
  }

  protected void retryAction()
  {
    ScheduleLoadItemsTask(this.mNavSelect, this.mSortOption, 1000L);
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     net.flixster.android.BoxOfficePage
 * JD-Core Version:    0.6.2
 */
