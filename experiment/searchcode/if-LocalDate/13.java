package com.flixster.android.activity.hc;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.flixster.android.analytics.Tracker;
import com.flixster.android.analytics.Trackers;
import com.flixster.android.utils.ListHelper;
import com.flixster.android.utils.Logger;
import com.flixster.android.view.SubNavBar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.flixster.android.FlixsterApplication;
import net.flixster.android.data.DaoException;
import net.flixster.android.data.MovieDao;
import net.flixster.android.lvi.Lvi;
import net.flixster.android.lvi.LviCategory;
import net.flixster.android.lvi.LviFooter;
import net.flixster.android.lvi.LviMovie;
import net.flixster.android.lvi.LviSubHeader;
import net.flixster.android.model.Movie;

@TargetApi(11)
public class DvdFragment extends LviFragment
{
  private Handler initialContentLoadingHandler = new Handler()
  {
    public void handleMessage(Message paramAnonymousMessage)
    {
      Main localMain = (Main)DvdFragment.this.getActivity();
      if ((localMain == null) || (DvdFragment.this.isRemoving()));
      while (true)
      {
        return;
        long l = 0L;
        switch (DvdFragment.this.mNavSelect)
        {
        default:
        case 2131165784:
        case 2131165785:
        }
        while (l != 0L)
        {
          Intent localIntent = new Intent("DETAILS", null, localMain, MovieDetailsFragment.class);
          localIntent.putExtra("net.flixster.android.EXTRA_MOVIE_ID", l);
          localMain.startFragment(localIntent, MovieDetailsFragment.class);
          return;
          if (!DvdFragment.this.mNewReleases.isEmpty())
          {
            l = ((Movie)DvdFragment.this.mNewReleases.get(0)).getId();
            continue;
            if (!DvdFragment.this.mComingSoon.isEmpty())
              l = ((Movie)DvdFragment.this.mComingSoon.get(0)).getId();
          }
        }
      }
    }
  };
  private AdapterView.OnItemClickListener mCategoryListener = new AdapterView.OnItemClickListener()
  {
    public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
    {
      Lvi localLvi = (Lvi)DvdFragment.this.mData.get(paramAnonymousInt);
      if (localLvi.getItemViewType() == Lvi.VIEW_TYPE_CATEGORY)
      {
        Intent localIntent = new Intent("DETAILS", null, DvdFragment.this.getActivity(), CategoryFragment.class);
        localIntent.putExtra("TYPE", 3);
        localIntent.putExtra("CATEGORY_FILTER", ((LviCategory)localLvi).mFilter);
        localIntent.putExtra("CATEGORY_TITLE", ((LviCategory)localLvi).mCategory);
        ((Main)DvdFragment.this.getActivity()).startFragment(localIntent, CategoryFragment.class);
      }
    }
  };
  private final ArrayList<Movie> mComingSoon = new ArrayList();
  private final ArrayList<Movie> mComingSoonFeatured = new ArrayList();
  private View.OnClickListener mNavListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      switch (paramAnonymousView.getId())
      {
      default:
      case 2131165784:
      case 2131165785:
      case 2131165786:
      }
      while (true)
      {
        DvdFragment.this.ScheduleLoadItemsTask(DvdFragment.this.mNavSelect, 100L);
        return;
        DvdFragment.this.mNavSelect = 2131165784;
        DvdFragment.this.trackPage();
        DvdFragment.this.mListView.setOnItemClickListener(DvdFragment.this.getMovieItemClickListener());
        continue;
        DvdFragment.this.mNavSelect = 2131165785;
        DvdFragment.this.mListView.setOnItemClickListener(DvdFragment.this.getMovieItemClickListener());
        continue;
        DvdFragment.this.mNavSelect = 2131165786;
        DvdFragment.this.mListView.setOnItemClickListener(DvdFragment.this.mCategoryListener);
      }
    }
  };
  private int mNavSelect;
  private SubNavBar mNavbar;
  private LinearLayout mNavbarHolder;
  private final ArrayList<Movie> mNewReleases = new ArrayList();
  private final ArrayList<Movie> mNewReleasesFeatured = new ArrayList();

  private void ScheduleLoadItemsTask(final int paramInt, long paramLong)
  {
    try
    {
      ((Main)getActivity()).mShowDialogHandler.sendEmptyMessage(1);
      TimerTask local4 = new TimerTask()
      {
        public void run()
        {
          Main localMain = (Main)DvdFragment.this.getActivity();
          if ((localMain == null) || (DvdFragment.this.isRemoving()))
            return;
          Logger.d("FlxMain", "DvdPage.ScheduleLoadItemsTask.run navSelection:" + paramInt);
          while (true)
          {
            try
            {
              switch (paramInt)
              {
              default:
                DvdFragment.this.mUpdateHandler.sendEmptyMessage(0);
                if (localMain != null)
                  localMain.mRemoveDialogHandler.sendEmptyMessage(1);
                if (!DvdFragment.this.isInitialContentLoaded)
                {
                  DvdFragment.this.isInitialContentLoaded = true;
                  DvdFragment.this.initialContentLoadingHandler.sendEmptyMessage(0);
                }
                return;
              case 2131165784:
                if (DvdFragment.this.mNewReleases.isEmpty())
                {
                  ArrayList localArrayList3 = DvdFragment.this.mNewReleasesFeatured;
                  ArrayList localArrayList4 = DvdFragment.this.mNewReleases;
                  int j = DvdFragment.this.mRetryCount;
                  boolean bool2 = false;
                  if (j == 0)
                    bool2 = true;
                  MovieDao.fetchDvdNewRelease(localArrayList3, localArrayList4, bool2);
                }
                DvdFragment.this.setNewReleasesLviList();
                continue;
              case 2131165785:
              case 2131165786:
              }
            }
            catch (DaoException localDaoException)
            {
              Logger.e("FlxMain", "DvdPage.ScheduleLoadItemsTask.run DaoException", localDaoException);
              DvdFragment.this.retryLogic(localDaoException);
              return;
              if (DvdFragment.this.mComingSoon.isEmpty())
              {
                ArrayList localArrayList1 = DvdFragment.this.mComingSoonFeatured;
                ArrayList localArrayList2 = DvdFragment.this.mComingSoon;
                int i = DvdFragment.this.mRetryCount;
                boolean bool1 = false;
                if (i == 0)
                  bool1 = true;
                MovieDao.fetchDvdComingSoon(localArrayList1, localArrayList2, bool1);
              }
              DvdFragment.this.setComingSoonLviList();
              continue;
            }
            finally
            {
              if ((localMain != null) && (localMain.mLoadingDialog != null) && (localMain.mLoadingDialog.isShowing()))
                localMain.mRemoveDialogHandler.sendEmptyMessage(1);
            }
            DvdFragment.this.setBrowseLviList();
          }
        }
      };
      Main localMain = (Main)getActivity();
      if ((localMain != null) && (!localMain.isFinishing()) && (localMain.mPageTimer != null))
        localMain.mPageTimer.schedule(local4, paramLong);
      return;
    }
    finally
    {
    }
  }

  public static int getFlixFragId()
  {
    return 4;
  }

  private void setBrowseLviList()
  {
    this.mDataHolder.clear();
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
    if (((Main)getActivity() == null) || (isRemoving()))
      return;
    this.mDataHolder.clear();
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

  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
  {
    View localView = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
    this.mListView.setOnItemClickListener(getMovieItemClickListener());
    this.mNavbarHolder = ((LinearLayout)localView.findViewById(2131165427));
    this.mNavbar = new SubNavBar(getActivity());
    this.mNavbar.load(this.mNavListener, 2131492910, 2131492909, 2131492905);
    this.mNavbar.setSelectedButton(2131165784);
    this.mNavbarHolder.addView(this.mNavbar, 0);
    this.mNavSelect = 2131165784;
    return localView;
  }

  public void onPause()
  {
    super.onPause();
    this.mNewReleases.clear();
    this.mComingSoon.clear();
    this.mNewReleasesFeatured.clear();
    this.mComingSoonFeatured.clear();
  }

  public void onResume()
  {
    super.onResume();
    trackPage();
    if (this.mNavSelect == 2131165786)
      this.mListView.setOnItemClickListener(this.mCategoryListener);
    while (true)
    {
      this.mNavbar.setSelectedButton(this.mNavSelect);
      ScheduleLoadItemsTask(this.mNavSelect, 100L);
      return;
      this.mListView.setOnItemClickListener(getMovieItemClickListener());
    }
  }

  public void onSaveInstanceState(Bundle paramBundle)
  {
    super.onSaveInstanceState(paramBundle);
    Logger.d("FlxMain", "DvdPage.onSaveInstanceState()");
    paramBundle.putInt("LISTSTATE_NAV", this.mNavSelect);
  }

  protected void retryAction()
  {
    ScheduleLoadItemsTask(this.mNavSelect, 1000L);
  }

  public void trackPage()
  {
    switch (this.mNavSelect)
    {
    default:
      return;
    case 2131165784:
      Trackers.instance().track("/dvds/new-releases", "Dvds - New Releases");
      return;
    case 2131165785:
      Trackers.instance().track("/dvds/coming-soon", "Dvds - Coming Soon");
      return;
    case 2131165786:
    }
    Trackers.instance().track("/dvds/browse", "Dvds - Browse");
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     com.flixster.android.activity.hc.DvdFragment
 * JD-Core Version:    0.6.2
 */
