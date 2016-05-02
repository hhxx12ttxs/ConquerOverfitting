package net.flixster.android;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import com.flixster.android.activity.decorator.TopLevelDecorator;
import com.flixster.android.utils.ListHelper;
import com.flixster.android.utils.Logger;
import java.util.ArrayList;
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

@Deprecated
public class UpcomingPage extends LviActivityCopy
{
  private final ArrayList<Movie> mUpcoming = new ArrayList();
  private final ArrayList<Movie> mUpcomingFeatured = new ArrayList();

  private void ScheduleLoadItemsTask(long paramLong)
  {
    try
    {
      this.throbberHandler.sendEmptyMessage(1);
      TimerTask local1 = new TimerTask()
      {
        public void run()
        {
          Logger.d("FlxMain", "UpcomingPage.ScheduleLoadItemsTask.run");
          try
          {
            ArrayList localArrayList1;
            ArrayList localArrayList2;
            if (UpcomingPage.this.mUpcoming.isEmpty())
            {
              localArrayList1 = UpcomingPage.this.mUpcomingFeatured;
              localArrayList2 = UpcomingPage.this.mUpcoming;
              if (UpcomingPage.this.mRetryCount != 0)
                break label91;
            }
            label91: for (boolean bool2 = true; ; bool2 = false)
            {
              MovieDao.fetchUpcoming(localArrayList1, localArrayList2, bool2);
              boolean bool1 = UpcomingPage.this.shouldSkipBackgroundTask(this.val$currResumeCtr);
              if (!bool1)
                break;
              return;
            }
            UpcomingPage.this.setUpcomingLviList();
            UpcomingPage.this.trackPage();
            UpcomingPage.this.mUpdateHandler.sendEmptyMessage(0);
            return;
          }
          catch (DaoException localDaoException)
          {
            Logger.e("FlxMain", "UpcomingPage.ScheduleLoadItemsTask.run DaoException", localDaoException);
            UpcomingPage.this.retryLogic(localDaoException);
            return;
          }
          finally
          {
            UpcomingPage.this.throbberHandler.sendEmptyMessage(0);
          }
        }
      };
      if (this.mPageTimer != null)
        this.mPageTimer.schedule(local1, paramLong);
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  private void setUpcomingLviList()
  {
    Logger.d("FlxMain", "UpcomingPage.setUpcomingLviList");
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "UpcomingTab";
    this.mDataHolder.add(this.lviAd);
    ArrayList localArrayList = ListHelper.clone(this.mUpcomingFeatured);
    Iterator localIterator1;
    Object localObject;
    Iterator localIterator2;
    if (!localArrayList.isEmpty())
    {
      LviSubHeader localLviSubHeader1 = new LviSubHeader();
      localLviSubHeader1.mTitle = getResources().getString(2131493054);
      this.mDataHolder.add(localLviSubHeader1);
      localIterator1 = localArrayList.iterator();
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
    return "/upcoming";
  }

  protected String getAnalyticsTitle()
  {
    return "Upcoming Movies";
  }

  protected List<Movie> getFeaturedMovies()
  {
    return this.mUpcomingFeatured;
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    this.mListView.setOnItemClickListener(getMovieItemClickListener());
    this.mStickyTopAd.setSlot("UpcomingTabStickyTop");
    this.mStickyBottomAd.setSlot("UpcomingTabStickyBottom");
  }

  public void onPause()
  {
    super.onPause();
    this.mUpcoming.clear();
    this.mUpcomingFeatured.clear();
  }

  public void onResume()
  {
    super.onResume();
    ScheduleLoadItemsTask(100L);
  }

  protected void retryAction()
  {
    ScheduleLoadItemsTask(1000L);
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     net.flixster.android.UpcomingPage
 * JD-Core Version:    0.6.2
 */
