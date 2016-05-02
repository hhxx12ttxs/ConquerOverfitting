package com.fiksu.asotracking;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import java.util.Date;

class RatingPrompter
{
  private static final int NUMBER_OF_DAYS_BEFORE_RATING_IN_MILLIS = 432000000;
  private static final int NUMBER_OF_LAUNCHES_BEFORE_RATING = 5;
  private static final String PREFERENCES_ALREADY_RATED_KEY = "Fiksu.alreadyRated";
  private static final String PREFERENCES_FIRST_LAUNCHED_KEY = "Fiksu.firstLaunchedAt";
  private static final String PREFERENCES_NAME_KEY = "Fiksu.ratingsDictionary";
  private static final String PREFERENCES_NUMBER_OF_LAUNCHES_KEY = "Fiksu.numberOfLaunches";
  private final Activity mActivity;
  private final String mAppName;
  private final RatingClickListener mNoRatingButtonListener;
  private final RatingClickListener mPostponeRatingButtonListener;
  private final RatingClickListener mRatingButtonListener;

  public RatingPrompter(Activity paramActivity)
  {
    this.mRatingButtonListener = new RatingClickListener(PromptResult.USER_RATED, paramActivity);
    this.mNoRatingButtonListener = new RatingClickListener(PromptResult.USER_DID_NOT_RATE, paramActivity);
    this.mPostponeRatingButtonListener = new RatingClickListener(PromptResult.USER_POSTPONED_RATING, paramActivity);
    this.mActivity = paramActivity;
    PackageManager localPackageManager = this.mActivity.getPackageManager();
    String str1 = this.mActivity.getPackageName();
    try
    {
      String str3 = localPackageManager.getApplicationInfo(str1, 0).loadLabel(localPackageManager).toString();
      str2 = str3;
      this.mAppName = str2;
      return;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      while (true)
      {
        Log.e("FiksuTracking", "Could not access package: " + str1);
        String str2 = null;
      }
    }
  }

  private boolean connectedToNetwork()
  {
    NetworkInfo localNetworkInfo = ((ConnectivityManager)this.mActivity.getSystemService("connectivity")).getActiveNetworkInfo();
    return (localNetworkInfo != null) && (localNetworkInfo.isAvailable()) && (localNetworkInfo.isConnected());
  }

  private boolean enoughTimeSinceFirstLaunch(SharedPreferences paramSharedPreferences, SharedPreferences.Editor paramEditor)
  {
    Date localDate = new Date();
    long l = paramSharedPreferences.getLong("Fiksu.firstLaunchedAt", localDate.getTime());
    if (localDate.getTime() - l > 432000000L);
    for (boolean bool = true; ; bool = false)
    {
      if (localDate.getTime() == l)
        paramEditor.putLong("Fiksu.firstLaunchedAt", localDate.getTime());
      return bool;
    }
  }

  private int getNumberOfLaunches(SharedPreferences paramSharedPreferences, SharedPreferences.Editor paramEditor)
  {
    int i = 1 + paramSharedPreferences.getInt("Fiksu.numberOfLaunches", 0);
    paramEditor.putInt("Fiksu.numberOfLaunches", i);
    return i;
  }

  private void setUserRated()
  {
    SharedPreferences.Editor localEditor = this.mActivity.getSharedPreferences("Fiksu.ratingsDictionary", 0).edit();
    localEditor.putBoolean("Fiksu.alreadyRated", true);
    localEditor.commit();
  }

  private boolean shouldPrompt()
  {
    if (this.mAppName == null);
    int i;
    boolean bool;
    do
    {
      SharedPreferences localSharedPreferences;
      do
      {
        do
          return false;
        while (!connectedToNetwork());
        localSharedPreferences = this.mActivity.getSharedPreferences("Fiksu.ratingsDictionary", 0);
      }
      while (localSharedPreferences.getBoolean("Fiksu.alreadyRated", false));
      SharedPreferences.Editor localEditor = localSharedPreferences.edit();
      i = getNumberOfLaunches(localSharedPreferences, localEditor);
      bool = enoughTimeSinceFirstLaunch(localSharedPreferences, localEditor);
      localEditor.commit();
    }
    while ((i < 5) || (!bool));
    return true;
  }

  public void maybeShowPrompt()
  {
    if (!shouldPrompt())
      return;
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(this.mActivity);
    localBuilder.setTitle("Enjoying " + this.mAppName + "?");
    localBuilder.setMessage("If so, please rate it in the Android Marketplace.  It takes less than a minute and we appreciate your support!");
    localBuilder.setPositiveButton("Rate " + this.mAppName, this.mRatingButtonListener);
    localBuilder.setNegativeButton("No thanks", this.mNoRatingButtonListener);
    localBuilder.setNeutralButton("Remind me later", this.mPostponeRatingButtonListener);
    localBuilder.show();
  }

  private static enum PromptResult
  {
    static
    {
      USER_DID_NOT_RATE = new PromptResult("USER_DID_NOT_RATE", 1, 2);
      USER_POSTPONED_RATING = new PromptResult("USER_POSTPONED_RATING", 2, 3);
      PromptResult[] arrayOfPromptResult = new PromptResult[3];
      arrayOfPromptResult[0] = USER_RATED;
      arrayOfPromptResult[1] = USER_DID_NOT_RATE;
      arrayOfPromptResult[2] = USER_POSTPONED_RATING;
    }

    private PromptResult(int arg3)
    {
    }
  }

  private class RatingClickListener
    implements DialogInterface.OnClickListener
  {
    private RatingPrompter.PromptResult mPromptResult;

    RatingClickListener(RatingPrompter.PromptResult paramActivity, Activity arg3)
    {
      this.mPromptResult = paramActivity;
    }

    public void onClick(DialogInterface paramDialogInterface, int paramInt)
    {
      switch ($SWITCH_TABLE$com$fiksu$asotracking$RatingPrompter$PromptResult()[this.mPromptResult.ordinal()])
      {
      default:
        return;
      case 1:
        new RatingEventTracker(RatingPrompter.this.mActivity, "rated", 5).uploadEvent();
        Log.e("FiksuTracking", RatingPrompter.this.mActivity.getPackageName());
        RatingPrompter.this.setUserRated();
        RatingPrompter.this.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + RatingPrompter.this.mActivity.getPackageName())));
        return;
      case 2:
        new RatingEventTracker(RatingPrompter.this.mActivity, "did_not_rate", 5).uploadEvent();
        RatingPrompter.this.setUserRated();
        return;
      case 3:
      }
      new RatingEventTracker(RatingPrompter.this.mActivity, "deferred_rating", 5).uploadEvent();
    }
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     com.fiksu.asotracking.RatingPrompter
 * JD-Core Version:    0.6.2
 */
