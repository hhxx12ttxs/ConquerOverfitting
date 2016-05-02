package net.flixster.android;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.flixster.android.activity.decorator.TopLevelDecorator;
import com.flixster.android.utils.ListHelper;
import com.flixster.android.utils.Logger;
import com.flixster.android.view.SubNavBar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.flixster.android.ads.AdView;
import net.flixster.android.data.DaoException;
import net.flixster.android.data.MovieDao;
import net.flixster.android.lvi.Lvi;
import net.flixster.android.lvi.LviAd;
import net.flixster.android.lvi.LviCategory;
import net.flixster.android.lvi.LviFooter;
import net.flixster.android.lvi.LviMovie;
import net.flixster.android.lvi.LviSubHeader;
import net.flixster.android.model.Movie;

public class DvdPage extends LviActivityCopy
{
  private AdapterView.OnItemClickListener mCategoryListener = new AdapterView.OnItemClickListener()
  {
    public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
    {
      Lvi localLvi = (Lvi)DvdPage.this.mData.get(paramAnonymousInt);
      if (localLvi.getItemViewType() == Lvi.VIEW_TYPE_CATEGORY)
      {
        Intent localIntent = new Intent("DETAILS", null, DvdPage.this, CategoryPage.class);
        localIntent.putExtra("TYPE", 3);
        localIntent.putExtra("CATEGORY_FILTER", ((LviCategory)localLvi).mFilter);
        localIntent.putExtra("CATEGORY_TITLE", ((LviCategory)localLvi).mCategory);
        DvdPage.this.startActivity(localIntent);
      }
    }
  };
  private final ArrayList<Movie> mComingSoon = new ArrayList();
  private final ArrayList<Movie> mComingSoonFeatured = new ArrayList();
  private View.OnClickListener mNavListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      DvdPage.this.mNavSelect = paramAnonymousView.getId();
      if (DvdPage.this.mNavSelect == 2131165786)
        DvdPage.this.mListView.setOnItemClickListener(DvdPage.this.mCategoryListener);
      while (true)
      {
        TopLevelDecorator.incrementResumeCtr();
        DvdPage.this.ScheduleLoadItemsTask(DvdPage.this.mNavSelect, 100L);
        return;
        DvdPage.this.mListView.setOnItemClickListener(DvdPage.this.getMovieItemClickListener());
      }
    }
  };
  private int mNavSelect;
  private final ArrayList<Movie> mNewReleases = new ArrayList();
  private final ArrayList<Movie> mNewReleasesFeatured = new ArrayList();
  private SubNavBar navBar;

  private void ScheduleLoadItemsTask(final int paramInt, long paramLong)
  {
    try
    {
      this.throbberHandler.sendEmptyMessage(1);
      TimerTask local3 = new TimerTask()
      {
        public void run()
        {
          boolean bool1 = true;
          Logger.d("FlxMain", "DvdPage.ScheduleLoadItemsTask.run navSelection:" + paramInt);
          while (true)
          {
            try
            {
              switch (paramInt)
              {
              default:
                DvdPage.this.trackPage();
                DvdPage.this.mUpdateHandler.sendEmptyMessage(0);
                DvdPage.this.checkAndShowLaunchAd();
                return;
              case 2131165784:
                if (DvdPage.this.mNewReleases.isEmpty())
                {
                  ArrayList localArrayList3 = DvdPage.this.mNewReleasesFeatured;
                  ArrayList localArrayList4 = DvdPage.this.mNewReleases;
                  if (DvdPage.this.mRetryCount == 0)
                    MovieDao.fetchDvdNewRelease(localArrayList3, localArrayList4, bool1);
                }
                else
                {
                  boolean bool2 = DvdPage.this.shouldSkipBackgroundTask(this.val$currResumeCtr);
                  if (!bool2)
                    continue;
                  return;
                }
                bool1 = false;
                continue;
                DvdPage.this.setNewReleasesLviList();
                continue;
              case 2131165785:
              case 2131165786:
              }
            }
            catch (DaoException localDaoException)
            {
              Logger.e("FlxMain", "DvdPage.ScheduleLoadItemsTask.run DaoException", localDaoException);
              DvdPage.this.retryLogic(localDaoException);
              return;
              if (DvdPage.this.mComingSoon.isEmpty())
              {
                ArrayList localArrayList1 = DvdPage.this.mComingSoonFeatured;
                ArrayList localArrayList2 = DvdPage.this.mComingSoon;
                if (DvdPage.this.mRetryCount == 0)
                  MovieDao.fetchDvdComingSoon(localArrayList1, localArrayList2, bool1);
              }
              else
              {
                if (DvdPage.this.shouldSkipBackgroundTask(this.val$currResumeCtr))
                  continue;
                DvdPage.this.setComingSoonLviList();
                continue;
              }
            }
            finally
            {
              DvdPage.this.throbberHandler.sendEmptyMessage(0);
            }
            bool1 = false;
            continue;
            DvdPage.this.setBrowseLviList();
          }
        }
      };
      if (this.mPageTimer != null)
        this.mPageTimer.schedule(local3, paramLong);
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  private void setBrowseLviList()
  {
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "DVDTab";
    this.mDataHolder.add(this.lviAd);
    String[] arrayOfString1 = getResources().getStringArray(2131623944);
    String[] arrayOfString2 = getResources().getStringArray(2131623945);
    for (int i = 0; ; i++)
    {
      if (i >= arrayOfString1.length)
      {
        LviFooter localLviFooter = new LviFooter();
        this.mDataHolder.add(localLviFooter);
        return;
      }
      LviCategory localLviCategory = new LviCategory();
      localLviCategory.mCategory = arrayOfString1[i];
      localLviCategory.mFilter = arrayOfString2[i];
      this.mDataHolder.add(localLviCategory);
    }
  }

  private void setComingSoonLviList()
  {
    Object localObject = FlixsterApplication.sToday;
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "DVDTab";
    this.mDataHolder.add(this.lviAd);
    ArrayList localArrayList = ListHelper.clone(this.mComingSoonFeatured);
    Iterator localIterator1;
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
      Logger.d("FlxMain", "DvdPage.setUpcomingLviLlist");
      localIterator2 = ListHelper.clone(this.mComingSoon).iterator();
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
      String str = localMovie2.getProperty("dvdReleaseDate");
      Date localDate = localMovie2.getDvdReleaseDate();
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

  private void setNewReleasesLviList()
  {
    this.mDataHolder.clear();
    destroyExistingLviAd();
    this.lviAd = new LviAd();
    this.lviAd.mAdSlot = "DVDTab";
    this.mDataHolder.add(this.lviAd);
    ArrayList localArrayList = ListHelper.clone(this.mNewReleasesFeatured);
    Iterator localIterator1;
    Object localObject;
    int i;
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
      i = 1;
      localIterator2 = ListHelper.clone(this.mNewReleases).iterator();
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
      String str = localMovie2.getProperty("dvdReleaseDate");
      Date localDate = localMovie2.getDvdReleaseDate();
      if ((i != 0) || ((localDate != null) && (((Date)localObject).after(localDate))))
      {
        i = 0;
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
    switch (this.mNavSelect)
    {
    default:
      return null;
    case 2131165784:
      return "/dvds/new-releases";
    case 2131165785:
      return "/dvds/coming-soon";
    case 2131165786:
    }
    return "/dvds/browse";
  }

  protected String getAnalyticsTitle()
  {
    switch (this.mNavSelect)
    {
    default:
      return null;
    case 2131165784:
      return "Dvds - New Releases";
    case 2131165785:
      return "Dvds - Coming Soon";
    case 2131165786:
    }
    return "Dvds - Browse";
  }

  protected List<Movie> getFeaturedMovies()
  {
    switch (this.mNavSelect)
    {
    default:
      return null;
    case 2131165784:
      return this.mNewReleasesFeatured;
    case 2131165785:
    }
    return this.mComingSoonFeatured;
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    this.mListView.setOnItemClickListener(getMovieItemClickListener());
    LinearLayout localLinearLayout = (LinearLayout)findViewById(2131165427);
    this.navBar = new SubNavBar(this);
    this.navBar.load(this.mNavListener, 2131492910, 2131492909, 2131492905);
    this.navBar.setSelectedButton(2131165784);
    localLinearLayout.addView(this.navBar, 0);
    this.mNavSelect = 2131165784;
    this.mStickyTopAd.setSlot("DVDTabStickyTop");
    this.mStickyBottomAd.setSlot("DVDTabStickyBottom");
  }

  public void onPause()
  {
    super.onPause();
    this.mNewReleases.clear();
    this.mComingSoon.clear();
    this.mNewReleasesFeatured.clear();
    this.mComingSoonFeatured.clear();
  }

  protected void onRestoreInstanceState(Bundle paramBundle)
  {
    super.onRestoreInstanceState(paramBundle);
    Logger.d("FlxMain", "DvdPage.onRestoreInstanceState()");
    this.mNavSelect = paramBundle.getInt("LISTSTATE_NAV");
  }

  public void onResume()
  {
    super.onResume();
    if (this.mNavSelect == 2131165786)
      this.mListView.setOnItemClickListener(this.mCategoryListener);
    while (true)
    {
      this.navBar.setSelectedButton(this.mNavSelect);
      ScheduleLoadItemsTask(this.mNavSelect, 100L);
      return;
      this.mListView.setOnItemClickListener(getMovieItemClickListener());
    }
  }

  protected void onSaveInstanceState(Bundle paramBundle)
  {
    super.onSaveInstanceState(paramBundle);
    Logger.d("FlxMain", "DvdPage.onSaveInstanceState()");
    paramBundle.putInt("LISTSTATE_NAV", this.mNavSelect);
  }

  protected void retryAction()
  {
    ScheduleLoadItemsTask(this.mNavSelect, 1000L);
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     net.flixster.android.DvdPage
 * JD-Core Version:    0.6.2
 */
