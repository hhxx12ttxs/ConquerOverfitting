/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.am;

import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.am.ActivityManagerService.PendingActivityLaunch;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.IThumbnailRetriever;
import static android.app.IActivityManager.START_CLASS_NOT_FOUND;
import static android.app.IActivityManager.START_DELIVERED_TO_TOP;
import static android.app.IActivityManager.START_FORWARD_AND_REQUEST_CONFLICT;
import static android.app.IActivityManager.START_INTENT_NOT_RESOLVED;
import static android.app.IActivityManager.START_PERMISSION_DENIED;
import static android.app.IActivityManager.START_RETURN_INTENT_TO_CALLER;
import static android.app.IActivityManager.START_SUCCESS;
import static android.app.IActivityManager.START_SWITCHES_CANCELED;
import static android.app.IActivityManager.START_TASK_TO_FRONT;
import android.app.IApplicationThread;
import android.app.PendingIntent;
import android.app.ResultInfo;
import android.app.IActivityManager.WaitResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerPolicy;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * State and management of a single stack of activities.
 */
final class ActivityStack {
    static final String TAG = ActivityManagerService.TAG;
    static final boolean localLOGV = ActivityManagerService.localLOGV;
    static final boolean DEBUG_SWITCH = ActivityManagerService.DEBUG_SWITCH;
    static final boolean DEBUG_PAUSE = ActivityManagerService.DEBUG_PAUSE;
    static final boolean DEBUG_VISBILITY = ActivityManagerService.DEBUG_VISBILITY;
    static final boolean DEBUG_USER_LEAVING = ActivityManagerService.DEBUG_USER_LEAVING;
    static final boolean DEBUG_TRANSITION = ActivityManagerService.DEBUG_TRANSITION;
    static final boolean DEBUG_RESULTS = ActivityManagerService.DEBUG_RESULTS;
    static final boolean DEBUG_CONFIGURATION = ActivityManagerService.DEBUG_CONFIGURATION;
    static final boolean DEBUG_TASKS = ActivityManagerService.DEBUG_TASKS;
    
    static final boolean DEBUG_STATES = false;
    static final boolean DEBUG_ADD_REMOVE = false;
    static final boolean DEBUG_SAVED_STATE = false;

    static final boolean VALIDATE_TOKENS = ActivityManagerService.VALIDATE_TOKENS;
    
    // How long we wait until giving up on the last activity telling us it
    // is idle.
    static final int IDLE_TIMEOUT = 10*1000;

    // Ticks during which we check progress while waiting for an app to launch.
    static final int LAUNCH_TICK = 500;

    // How long we wait until giving up on the last activity to pause.  This
    // is short because it directly impacts the responsiveness of starting the
    // next activity.
    static final int PAUSE_TIMEOUT = 500;

    // How long we can hold the sleep wake lock before giving up.
    static final int SLEEP_TIMEOUT = 5*1000;

    // How long we can hold the launch wake lock before giving up.
    static final int LAUNCH_TIMEOUT = 10*1000;

    // How long we wait until giving up on an activity telling us it has
    // finished destroying itself.
    static final int DESTROY_TIMEOUT = 10*1000;
    
    // How long until we reset a task when the user returns to it.  Currently
    // disabled.
    static final long ACTIVITY_INACTIVE_RESET_TIME = 0;
    
    // How long between activity launches that we consider safe to not warn
    // the user about an unexpected activity being launched on top.
    static final long START_WARN_TIME = 5*1000;
    
    // Set to false to disable the preview that is shown while a new activity
    // is being started.
    static final boolean SHOW_APP_STARTING_PREVIEW = true;
    
    enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED
    }

    final ActivityManagerService mService;
    final boolean mMainStack;
    
    final Context mContext;
    
    /**
     * The back history of all previous (and possibly still
     * running) activities.  It contains HistoryRecord objects.
     */
    final ArrayList<ActivityRecord> mHistory = new ArrayList<ActivityRecord>();

    /**
     * Used for validating app tokens with window manager.
     */
    final ArrayList<IBinder> mValidateAppTokens = new ArrayList<IBinder>();

    /**
     * List of running activities, sorted by recent usage.
     * The first entry in the list is the least recently used.
     * It contains HistoryRecord objects.
     */
    final ArrayList<ActivityRecord> mLRUActivities = new ArrayList<ActivityRecord>();

    /**
     * List of activities that are waiting for a new activity
     * to become visible before completing whatever operation they are
     * supposed to do.
     */
    final ArrayList<ActivityRecord> mWaitingVisibleActivities
            = new ArrayList<ActivityRecord>();

    /**
     * List of activities that are ready to be stopped, but waiting
     * for the next activity to settle down before doing so.  It contains
     * HistoryRecord objects.
     */
    final ArrayList<ActivityRecord> mStoppingActivities
            = new ArrayList<ActivityRecord>();

    /**
     * List of activities that are in the process of going to sleep.
     */
    final ArrayList<ActivityRecord> mGoingToSleepActivities
            = new ArrayList<ActivityRecord>();

    /**
     * Animations that for the current transition have requested not to
     * be considered for the transition animation.
     */
    final ArrayList<ActivityRecord> mNoAnimActivities
            = new ArrayList<ActivityRecord>();

    /**
     * List of activities that are ready to be finished, but waiting
     * for the previous activity to settle down before doing so.  It contains
     * HistoryRecord objects.
     */
    final ArrayList<ActivityRecord> mFinishingActivities
            = new ArrayList<ActivityRecord>();
    
    /**
     * List of people waiting to find out about the next launched activity.
     */
    final ArrayList<IActivityManager.WaitResult> mWaitingActivityLaunched
            = new ArrayList<IActivityManager.WaitResult>();
    
    /**
     * List of people waiting to find out about the next visible activity.
     */
    final ArrayList<IActivityManager.WaitResult> mWaitingActivityVisible
            = new ArrayList<IActivityManager.WaitResult>();
    
    /**
     * Set when the system is going to sleep, until we have
     * successfully paused the current activity and released our wake lock.
     * At that point the system is allowed to actually sleep.
     */
    final PowerManager.WakeLock mGoingToSleep;

    /**
     * We don't want to allow the device to go to sleep while in the process
     * of launching an activity.  This is primarily to allow alarm intent
     * receivers to launch an activity and get that to run before the device
     * goes back to sleep.
     */
    final PowerManager.WakeLock mLaunchingActivity;

    /**
     * When we are in the process of pausing an activity, before starting the
     * next one, this variable holds the activity that is currently being paused.
     */
    ActivityRecord mPausingActivity = null;

    /**
     * This is the last activity that we put into the paused state.  This is
     * used to determine if we need to do an activity transition while sleeping,
     * when we normally hold the top activity paused.
     */
    ActivityRecord mLastPausedActivity = null;

    /**
     * Current activity that is resumed, or null if there is none.
     */
    ActivityRecord mResumedActivity = null;
    
    /**
     * This is the last activity that has been started.  It is only used to
     * identify when multiple activities are started at once so that the user
     * can be warned they may not be in the activity they think they are.
     */
    ActivityRecord mLastStartedActivity = null;
    
    /**
     * Set when we know we are going to be calling updateConfiguration()
     * soon, so want to skip intermediate config checks.
     */
    boolean mConfigWillChange;

    /**
     * Set to indicate whether to issue an onUserLeaving callback when a
     * newly launched activity is being brought in front of us.
     */
    boolean mUserLeaving = false;
    
    long mInitialStartTime = 0;
    
    /**
     * Set when we have taken too long waiting to go to sleep.
     */
    boolean mSleepTimeout = false;

    /**
     * Dismiss the keyguard after the next activity is displayed?
     */
    boolean mDismissKeyguardOnNextActivity = false;

    int mThumbnailWidth = -1;
    int mThumbnailHeight = -1;

    static final int SLEEP_TIMEOUT_MSG = 8;
    static final int PAUSE_TIMEOUT_MSG = 9;
    static final int IDLE_TIMEOUT_MSG = 10;
    static final int IDLE_NOW_MSG = 11;
    static final int LAUNCH_TICK_MSG = 12;
    static final int LAUNCH_TIMEOUT_MSG = 16;
    static final int DESTROY_TIMEOUT_MSG = 17;
    static final int RESUME_TOP_ACTIVITY_MSG = 19;

    final Handler mHandler = new Handler() {
        //public Handler() {
        //    if (localLOGV) Slog.v(TAG, "Handler started!");
        //}

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SLEEP_TIMEOUT_MSG: {
                    synchronized (mService) {
                        if (mService.isSleeping()) {
                            Slog.w(TAG, "Sleep timeout!  Sleeping now.");
                            mSleepTimeout = true;
                            checkReadyForSleepLocked();
                        }
                    }
                } break;
                case PAUSE_TIMEOUT_MSG: {
                    ActivityRecord r = (ActivityRecord)msg.obj;
                    // We don't at this point know if the activity is fullscreen,
                    // so we need to be conservative and assume it isn't.
                    Slog.w(TAG, "Activity pause timeout for " + r);
                    int pid = -1;
                    long pauseTime = 0;
                    String m = null;
                    synchronized (mService) {
                        if (r.app != null) {
                            pid = r.app.pid;
                        }
                        pauseTime = r.pauseTime;
                        m = "pausing " + r;
                    }
                    if (pid > 0) {
                        mService.logAppTooSlow(pid, pauseTime, m);
                    }

                    activityPaused(r != null ? r.appToken : null, true);
                } break;
                case IDLE_TIMEOUT_MSG: {
                    if (mService.mDidDexOpt) {
                        mService.mDidDexOpt = false;
                        Message nmsg = mHandler.obtainMessage(IDLE_TIMEOUT_MSG);
                        nmsg.obj = msg.obj;
                        mHandler.sendMessageDelayed(nmsg, IDLE_TIMEOUT);
                        return;
                    }
                    // We don't at this point know if the activity is fullscreen,
                    // so we need to be conservative and assume it isn't.
                    ActivityRecord r = (ActivityRecord)msg.obj;
                    Slog.w(TAG, "Activity idle timeout for " + r);
                    activityIdleInternal(r != null ? r.appToken : null, true, null);
                } break;
                case LAUNCH_TICK_MSG: {
                    ActivityRecord r = (ActivityRecord)msg.obj;
                    int pid = -1;
                    long launchTickTime = 0;
                    String m = null;
                    synchronized (mService) {
                        if (r.continueLaunchTickingLocked()) {
                            if (r.app != null) {
                                pid = r.app.pid;
                            }
                            launchTickTime = r.launchTickTime;
                            m = "launching " + r;
                        }
                    }
                    if (pid > 0) {
                        mService.logAppTooSlow(pid, launchTickTime, m);
                    }
                } break;
                case DESTROY_TIMEOUT_MSG: {
                    ActivityRecord r = (ActivityRecord)msg.obj;
                    // We don't at this point know if the activity is fullscreen,
                    // so we need to be conservative and assume it isn't.
                    Slog.w(TAG, "Activity destroy timeout for " + r);
                    activityDestroyed(r != null ? r.appToken : null);
                } break;
                case IDLE_NOW_MSG: {
                    ActivityRecord r = (ActivityRecord)msg.obj;
                    activityIdleInternal(r != null ? r.appToken : null, false, null);
                } break;
                case LAUNCH_TIMEOUT_MSG: {
                    if (mService.mDidDexOpt) {
                        mService.mDidDexOpt = false;
                        Message nmsg = mHandler.obtainMessage(LAUNCH_TIMEOUT_MSG);
                        mHandler.sendMessageDelayed(nmsg, LAUNCH_TIMEOUT);
                        return;
                    }
                    synchronized (mService) {
                        if (mLaunchingActivity.isHeld()) {
                            Slog.w(TAG, "Launch timeout has expired, giving up wake lock!");
                            mLaunchingActivity.release();
                        }
                    }
                } break;
                case RESUME_TOP_ACTIVITY_MSG: {
                    synchronized (mService) {
                        resumeTopActivityLocked(null);
                    }
                } break;
            }
        }
    };
    
    ActivityStack(ActivityManagerService service, Context context, boolean mainStack) {
        mService = service;
        mContext = context;
        mMainStack = mainStack;
        PowerManager pm =
            (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mGoingToSleep = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActivityManager-Sleep");
        mLaunchingActivity = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActivityManager-Launch");
        mLaunchingActivity.setReferenceCounted(false);
    }
    
    final ActivityRecord topRunningActivityLocked(ActivityRecord notTop) {
        int i = mHistory.size()-1;
        while (i >= 0) {
            ActivityRecord r = mHistory.get(i);
            if (!r.finishing && r != notTop) {
                return r;
            }
            i--;
        }
        return null;
    }

    final ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        int i = mHistory.size()-1;
        while (i >= 0) {
            ActivityRecord r = mHistory.get(i);
            if (!r.finishing && !r.delayedResume && r != notTop) {
                return r;
            }
            i--;
        }
        return null;
    }

    /**
     * This is a simplified version of topRunningActivityLocked that provides a number of
     * optional skip-over modes.  It is intended for use with the ActivityController hook only.
     * 
     * @param token If non-null, any history records matching this token will be skipped.
     * @param taskId If non-zero, we'll attempt to skip over records with the same task ID.
     * 
     * @return Returns the HistoryRecord of the next activity on the stack.
     */
    final ActivityRecord topRunningActivityLocked(IBinder token, int taskId) {
        int i = mHistory.size()-1;
        while (i >= 0) {
            ActivityRecord r = mHistory.get(i);
            // Note: the taskId check depends on real taskId fields being non-zero
            if (!r.finishing && (token != r.appToken) && (taskId != r.task.taskId)) {
                return r;
            }
            i--;
        }
        return null;
    }

    final int indexOfTokenLocked(IBinder token) {
        return mHistory.indexOf(ActivityRecord.forToken(token));
    }

    final int indexOfActivityLocked(ActivityRecord r) {
        return mHistory.indexOf(r);
    }

    final ActivityRecord isInStackLocked(IBinder token) {
        ActivityRecord r = ActivityRecord.forToken(token);
        if (mHistory.contains(r)) {
            return r;
        }
        return null;
    }

    private final boolean updateLRUListLocked(ActivityRecord r) {
        final boolean hadit = mLRUActivities.remove(r);
        mLRUActivities.add(r);
        return hadit;
    }

    /**
     * Returns the top activity in any existing task matching the given
     * Intent.  Returns null if no such task is found.
     */
    private ActivityRecord findTaskLocked(Intent intent, ActivityInfo info) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }

        TaskRecord cp = null;

        final int N = mHistory.size();
        for (int i=(N-1); i>=0; i--) {
            ActivityRecord r = mHistory.get(i);
            if (!r.finishing && r.task != cp
                    && r.launchMode != ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                cp = r.task;
                //Slog.i(TAG, "Comparing existing cls=" + r.task.intent.getComponent().flattenToShortString()
                //        + "/aff=" + r.task.affinity + " to new cls="
                //        + intent.getComponent().flattenToShortString() + "/aff=" + taskAffinity);
                if (r.task.affinity != null) {
                    if (r.task.affinity.equals(info.taskAffinity)) {
                        //Slog.i(TAG, "Found matching affinity!");
                        return r;
                    }
                } else if (r.task.intent != null
                        && r.task.intent.getComponent().equals(cls)) {
                    //Slog.i(TAG, "Found matching class!");
                    //dump();
                    //Slog.i(TAG, "For Intent " + intent + " bringing to top: " + r.intent);
                    return r;
                } else if (r.task.affinityIntent != null
                        && r.task.affinityIntent.getComponent().equals(cls)) {
                    //Slog.i(TAG, "Found matching class!");
                    //dump();
                    //Slog.i(TAG, "For Intent " + intent + " bringing to top: " + r.intent);
                    return r;
                }
            }
        }

        return null;
    }

    /**
     * Returns the first activity (starting from the top of the stack) that
     * is the same as the given activity.  Returns null if no such activity
     * is found.
     */
    private ActivityRecord findActivityLocked(Intent intent, ActivityInfo info) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }

        final int N = mHistory.size();
        for (int i=(N-1); i>=0; i--) {
            ActivityRecord r = mHistory.get(i);
            if (!r.finishing) {
                if (r.intent.getComponent().equals(cls)) {
                    //Slog.i(TAG, "Found matching class!");
                    //dump();
                    //Slog.i(TAG, "For Intent " + intent + " bringing to top: " + r.intent);
                    return r;
                }
            }
        }

        return null;
    }

    final void showAskCompatModeDialogLocked(ActivityRecord r) {
        Message msg = Message.obtain();
        msg.what = ActivityManagerService.SHOW_COMPAT_MODE_DIALOG_MSG;
        msg.obj = r.task.askedCompatMode ? null : r;
        mService.mHandler.sendMessage(msg);
    }

    final boolean realStartActivityLocked(ActivityRecord r,
            ProcessRecord app, boolean andResume, boolean checkConfig)
            throws RemoteException {

        r.startFreezingScreenLocked(app, 0);
        mService.mWindowManager.setAppVisibility(r.appToken, true);

        // schedule launch ticks to collect information about slow apps.
        r.startLaunchTickingLocked();

        // Have the window manager re-evaluate the orientation of
        // the screen based on the new activity order.  Note that
        // as a result of this, it can call back into the activity
        // manager with a new orientation.  We don't care about that,
        // because the activity is not currently running so we are
        // just restarting it anyway.
        if (checkConfig) {
            Configuration config = mService.mWindowManager.updateOrientationFromAppTokens(
                    mService.mConfiguration,
                    r.mayFreezeScreenLocked(app) ? r.appToken : null);
            mService.updateConfigurationLocked(config, r, false, false);
        }

        r.app = app;
        app.waitingToKill = null;

        if (localLOGV) Slog.v(TAG, "Launching: " + r);

        int idx = app.activities.indexOf(r);
        if (idx < 0) {
            app.activities.add(r);
        }
        mService.updateLruProcessLocked(app, true, true);

        try {
            if (app.thread == null) {
                throw new RemoteException();
            }
            List<ResultInfo> results = null;
            List<Intent> newIntents = null;
            if (andResume) {
                results = r.results;
                newIntents = r.newIntents;
            }
            if (DEBUG_SWITCH) Slog.v(TAG, "Launching: " + r
                    + " icicle=" + r.icicle
                    + " with results=" + results + " newIntents=" + newIntents
                    + " andResume=" + andResume);
            if (andResume) {
                EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY,
                        System.identityHashCode(r),
                        r.task.taskId, r.shortComponentName);
            }
            if (r.isHomeActivity) {
                mService.mHomeProcess = app;
            }
            mService.ensurePackageDexOpt(r.intent.getComponent().getPackageName());
            r.sleeping = false;
            r.forceNewConfig = false;
            showAskCompatModeDialogLocked(r);
            r.compat = mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
            String profileFile = null;
            ParcelFileDescriptor profileFd = null;
            boolean profileAutoStop = false;
            if (mService.mProfileApp != null && mService.mProfileApp.equals(app.processName)) {
                if (mService.mProfileProc == null || mService.mProfileProc == app) {
                    mService.mProfileProc = app;
                    profileFile = mService.mProfileFile;
                    profileFd = mService.mProfileFd;
                    profileAutoStop = mService.mAutoStopProfiler;
                }
            }
            app.hasShownUi = true;
            app.pendingUiClean = true;
            if (profileFd != null) {
                try {
                    profileFd = profileFd.dup();
                } catch (IOException e) {
                    profileFd = null;
                }
            }
            app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
                    System.identityHashCode(r), r.info,
                    new Configuration(mService.mConfiguration),
                    r.compat, r.icicle, results, newIntents, !andResume,
                    mService.isNextTransitionForward(), profileFile, profileFd,
                    profileAutoStop);
            
            if ((app.info.flags&ApplicationInfo.FLAG_CANT_SAVE_STATE) != 0) {
                // This may be a heavy-weight process!  Note that the package
                // manager will ensure that only activity can run in the main
                // process of the .apk, which is the only thing that will be
                // considered heavy-weight.
                if (app.processName.equals(app.info.packageName)) {
                    if (mService.mHeavyWeightProcess != null
                            && mService.mHeavyWeightProcess != app) {
                        Log.w(TAG, "Starting new heavy weight process " + app
                                + " when already running "
                                + mService.mHeavyWeightProcess);
                    }
                    mService.mHeavyWeightProcess = app;
                    Message msg = mService.mHandler.obtainMessage(
                            ActivityManagerService.POST_HEAVY_NOTIFICATION_MSG);
                    msg.obj = r;
                    mService.mHandler.sendMessage(msg);
                }
            }
            
        } catch (RemoteException e) {
            if (r.launchFailed) {
                // This is the second time we failed -- finish activity
                // and give up.
                Slog.e(TAG, "Second failure launching "
                      + r.intent.getComponent().flattenToShortString()
                      + ", giving up", e);
                mService.appDiedLocked(app, app.pid, app.thread);
                requestFinishActivityLocked(r.appToken, Activity.RESULT_CANCELED, null,
                        "2nd-crash");
                return false;
            }

            // This is the first time we failed -- restart process and
            // retry.
            app.activities.remove(r);
            throw e;
        }

        r.launchFailed = false;
        if (updateLRUListLocked(r)) {
            Slog.w(TAG, "Activity " + r
                  + " being launched, but already in LRU list");
        }

        if (andResume) {
            // As part of the process of launching, ActivityThread also performs
            // a resume.
            r.state = ActivityState.RESUMED;
            if (DEBUG_STATES) Slog.v(TAG, "Moving to RESUMED: " + r
                    + " (starting new instance)");
            r.stopped = false;
            mResumedActivity = r;
            r.task.touchActiveTime();
            if (mMainStack) {
                mService.addRecentTaskLocked(r.task);
            }
            completeResumeLocked(r);
            checkReadyForSleepLocked();
            if (DEBUG_SAVED_STATE) Slog.i(TAG, "Launch completed; removing icicle of " + r.icicle);
            r.icicle = null;
            r.haveState = false;
        } else {
            // This activity is not starting in the resumed state... which
            // should look like we asked it to pause+stop (but remain visible),
            // and it has done so and reported back the current icicle and
            // other state.
            if (DEBUG_STATES) Slog.v(TAG, "Moving to STOPPED: " + r
                    + " (starting in stopped state)");
            r.state = ActivityState.STOPPED;
            r.stopped = true;
        }

        // Launch the new version setup screen if needed.  We do this -after-
        // launching the initial activity (that is, home), so that it can have
        // a chance to initialize itself while in the background, making the
        // switch back to it faster and look better.
        if (mMainStack) {
            mService.startSetupActivityLocked();
        }
        
        return true;
    }

    private final void startSpecificActivityLocked(ActivityRecord r,
            boolean andResume, boolean checkConfig) {
        // Is this activity's application already running?
        ProcessRecord app = mService.getProcessRecordLocked(r.processName,
                r.info.applicationInfo.uid);
        
        if (r.launchTime == 0) {
            r.launchTime = SystemClock.uptimeMillis();
            if (mInitialStartTime == 0) {
                mInitialStartTime = r.launchTime;
            }
        } else if (mInitialStartTime == 0) {
            mInitialStartTime = SystemClock.uptimeMillis();
        }
        
        if (app != null && app.thread != null) {
            try {
                app.addPackage(r.info.packageName);
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting activity "
                        + r.intent.getComponent().flattenToShortString(), e);
            }

            // If a dead object exception was thrown -- fall through to
            // restart the application.
        }

        mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
                "activity", r.intent.getComponent(), false);
    }
    
    void stopIfSleepingLocked() {
        if (mService.isSleeping()) {
            if (!mGoingToSleep.isHeld()) {
                mGoingToSleep.acquire();
                if (mLaunchingActivity.isHeld()) {
                    mLaunchingActivity.release();
                    mService.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                }
            }
            mHandler.removeMessages(SLEEP_TIMEOUT_MSG);
            Message msg = mHandler.obtainMessage(SLEEP_TIMEOUT_MSG);
            mHandler.sendMessageDelayed(msg, SLEEP_TIMEOUT);
            checkReadyForSleepLocked();
        }
    }

    void awakeFromSleepingLocked() {
        mHandler.removeMessages(SLEEP_TIMEOUT_MSG);
        mSleepTimeout = false;
        if (mGoingToSleep.isHeld()) {
            mGoingToSleep.release();
        }
        // Ensure activities are no longer sleeping.
        for (int i=mHistory.size()-1; i>=0; i--) {
            ActivityRecord r = mHistory.get(i);
            r.setSleeping(false);
        }
        mGoingToSleepActivities.clear();
    }

    void activitySleptLocked(ActivityRecord r) {
        mGoingToSleepActivities.remove(r);
        checkReadyForSleepLocked();
    }

    void checkReadyForSleepLocked() {
        if (!mService.isSleeping()) {
            // Do not care.
            return;
        }

        if (!mSleepTimeout) {
            if (mResumedActivity != null) {
                // Still have something resumed; can't sleep until it is paused.
                if (DEBUG_PAUSE) Slog.v(TAG, "Sleep needs to pause " + mResumedActivity);
                if (DEBUG_USER_LEAVING) Slog.v(TAG, "Sleep => pause with userLeaving=false");
                startPausingLocked(false, true);
                return;
            }
            if (mPausingActivity != null) {
                // Still waiting for something to pause; can't sleep yet.
                if (DEBUG_PAUSE) Slog.v(TAG, "Sleep still waiting to pause " + mPausingActivity);
                return;
            }

            if (mStoppingActivities.size() > 0) {
                // Still need to tell some activities to stop; can't sleep yet.
                if (DEBUG_PAUSE) Slog.v(TAG, "Sleep still need to stop "
                        + mStoppingActivities.size() + " activities");
                scheduleIdleLocked();
                return;
            }

            ensureActivitiesVisibleLocked(null, 0);

            // Make sure any stopped but visible activities are now sleeping.
            // This ensures that the activity's onStop() is called.
            for (int i=mHistory.size()-1; i>=0; i--) {
                ActivityRecord r = mHistory.get(i);
                if (r.state == ActivityState.STOPPING || r.state == ActivityState.STOPPED) {
                    r.setSleeping(true);
                }
            }

            if (mGoingToSleepActivities.size() > 0) {
                // Still need to tell some activities to sleep; can't sleep yet.
                if (DEBUG_PAUSE) Slog.v(TAG, "Sleep still need to sleep "
                        + mGoingToSleepActivities.size() + " activities");
                return;
            }
        }

        mHandler.removeMessages(SLEEP_TIMEOUT_MSG);

        if (mGoingToSleep.isHeld()) {
            mGoingToSleep.release();
        }
        if (mService.mShuttingDown) {
            mService.notifyAll();
        }
    }
    
    public final Bitmap screenshotActivities(ActivityRecord who) {
        if (who.noDisplay) {
            return null;
        }
        
        Resources res = mService.mContext.getResources();
        int w = mThumbnailWidth;
        int h = mThumbnailHeight;
        if (w < 0) {
            mThumbnailWidth = w =
                res.getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_width);
            mThumbnailHeight = h =
                res.getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_height);
        }

        if (w > 0) {
            return mService.mWindowManager.screenshotApplications(who.appToken, w, h);
        }
        return null;
    }

    private final void startPausingLocked(boolean userLeaving, boolean uiSleeping) {
        if (mPausingActivity != null) {
            RuntimeException e = new RuntimeException();
            Slog.e(TAG, "Trying to pause when pause is already pending for "
                  + mPausingActivity, e);
        }
        ActivityRecord prev = mResumedActivity;
        if (prev == null) {
            RuntimeException e = new RuntimeException();
            Slog.e(TAG, "Trying to pause when nothing is resumed", e);
            resumeTopActivityLocked(null);
            return;
        }
        if (DEBUG_STATES) Slog.v(TAG, "Moving to PAUSING: " + prev);
        else if (DEBUG_PAUSE) Slog.v(TAG, "Start pausing: " + prev);
        mResumedActivity = null;
        mPausingActivity = prev;
        mLastPausedActivity = prev;
        prev.state = ActivityState.PAUSING;
        prev.task.touchActiveTime();
        prev.updateThumbnail(screenshotActivities(prev), null);

        mService.updateCpuStats();
        
        if (prev.app != null && prev.app.thread != null) {
            if (DEBUG_PAUSE) Slog.v(TAG, "Enqueueing pending pause: " + prev);
            try {
                EventLog.writeEvent(EventLogTags.AM_PAUSE_ACTIVITY,
                        System.identityHashCode(prev),
                        prev.shortComponentName);
                prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing,
                        userLeaving, prev.configChangeFlags);
                if (mMainStack) {
                    mService.updateUsageStats(prev, false);
                }
            } catch (Exception e) {
                // Ignore exception, if process died other code will cleanup.
                Slog.w(TAG, "Exception thrown during pause", e);
                mPausingActivity = null;
                mLastPausedActivity = null;
            }
        } else {
            mPausingActivity = null;
            mLastPausedActivity = null;
        }

        // If we are not going to sleep, we want to ensure the device is
        // awake until the next activity is started.
        if (!mService.mSleeping && !mService.mShuttingDown) {
            mLaunchingActivity.acquire();
            if (!mHandler.hasMessages(LAUNCH_TIMEOUT_MSG)) {
                // To be safe, don't allow the wake lock to be held for too long.
                Message msg = mHandler.obtainMessage(LAUNCH_TIMEOUT_MSG);
                mHandler.sendMessageDelayed(msg, LAUNCH_TIMEOUT);
            }
        }


        if (mPausingActivity != null) {
            // Have the window manager pause its key dispatching until the new
            // activity has started.  If we're pausing the activity just because
            // the screen is being turned off and the UI is sleeping, don't interrupt
            // key dispatch; the same activity will pick it up again on wakeup.
            if (!uiSleeping) {
                prev.pauseKeyDispatchingLocked();
            } else {
                if (DEBUG_PAUSE) Slog.v(TAG, "Key dispatch not paused for screen off");
            }

            // Schedule a pause timeout in case the app doesn't respond.
            // We don't give it much time because this directly impacts the
            // responsiveness seen by the user.
            Message msg = mHandler.obtainMessage(PAUSE_TIMEOUT_MSG);
            msg.obj = prev;
            prev.pauseTime = SystemClock.uptimeMillis();
            mHandler.sendMessageDelayed(msg, PAUSE_TIMEOUT);
            if (DEBUG_PAUSE) Slog.v(TAG, "Waiting for pause to complete...");
        } else {
            // This activity failed to schedule the
            // pause, so just treat it as being paused now.
            if (DEBUG_PAUSE) Slog.v(TAG, "Activity not running, resuming next.");
            resumeTopActivityLocked(null);
        }
    }
    
    final void activityPaused(IBinder token, boolean timeout) {
        if (DEBUG_PAUSE) Slog.v(
            TAG, "Activity paused: token=" + token + ", timeout=" + timeout);

        ActivityRecord r = null;

        synchronized (mService) {
            int index = indexOfTokenLocked(token);
            if (index >= 0) {
                r = mHistory.get(index);
                mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
                if (mPausingActivity == r) {
                    if (DEBUG_STATES) Slog.v(TAG, "Moving to PAUSED: " + r
                            + (timeout ? " (due to timeout)" : " (pause complete)"));
                    r.state = ActivityState.PAUSED;
                    completePauseLocked();
                } else {
                    EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE,
                            System.identityHashCode(r), r.shortComponentName, 
                            mPausingActivity != null
                                ? mPausingActivity.shortComponentName : "(none)");
                }
            }
        }
    }

    final void activityStoppedLocked(ActivityRecord r, Bundle icicle, Bitmap thumbnail,
            CharSequence description) {
        if (DEBUG_SAVED_STATE) Slog.i(TAG, "Saving icicle of " + r + ": " + icicle);
        r.icicle = icicle;
        r.haveState = true;
        r.updateThumbnail(thumbnail, description);
        r.stopped = true;
        if (DEBUG_STATES) Slog.v(TAG, "Moving to STOPPED: " + r + " (stop complete)");
        r.state = ActivityState.STOPPED;
        if (!r.finishing) {
            if (r.configDestroy) {
                destroyActivityLocked(r, true, false, "stop-config");
                resumeTopActivityLocked(null);
            } else {
                // Now that this process has stopped, we may want to consider
                // it to be the previous app to try to keep around in case
                // the user wants to return to it.
                ProcessRecord fgApp = null;
                if (mResumedActivity != null) {
                    fgApp = mResumedActivity.app;
                } else if (mPausingActivity != null) {
                    fgApp = mPausingActivity.app;
                }
                if (r.app != null && fgApp != null && r.app != fgApp
                        && r.lastVisibleTime > mService.mPreviousProcessVisibleTime
                        && r.app != mService.mHomeProcess) {
                    mService.mPreviousProcess = r.app;
                    mService.mPreviousProcessVisibleTime = r.lastVisibleTime;
                }
            }
        }
    }

    private final void completePauseLocked() {
        ActivityRecord prev = mPausingActivity;
        if (DEBUG_PAUSE) Slog.v(TAG, "Complete pause: " + prev);
        
        if (prev != null) {
            if (prev.finishing) {
                if (DEBUG_PAUSE) Slog.v(TAG, "Executing finish of activity: " + prev);
                prev = finishCurrentActivityLocked(prev, FINISH_AFTER_VISIBLE);
            } else if (prev.app != null) {
                if (DEBUG_PAUSE) Slog.v(TAG, "Enqueueing pending stop: " + prev);
                if (prev.waitingVisible) {
                    prev.waitingVisible = false;
                    mWaitingVisibleActivities.remove(prev);
                    if (DEBUG_SWITCH || DEBUG_PAUSE) Slog.v(
                            TAG, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.configDestroy) {
                    // The previous is being paused because the configuration
                    // is changing, which means it is actually stopping...
                    // To juggle the fact that we are also starting a new
                    // instance right now, we need to first completely stop
                    // the current instance before starting the new one.
                    if (DEBUG_PAUSE) Slog.v(TAG, "Destroying after pause: " + prev);
                    destroyActivityLocked(prev, true, false, "pause-config");
                } else {
                    mStoppingActivities.add(prev);
                    if (mStoppingActivities.size() > 3) {
                        // If we already have a few activities waiting to stop,
                        // then give up on things going idle and start clearing
                        // them out.
                        if (DEBUG_PAUSE) Slog.v(TAG, "To many pending stops, forcing idle");
                        scheduleIdleLocked();
                    } else {
                        checkReadyForSleepLocked();
                    }
                }
            } else {
                if (DEBUG_PAUSE) Slog.v(TAG, "App died during pause, not stopping: " + prev);
                prev = null;
            }
            mPausingActivity = null;
        }

        if (!mService.isSleeping()) {
            resumeTopActivityLocked(prev);
        } else {
            checkReadyForSleepLocked();
        }
        
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
        }

        if (prev.app != null && prev.cpuTimeAtResume > 0
                && mService.mBatteryStatsService.isOnBattery()) {
            long diff = 0;
            synchronized (mService.mProcessStatsThread) {
                diff = mService.mProcessStats.getCpuTimeForPid(prev.app.pid)
                        - prev.cpuTimeAtResume;
            }
            if (diff > 0) {
                BatteryStatsImpl bsi = mService.mBatteryStatsService.getActiveStatistics();
                synchronized (bsi) {
                    BatteryStatsImpl.Uid.Proc ps =
                            bsi.getProcessStatsLocked(prev.info.applicationInfo.uid,
                            prev.info.packageName);
                    if (ps != null) {
                        ps.addForegroundTimeLocked(diff);
                    }
                }
            }
        }
        prev.cpuTimeAtResume = 0; // reset it
    }

    /**
     * Once we know that we have asked an application to put an activity in
     * the resumed state (either by launching it or explicitly telling it),
     * this function updates the rest of our state to match that fact.
     */
    private final void completeResumeLocked(ActivityRecord next) {
        next.idle = false;
        next.results = null;
        next.newIntents = null;

        // schedule an idle timeout in case the app doesn't do it for us.
        Message msg = mHandler.obtainMessage(IDLE_TIMEOUT_MSG);
        msg.obj = next;
        mHandler.sendMessageDelayed(msg, IDLE_TIMEOUT);

        if (false) {
            // The activity was never told to pause, so just keep
            // things going as-is.  To maintain our own state,
            // we need to emulate it coming back and saying it is
            // idle.
            msg = mHandler.obtainMessage(IDLE_NOW_MSG);
            msg.obj = next;
            mHandler.sendMessage(msg);
        }

        if (mMainStack) {
            mService.reportResumedActivityLocked(next);
        }
        
        next.clearThumbnail();
        if (mMainStack) {
            mService.setFocusedActivityLocked(next);
        }
        next.resumeKeyDispatchingLocked();
        ensureActivitiesVisibleLocked(null, 0);
        mService.mWindowManager.executeAppTransition();
        mNoAnimActivities.clear();

        // Mark the point when the activity is resuming
        // TODO: To be more accurate, the mark should be before the onCreate,
        //       not after the onResume. But for subsequent starts, onResume is fine.
        if (next.app != null) {
            synchronized (mService.mProcessStatsThread) {
                next.cpuTimeAtResume = mService.mProcessStats.getCpuTimeForPid(next.app.pid);
            }
        } else {
            next.cpuTimeAtResume = 0; // Couldn't get the cpu time of process
        }
    }

    /**
     * Make sure that all activities that need to be visible (that is, they
     * currently can be seen by the user) actually are.
     */
    final void ensureActivitiesVisibleLocked(ActivityRecord top,
            ActivityRecord starting, String onlyThisProcess, int configChanges) {
        if (DEBUG_VISBILITY) Slog.v(
                TAG, "ensureActivitiesVisible behind " + top
                + " configChanges=0x" + Integer.toHexString(configChanges));

        // If the top activity is not fullscreen, then we need to
        // make sure any activities under it are now visible.
        final int count = mHistory.size();
        int i = count-1;
        while (mHistory.get(i) != top) {
            i--;
        }
        ActivityRecord r;
        boolean behindFullscreen = false;
        for (; i>=0; i--) {
            r = mHistory.get(i);
            if (DEBUG_VISBILITY) Slog.v(
                    TAG, "Make visible? " + r + " finishing=" + r.finishing
                    + " state=" + r.state);
            if (r.finishing) {
                continue;
            }
            
            final boolean doThisProcess = onlyThisProcess == null
                    || onlyThisProcess.equals(r.processName);
            
            // First: if this is not the current activity being started, make
            // sure it matches the current configuration.
            if (r != starting && doThisProcess) {
                ensureActivityConfigurationLocked(r, 0);
            }
            
            if (r.app == null || r.app.thread == null) {
                if (onlyThisProcess == null
                        || onlyThisProcess.equals(r.processName)) {
                    // This activity needs to be visible, but isn't even
                    // running...  get it started, but don't resume it
                    // at this point.
                    if (DEBUG_VISBILITY) Slog.v(
                            TAG, "Start and freeze screen for " + r);
                    if (r != starting) {
                        r.startFreezingScreenLocked(r.app, configChanges);
                    }
                    if (!r.visible) {
                        if (DEBUG_VISBILITY) Slog.v(
                                TAG, "Starting and making visible: " + r);
                        mService.mWindowManager.setAppVisibility(r.appToken, true);
                    }
                    if (r != starting) {
                        startSpecificActivityLocked(r, false, false);
                    }
                }

            } else if (r.visible) {
                // If this activity is already visible, then there is nothing
                // else to do here.
                if (DEBUG_VISBILITY) Slog.v(
                        TAG, "Skipping: already visible at " + r);
                r.stopFreezingScreenLocked(false);

            } else if (onlyThisProcess == null) {
                // This activity is not currently visible, but is running.
                // Tell it to become visible.
                r.visible = true;
                if (r.state != ActivityState.RESUMED && r != starting) {
                    // If this activity is paused, tell it
                    // to now show its window.
                    if (DEBUG_VISBILITY) Slog.v(
                            TAG, "Making visible and scheduling visibility: " + r);
                    try {
                        mService.mWindowManager.setAppVisibility(r.appToken, true);
                        r.sleeping = false;
                        r.app.pendingUiClean = true;
                        r.app.thread.scheduleWindowVisibility(r.appToken, true);
                        r.stopFreezingScreenLocked(false);
                    } catch (Exception e) {
                        // Just skip on any failure; we'll make it
                        // visible when it next restarts.
                        Slog.w(TAG, "Exception thrown making visibile: "
                                + r.intent.getComponent(), e);
                    }
                }
            }

            // Aggregate current change flags.
            configChanges |= r.configChangeFlags;

            if (r.fullscreen) {
                // At this point, nothing else needs to be shown
                if (DEBUG_VISBILITY) Slog.v(
                        TAG, "Stopping: fullscreen at " + r);
                behindFullscreen = true;
                i--;
                break;
            }
        }

        // Now for any activities that aren't visible to the user, make
        // sure they no longer are keeping the screen frozen.
        while (i >= 0) {
            r = mHistory.get(i);
            if (DEBUG_VISBILITY) Slog.v(
                    TAG, "Make invisible? " + r + " finishing=" + r.finishing
                    + " state=" + r.state
                    + " behindFullscreen=" + behindFullscreen);
            if (!r.finishing) {
                if (behindFullscreen) {
                    if (r.visible) {
                        if (DEBUG_VISBILITY) Slog.v(
                                TAG, "Making invisible: " + r);
                        r.visible = false;
                        try {
                            mService.mWindowManager.setAppVisibility(r.appToken, false);
                            if ((r.state == ActivityState.STOPPING
                                    || r.state == ActivityState.STOPPED)
                                    && r.app != null && r.app.thread != null) {
                                if (DEBUG_VISBILITY) Slog.v(
                                        TAG, "Scheduling invisibility: " + r);
                                r.app.thread.scheduleWindowVisibility(r.appToken, false);
                            }
                        } catch (Exception e) {
                            // Just skip on any failure; we'll make it
                            // visible when it next restarts.
                            Slog.w(TAG, "Exception thrown making hidden: "
                                    + r.intent.getComponent(), e);
                        }
                    } else {
                        if (DEBUG_VISBILITY) Slog.v(
                                TAG, "Already invisible: " + r);
                    }
                } else if (r.fullscreen) {
                    if (DEBUG_VISBILITY) Slog.v(
                            TAG, "Now behindFullscreen: " + r);
                    behindFullscreen = true;
                }
            }
            i--;
        }
    }

    /**
     * Version of ensureActivitiesVisible that can easily be called anywhere.
     */
    final void ensureActivitiesVisibleLocked(ActivityRecord starting,
            int configChanges) {
        ActivityRecord r = topRunningActivityLocked(null);
        if (r != null) {
            ensureActivitiesVisibleLocked(r, starting, null, configChanges);
        }
    }
    
    /**
     * Ensure that the top activity in the stack is resumed.
     *
     * @param prev The previously resumed activity, for when in the process
     * of pausing; can be null to call from elsewhere.
     *
     * @return Returns true if something is being resumed, or false if
     * nothing happened.
     */
    final boolean resumeTopActivityLocked(ActivityRecord prev) {
        // Find the first activity that is not finishing.
        ActivityRecord next = topRunningActivityLocked(null);

        // Remember how we'll process this pause/resume situation, and ensure
        // that the state is reset however we wind up proceeding.
        final boolean userLeaving = mUserLeaving;
        mUserLeaving = false;

        if (next == null) {
            // There are no more activities!  Let's just start up the
            // Launcher...
            if (mMainStack) {
                return mService.startHomeActivityLocked();
            }
        }

        next.delayedResume = false;
        
        // If the top activity is the resumed one, nothing to do.
        if (mResumedActivity == next && next.state == ActivityState.RESUMED) {
            // Make sure we have executed any pending transitions, since there
            // should be nothing left to do at this point.
            mService.mWindowManager.executeAppTransition();
            mNoAnimActivities.clear();
            return false;
        }

        // If we are sleeping, and there is no resumed activity, and the top
        // activity is paused, well that is the state we want.
        if ((mService.mSleeping || mService.mShuttingDown)
                && mLastPausedActivity == next
                && (next.state == ActivityState.PAUSED
                    || next.state == ActivityState.STOPPED
                    || next.state == ActivityState.STOPPING)) {
            // Make sure we have executed any pending transitions, since there
            // should be nothing left to do at this point.
            mService.mWindowManager.executeAppTransition();
            mNoAnimActivities.clear();
            return false;
        }
        
        // The activity may be waiting for stop, but that is no longer
        // appropriate for it.
        mStoppingActivities.remove(next);
        mGoingToSleepActivities.remove(next);
        next.sleeping = false;
        mWaitingVisibleActivities.remove(next);

        if (DEBUG_SWITCH) Slog.v(TAG, "Resuming " + next);

        // If we are currently pausing an activity, then don't do anything
        // until that is done.
        if (mPausingActivity != null) {
            if (DEBUG_SWITCH) Slog.v(TAG, "Skip resume: pausing=" + mPausingActivity);
            return false;
        }

        // Okay we are now going to start a switch, to 'next'.  We may first
        // have to pause the current activity, but this is an important point
        // where we have decided to go to 'next' so keep track of that.
        // XXX "App Redirected" dialog is getting too many false positives
        // at this point, so turn off for now.
        if (false) {
            if (mLastStartedActivity != null && !mLastStartedActivity.finishing) {
                long now = SystemClock.uptimeMillis();
                final boolean inTime = mLastStartedActivity.startTime != 0
                        && (mLastStartedActivity.startTime + START_WARN_TIME) >= now;
                final int lastUid = mLastStartedActivity.info.applicationInfo.uid;
                final int nextUid = next.info.applicationInfo.uid;
                if (inTime && lastUid != nextUid
                        && lastUid != next.launchedFromUid
                        && mService.checkPermission(
                                android.Manifest.permission.STOP_APP_SWITCHES,
                                -1, next.launchedFromUid)
                        != PackageManager.PERMISSION_GRANTED) {
                    mService.showLaunchWarningLocked(mLastStartedActivity, next);
                } else {
                    next.startTime = now;
                    mLastStartedActivity = next;
                }
            } else {
                next.startTime = SystemClock.uptimeMillis();
                mLastStartedActivity = next;
            }
        }
        
        // We need to start pausing the current activity so the top one
        // can be resumed...
        if (mResumedActivity != null) {
            if (DEBUG_SWITCH) Slog.v(TAG, "Skip resume: need to start pausing");
            startPausingLocked(userLeaving, false);
            return true;
        }

        if (prev != null && prev != next) {
            if (!prev.waitingVisible && next != null && !next.nowVisible) {
                prev.waitingVisible = true;
                mWaitingVisibleActivities.add(prev);
                if (DEBUG_SWITCH) Slog.v(
                        TAG, "Resuming top, waiting visible to hide: " + prev);
            } else {
                // The next activity is already visible, so hide the previous
                // activity's windows right now so we can show the new one ASAP.
                // We only do this if the previous is finishing, which should mean
                // it is on top of the one being resumed so hiding it quickly
                // is good.  Otherwise, we want to do the normal route of allowing
                // the resumed activity to be shown so we can decide if the
                // previous should actually be hidden depending on whether the
                // new one is found to be full-screen or not.
                if (prev.finishing) {
                    mService.mWindowManager.setAppVisibility(prev.appToken, false);
                    if (DEBUG_SWITCH) Slog.v(TAG, "Not waiting for visible to hide: "
                            + prev + ", waitingVisible="
                            + (prev != null ? prev.waitingVisible : null)
                            + ", nowVisible=" + next.nowVisible);
                } else {
                    if (DEBUG_SWITCH) Slog.v(TAG, "Previous already visible but still waiting to hide: "
                        + prev + ", waitingVisible="
                        + (prev != null ? prev.waitingVisible : null)
                        + ", nowVisible=" + next.nowVisible);
                }
            }
        }

        // Launching this app's activity, make sure the app is no longer
        // considered stopped.
        try {
            AppGlobals.getPackageManager().setPackageStoppedState(
                    next.packageName, false);
        } catch (RemoteException e1) {
        } catch (IllegalArgumentException e) {
            Slog.w(TAG, "Failed trying to unstop package "
                    + next.packageName + ": " + e);
        }

        // We are starting up the next activity, so tell the window manager
        // that the previous one will be hidden soon.  This way it can know
        // to ignore it when computing the desired screen orientation.
        if (prev != null) {
            if (prev.finishing) {
                if (DEBUG_TRANSITION) Slog.v(TAG,
                        "Prepare close transition: prev=" + prev);
                if (mNoAnimActivities.contains(prev)) {
                    mService.mWindowManager.prepareAppTransition(
                            WindowManagerPolicy.TRANSIT_NONE, false);
                } else {
                    mService.mWindowManager.prepareAppTransition(prev.task == next.task
                            ? WindowManagerPolicy.TRANSIT_ACTIVITY_CLOSE
                            : WindowManagerPolicy.TRANSIT_TASK_CLOSE, false);
                }
                mService.mWindowManager.setAppWillBeHidden(prev.appToken);
                mService.mWindowManager.setAppVisibility(prev.appToken, false);
            } else {
                if (DEBUG_TRANSITION) Slog.v(TAG,
                        "Prepare open transition: prev=" + prev);
                if (mNoAnimActivities.contains(next)) {
                    mService.mWindowManager.prepareAppTransition(
                            WindowManagerPolicy.TRANSIT_NONE, false);
                } else {
                    mService.mWindowManager.prepareAppTransition(prev.task == next.task
                            ? WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN
                            : WindowManagerPolicy.TRANSIT_TASK_OPEN, false);
                }
            }
            if (false) {
                mService.mWindowManager.setAppWillBeHidden(prev.appToken);
                mService.mWindowManager.setAppVisibility(prev.appToken, false);
            }
        } else if (mHistory.size() > 1) {
            if (DEBUG_TRANSITION) Slog.v(TAG,
                    "Prepare open transition: no previous");
            if (mNoAnimActivities.contains(next)) {
                mService.mWindowManager.prepareAppTransition(
                        WindowManagerPolicy.TRANSIT_NONE, false);
            } else {
                mService.mWindowManager.prepareAppTransition(
                        WindowManagerPolicy.TRANSIT_ACTIVITY_OPEN, false);
            }
        }

        if (next.app != null && next.app.thread != null) {
            if (DEBUG_SWITCH) Slog.v(TAG, "Resume running: " + next);

            // This activity is now becoming visible.
            mService.mWindowManager.setAppVisibility(next.appToken, true);

            // schedule launch ticks to collect information about slow apps.
            next.startLaunchTickingLocked();

            ActivityRecord lastResumedActivity = mResumedActivity;
            ActivityState lastState = next.state;

            mService.updateCpuStats();
            
            if (DEBUG_STATES) Slog.v(TAG, "Moving to RESUMED: " + next + " (in existing)");
            next.state = ActivityState.RESUMED;
            mResumedActivity = next;
            next.task.touchActiveTime();
            if (mMainStack) {
                mService.addRecentTaskLocked(next.task);
            }
            mService.updateLruProcessLocked(next.app, true, true);
            updateLRUListLocked(next);

            // Have the window manager re-evaluate the orientation of
            // the screen based on the new activity order.
            boolean updated = false;
            if (mMainStack) {
                synchronized (mService) {
                    Configuration config = mService.mWindowManager.updateOrientationFromAppTokens(
                            mService.mConfiguration,
                            next.mayFreezeScreenLocked(next.app) ? next.appToken : null);
                    if (config != null) {
                        next.frozenBeforeDestroy = true;
                    }
                    updated = mService.updateConfigurationLocked(config, next, false, false);
                }
            }
            if (!updated) {
                // The configuration update wasn't able to keep the existing
                // instance of the activity, and instead started a new one.
                // We should be all done, but let's just make sure our activity
                // is still at the top and schedule another run if something
                // weird happened.
                ActivityRecord nextNext = topRunningActivityLocked(null);
                if (DEBUG_SWITCH) Slog.i(TAG,
                        "Activity config changed during resume: " + next
                        + ", new next: " + nextNext);
                if (nextNext != next) {
                    // Do over!
                    mHandler.sendEmptyMessage(RESUME_TOP_ACTIVITY_MSG);
                }
                if (mMainStack) {
                    mService.setFocusedActivityLocked(next);
                }
                ensureActivitiesVisibleLocked(null, 0);
                mService.mWindowManager.executeAppTransition();
                mNoAnimActivities.clear();
                return true;
            }
            
            try {
                // Deliver all pending results.
                ArrayList a = next.results;
                if (a != null) {
                    final int N = a.size();
                    if (!next.finishing && N > 0) {
                        if (DEBUG_RESULTS) Slog.v(
                                TAG, "Delivering results to " + next
                                + ": " + a);
                        next.app.thread.scheduleSendResult(next.appToken, a);
                    }
                }

                if (next.newIntents != null) {
                    next.app.thread.scheduleNewIntent(next.newIntents, next.appToken);
                }

    
