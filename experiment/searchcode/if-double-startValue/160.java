package com.android.server;

import android.annotation.MiuiHook;
import android.annotation.MiuiHook.MiuiHookType;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IPowerManager.Stub;
import android.os.LocalPowerManager;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.ScreenOnListener;
import com.android.internal.app.IBatteryStats;
import com.android.server.am.BatteryStatsService;
import com.android.server.pm.ShutdownThread;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class PowerManagerService extends IPowerManager.Stub
    implements LocalPowerManager, Watchdog.Monitor
{
    private static final int ALL_BRIGHT = 15;
    private static final int ALL_LIGHTS_OFF = 0;
    private static final int ANIM_SETTING_OFF = 16;
    private static final int ANIM_SETTING_ON = 1;
    static final int ANIM_STEPS = 60;
    static final int AUTOBRIGHTNESS_ANIM_STEPS = 120;

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    static final int AUTODIMNESS_ANIM_STEPS = 120;
    private static final int BATTERY_LOW_BIT = 16;
    private static final int BUTTON_BRIGHT_BIT = 4;
    static final boolean DEBUG_SCREEN_ON = false;
    private static final int DEFAULT_SCREEN_BRIGHTNESS = 192;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int FULL_WAKE_LOCK_ID = 2;
    static final int IMMEDIATE_ANIM_STEPS = 4;
    static final int INITIAL_BUTTON_BRIGHTNESS = 0;
    static final int INITIAL_KEYBOARD_BRIGHTNESS = 0;
    static final int INITIAL_SCREEN_BRIGHTNESS = 255;
    private static final int KEYBOARD_BRIGHT_BIT = 8;
    private static final int LIGHTS_MASK = 14;
    private static final int LIGHT_SENSOR_DELAY = 2000;
    private static final int LIGHT_SENSOR_OFFSET_SCALE = 8;
    private static final int LIGHT_SENSOR_RANGE_EXPANSION = 20;
    private static final int LIGHT_SENSOR_RATE = 1000000;
    private static final int LOCK_MASK = 63;
    private static final boolean LOG_PARTIAL_WL = false;
    private static final boolean LOG_TOUCH_DOWNS = true;
    private static final int LONG_DIM_TIME = 7000;
    private static final int LONG_KEYLIGHT_DELAY = 6000;
    private static final int LOW_BATTERY_THRESHOLD = 10;
    private static final int MEDIUM_KEYLIGHT_DELAY = 15000;
    private static final int NOMINAL_FRAME_TIME_MS = 16;
    static final String PARTIAL_NAME = "PowerManagerService";
    private static final int PARTIAL_WAKE_LOCK_ID = 1;
    private static final int PROXIMITY_SENSOR_DELAY = 1000;
    private static final float PROXIMITY_THRESHOLD = 5.0F;
    private static final int SCREEN_BRIGHT = 3;
    private static final int SCREEN_BRIGHT_BIT = 2;
    private static final int SCREEN_BUTTON_BRIGHT = 7;
    private static final int SCREEN_DIM = 1;
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON_BIT = 1;
    private static final int SHORT_KEYLIGHT_DELAY_DEFAULT = 6000;
    private static final String TAG = "PowerManagerService";
    private static final boolean mDebugLightAnimation;
    private static final boolean mDebugLightSensor;
    private static final boolean mDebugProximitySensor;
    private static final boolean mSpew;
    private final int MY_PID;
    private final int MY_UID;
    private IActivityManager mActivityService;
    boolean mAnimateScreenLights = true;
    private int mAnimationSetting;
    private LightsService.Light mAttentionLight;
    private boolean mAutoBrightessEnabled;
    private int[] mAutoBrightnessLevels;
    private Runnable mAutoBrightnessTask;
    private BatteryService mBatteryService;
    private IBatteryStats mBatteryStats;
    private boolean mBootCompleted = false;
    private final int[] mBroadcastQueue;
    private UnsynchronizedWakeLock mBroadcastWakeLock;
    private final int[] mBroadcastWhy;
    private int[] mButtonBacklightValues;
    private int mButtonBrightnessOverride;
    private LightsService.Light mButtonLight;
    private Context mContext;
    private int mDimDelay;
    private boolean mDimScreen;
    private boolean mDoneBooting = false;
    private Runnable mForceReenableScreenTask;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHeadless = false;
    private int mHighestLightSensorValue;
    private volatile boolean mInitComplete;
    private boolean mInitialAnimation;
    private boolean mInitialized;
    private boolean mIsDocked;
    private boolean mIsPowered;
    private int[] mKeyboardBacklightValues;
    private LightsService.Light mKeyboardLight;
    private boolean mKeyboardVisible;
    private int mKeylightDelay;
    private long mLastEventTime;
    private long mLastProximityEventTime;
    private long mLastScreenOnTime;
    private long mLastTouchDown;
    private int[] mLcdBacklightValues;
    private LightsService.Light mLcdLight;
    SensorEventListener mLightListener;
    private Sensor mLightSensor;
    private float mLightSensorAdjustSetting;
    private int mLightSensorButtonBrightness;
    private boolean mLightSensorEnabled;
    private int mLightSensorKeyboardBrightness;
    private boolean mLightSensorPendingDecrease;
    private boolean mLightSensorPendingIncrease;
    private float mLightSensorPendingValue;
    private int mLightSensorScreenBrightness;
    private float mLightSensorValue;
    private int mLightSensorWarmupTime;
    private LightsService mLightsService;
    private final LockList mLocks;
    private int mMaximumScreenOffTimeout;
    private long mNextTimeout;
    private Runnable mNotificationTask;
    private int mPartialCount;
    private volatile boolean mPokeAwakeOnSet;
    private final HashMap<IBinder, PokeLock> mPokeLocks;
    private volatile int mPokey;
    private volatile WindowManagerPolicy mPolicy;
    private int mPowerState;
    private boolean mPreparingForScreenOn;
    private boolean mPreventScreenOn;
    private UnsynchronizedWakeLock mPreventScreenOnPartialLock;
    private boolean mProxIgnoredBecauseScreenTurnedOff;
    SensorEventListener mProximityListener;
    private UnsynchronizedWakeLock mProximityPartialLock;
    private int mProximityPendingValue;
    private Sensor mProximitySensor;
    private boolean mProximitySensorActive;
    private boolean mProximitySensorEnabled;
    private Runnable mProximityTask;
    private int mProximityWakeLockCount;
    private ScreenBrightnessAnimator mScreenBrightnessAnimator;
    private int mScreenBrightnessDim;
    private Handler mScreenBrightnessHandler;
    private int mScreenBrightnessOverride;
    private int mScreenBrightnessSetting;
    private BroadcastReceiver mScreenOffBroadcastDone;
    private int mScreenOffDelay;
    private Handler mScreenOffHandler;
    private Intent mScreenOffIntent;
    private int mScreenOffReason;
    long mScreenOffStart;
    private long mScreenOffTime;
    private int mScreenOffTimeoutSetting;
    private BroadcastReceiver mScreenOnBroadcastDone;
    private Intent mScreenOnIntent;
    private WindowManagerPolicy.ScreenOnListener mScreenOnListener;
    long mScreenOnStart;
    private SensorManager mSensorManager;
    private ContentQueryMap mSettings;
    private int mShortKeylightDelay = 6000;
    private boolean mSkippedScreenOn;
    private int mStayOnConditions = 0;
    private UnsynchronizedWakeLock mStayOnWhilePluggedInPartialLock;
    private UnsynchronizedWakeLock mStayOnWhilePluggedInScreenDimLock;
    private boolean mStillNeedSleepNotification;
    private final TimeoutTask mTimeoutTask;
    private long mTotalTouchDownTime;
    private int mTouchCycles;
    boolean mUnplugTurnsOnScreen;
    private boolean mUseSoftwareAutoBrightness;
    private boolean mUserActivityAllowed;
    private int mUserState;
    private boolean mWaitingForFirstLightSensor;
    private int mWakeLockState;
    private int mWarningSpewThrottleCount;
    private long mWarningSpewThrottleTime;
    private float mWindowScaleAnimation;

    PowerManagerService()
    {
        int[] arrayOfInt = new int[3];
        arrayOfInt[0] = -1;
        arrayOfInt[1] = -1;
        arrayOfInt[2] = -1;
        this.mBroadcastQueue = arrayOfInt;
        this.mBroadcastWhy = new int[3];
        this.mPreparingForScreenOn = false;
        this.mSkippedScreenOn = false;
        this.mInitialized = false;
        this.mPartialCount = 0;
        this.mKeyboardVisible = false;
        this.mUserActivityAllowed = true;
        this.mProximityWakeLockCount = 0;
        this.mProximitySensorEnabled = false;
        this.mProximitySensorActive = false;
        this.mProximityPendingValue = -1;
        this.mMaximumScreenOffTimeout = 2147483647;
        this.mLastEventTime = 0L;
        this.mLocks = new LockList(null);
        this.mTimeoutTask = new TimeoutTask(null);
        this.mWaitingForFirstLightSensor = false;
        this.mIsPowered = false;
        this.mLightSensorValue = -1.0F;
        this.mProxIgnoredBecauseScreenTurnedOff = false;
        this.mHighestLightSensorValue = -1;
        this.mLightSensorPendingDecrease = false;
        this.mLightSensorPendingIncrease = false;
        this.mLightSensorPendingValue = -1.0F;
        this.mLightSensorAdjustSetting = 0.0F;
        this.mLightSensorScreenBrightness = -1;
        this.mLightSensorButtonBrightness = -1;
        this.mLightSensorKeyboardBrightness = -1;
        this.mDimScreen = true;
        this.mIsDocked = false;
        this.mPokey = 0;
        this.mPokeAwakeOnSet = false;
        this.mInitComplete = false;
        this.mPokeLocks = new HashMap();
        this.mScreenBrightnessSetting = 192;
        this.mScreenBrightnessOverride = -1;
        this.mButtonBrightnessOverride = -1;
        this.mAnimationSetting = 16;
        this.mScreenOnListener = new WindowManagerPolicy.ScreenOnListener()
        {
            public void onScreenOn()
            {
                synchronized (PowerManagerService.this.mLocks)
                {
                    if (PowerManagerService.this.mPreparingForScreenOn)
                    {
                        PowerManagerService.access$3102(PowerManagerService.this, false);
                        PowerManagerService.this.updateLightsLocked(PowerManagerService.this.mPowerState, 1);
                        Object[] arrayOfObject = new Object[2];
                        arrayOfObject[0] = Integer.valueOf(4);
                        arrayOfObject[1] = Integer.valueOf(PowerManagerService.this.mBroadcastWakeLock.mCount);
                        EventLog.writeEvent(2727, arrayOfObject);
                        PowerManagerService.this.mBroadcastWakeLock.release();
                    }
                    return;
                }
            }
        };
        this.mNotificationTask = new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    int i;
                    int j;
                    WindowManagerPolicy localWindowManagerPolicy;
                    synchronized (PowerManagerService.this.mLocks)
                    {
                        i = PowerManagerService.this.mBroadcastQueue[0];
                        j = PowerManagerService.this.mBroadcastWhy[0];
                        int k = 0;
                        if (k < 2)
                        {
                            PowerManagerService.this.mBroadcastQueue[k] = PowerManagerService.this.mBroadcastQueue[(k + 1)];
                            PowerManagerService.this.mBroadcastWhy[k] = PowerManagerService.this.mBroadcastWhy[(k + 1)];
                            k++;
                            continue;
                        }
                        localWindowManagerPolicy = PowerManagerService.this.getPolicyLocked();
                        if ((i == 1) && (!PowerManagerService.this.mPreparingForScreenOn))
                        {
                            PowerManagerService.access$3102(PowerManagerService.this, true);
                            PowerManagerService.this.mBroadcastWakeLock.acquire();
                            EventLog.writeEvent(2725, PowerManagerService.this.mBroadcastWakeLock.mCount);
                        }
                        if (i == 1)
                        {
                            PowerManagerService.this.mScreenOnStart = SystemClock.uptimeMillis();
                            localWindowManagerPolicy.screenTurningOn(PowerManagerService.this.mScreenOnListener);
                        }
                    }
                    try
                    {
                        ActivityManagerNative.getDefault().wakingUp();
                        label189: if ((PowerManagerService.this.mContext != null) && (ActivityManagerNative.isSystemReady()))
                        {
                            PowerManagerService.this.mContext.sendOrderedBroadcast(PowerManagerService.this.mScreenOnIntent, null, PowerManagerService.this.mScreenOnBroadcastDone, PowerManagerService.this.mHandler, 0, null, null);
                            continue;
                            localObject1 = finally;
                            throw localObject1;
                        }
                        synchronized (PowerManagerService.this.mLocks)
                        {
                            Object[] arrayOfObject2 = new Object[2];
                            arrayOfObject2[0] = Integer.valueOf(2);
                            arrayOfObject2[1] = Integer.valueOf(PowerManagerService.this.mBroadcastWakeLock.mCount);
                            EventLog.writeEvent(2727, arrayOfObject2);
                            PowerManagerService.this.mBroadcastWakeLock.release();
                        }
                        if (i == 0)
                        {
                            PowerManagerService.this.mScreenOffStart = SystemClock.uptimeMillis();
                            localWindowManagerPolicy.screenTurnedOff(j);
                        }
                        try
                        {
                            ActivityManagerNative.getDefault().goingToSleep();
                            label355: if ((PowerManagerService.this.mContext != null) && (ActivityManagerNative.isSystemReady()))
                            {
                                PowerManagerService.this.mContext.sendOrderedBroadcast(PowerManagerService.this.mScreenOffIntent, null, PowerManagerService.this.mScreenOffBroadcastDone, PowerManagerService.this.mHandler, 0, null, null);
                                continue;
                            }
                            synchronized (PowerManagerService.this.mLocks)
                            {
                                Object[] arrayOfObject1 = new Object[2];
                                arrayOfObject1[0] = Integer.valueOf(3);
                                arrayOfObject1[1] = Integer.valueOf(PowerManagerService.this.mBroadcastWakeLock.mCount);
                                EventLog.writeEvent(2727, arrayOfObject1);
                                PowerManagerService.this.updateLightsLocked(PowerManagerService.this.mPowerState, 1);
                                PowerManagerService.this.mBroadcastWakeLock.release();
                            }
                            return;
                        }
                        catch (RemoteException localRemoteException1)
                        {
                            break label355;
                        }
                    }
                    catch (RemoteException localRemoteException2)
                    {
                        break label189;
                    }
                }
            }
        };
        this.mScreenOnBroadcastDone = new BroadcastReceiver()
        {
            public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
            {
                synchronized (PowerManagerService.this.mLocks)
                {
                    Object[] arrayOfObject = new Object[3];
                    arrayOfObject[0] = Integer.valueOf(1);
                    arrayOfObject[1] = Long.valueOf(SystemClock.uptimeMillis() - PowerManagerService.this.mScreenOnStart);
                    arrayOfObject[2] = Integer.valueOf(PowerManagerService.this.mBroadcastWakeLock.mCount);
                    EventLog.writeEvent(2726, arrayOfObject);
                    PowerManagerService.this.mBroadcastWakeLock.release();
                    return;
                }
            }
        };
        this.mScreenOffBroadcastDone = new BroadcastReceiver()
        {
            public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
            {
                synchronized (PowerManagerService.this.mLocks)
                {
                    Object[] arrayOfObject = new Object[3];
                    arrayOfObject[0] = Integer.valueOf(0);
                    arrayOfObject[1] = Long.valueOf(SystemClock.uptimeMillis() - PowerManagerService.this.mScreenOffStart);
                    arrayOfObject[2] = Integer.valueOf(PowerManagerService.this.mBroadcastWakeLock.mCount);
                    EventLog.writeEvent(2726, arrayOfObject);
                    PowerManagerService.this.mBroadcastWakeLock.release();
                    return;
                }
            }
        };
        this.mForceReenableScreenTask = new Runnable()
        {
            public void run()
            {
                PowerManagerService.this.forceReenableScreen();
            }
        };
        this.mProximityTask = new Runnable()
        {
            public void run()
            {
                int j;
                for (int i = 1; ; j = 0)
                    synchronized (PowerManagerService.this.mLocks)
                    {
                        if (PowerManagerService.this.mProximityPendingValue != -1)
                        {
                            PowerManagerService localPowerManagerService = PowerManagerService.this;
                            if (PowerManagerService.this.mProximityPendingValue == i)
                            {
                                localPowerManagerService.proximityChangedLocked(i);
                                PowerManagerService.access$5902(PowerManagerService.this, -1);
                            }
                        }
                        else
                        {
                            if (PowerManagerService.this.mProximityPartialLock.isHeld())
                                PowerManagerService.this.mProximityPartialLock.release();
                            return;
                        }
                    }
            }
        };
        this.mAutoBrightnessTask = new Runnable()
        {
            public void run()
            {
                synchronized (PowerManagerService.this.mLocks)
                {
                    if ((PowerManagerService.this.mLightSensorPendingDecrease) || (PowerManagerService.this.mLightSensorPendingIncrease))
                    {
                        int i = (int)PowerManagerService.this.mLightSensorPendingValue;
                        PowerManagerService.access$6202(PowerManagerService.this, false);
                        PowerManagerService.access$6302(PowerManagerService.this, false);
                        PowerManagerService.this.lightSensorChangedLocked(i, false);
                    }
                    return;
                }
            }
        };
        this.mInitialAnimation = true;
        this.mProximityListener = new SensorEventListener()
        {
            public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt)
            {
            }

            // ERROR //
            public void onSensorChanged(SensorEvent paramAnonymousSensorEvent)
            {
                // Byte code:
                //     0: invokestatic 27	android/os/SystemClock:elapsedRealtime	()J
                //     3: lstore_2
                //     4: aload_0
                //     5: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     8: invokestatic 31	com/android/server/PowerManagerService:access$500	(Lcom/android/server/PowerManagerService;)Lcom/android/server/PowerManagerService$LockList;
                //     11: astore 4
                //     13: aload 4
                //     15: monitorenter
                //     16: aload_1
                //     17: getfield 37	android/hardware/SensorEvent:values	[F
                //     20: iconst_0
                //     21: faload
                //     22: fstore 6
                //     24: lload_2
                //     25: aload_0
                //     26: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     29: invokestatic 41	com/android/server/PowerManagerService:access$6800	(Lcom/android/server/PowerManagerService;)J
                //     32: lsub
                //     33: lstore 7
                //     35: aload_0
                //     36: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     39: lload_2
                //     40: invokestatic 45	com/android/server/PowerManagerService:access$6802	(Lcom/android/server/PowerManagerService;J)J
                //     43: pop2
                //     44: aload_0
                //     45: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     48: invokestatic 49	com/android/server/PowerManagerService:access$4000	(Lcom/android/server/PowerManagerService;)Landroid/os/Handler;
                //     51: aload_0
                //     52: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     55: invokestatic 53	com/android/server/PowerManagerService:access$6900	(Lcom/android/server/PowerManagerService;)Ljava/lang/Runnable;
                //     58: invokevirtual 59	android/os/Handler:removeCallbacks	(Ljava/lang/Runnable;)V
                //     61: iconst_0
                //     62: istore 11
                //     64: fload 6
                //     66: f2d
                //     67: dconst_0
                //     68: dcmpl
                //     69: iflt +177 -> 246
                //     72: fload 6
                //     74: ldc 60
                //     76: fcmpg
                //     77: ifge +169 -> 246
                //     80: fload 6
                //     82: aload_0
                //     83: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     86: invokestatic 64	com/android/server/PowerManagerService:access$7000	(Lcom/android/server/PowerManagerService;)Landroid/hardware/Sensor;
                //     89: invokevirtual 70	android/hardware/Sensor:getMaximumRange	()F
                //     92: fcmpg
                //     93: ifge +153 -> 246
                //     96: iconst_1
                //     97: istore 12
                //     99: lload 7
                //     101: ldc2_w 71
                //     104: lcmp
                //     105: ifge +88 -> 193
                //     108: aload_0
                //     109: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     112: astore 15
                //     114: iload 12
                //     116: ifeq +136 -> 252
                //     119: iconst_1
                //     120: istore 16
                //     122: aload 15
                //     124: iload 16
                //     126: invokestatic 76	com/android/server/PowerManagerService:access$5902	(Lcom/android/server/PowerManagerService;I)I
                //     129: pop
                //     130: aload_0
                //     131: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     134: invokestatic 49	com/android/server/PowerManagerService:access$4000	(Lcom/android/server/PowerManagerService;)Landroid/os/Handler;
                //     137: aload_0
                //     138: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     141: invokestatic 53	com/android/server/PowerManagerService:access$6900	(Lcom/android/server/PowerManagerService;)Ljava/lang/Runnable;
                //     144: ldc2_w 71
                //     147: lload 7
                //     149: lsub
                //     150: invokevirtual 80	android/os/Handler:postDelayed	(Ljava/lang/Runnable;J)Z
                //     153: pop
                //     154: iconst_1
                //     155: istore 11
                //     157: aload_0
                //     158: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     161: invokestatic 84	com/android/server/PowerManagerService:access$6100	(Lcom/android/server/PowerManagerService;)Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
                //     164: invokevirtual 90	com/android/server/PowerManagerService$UnsynchronizedWakeLock:isHeld	()Z
                //     167: istore 14
                //     169: iload 14
                //     171: ifne +52 -> 223
                //     174: iload 11
                //     176: ifeq +47 -> 223
                //     179: aload_0
                //     180: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     183: invokestatic 84	com/android/server/PowerManagerService:access$6100	(Lcom/android/server/PowerManagerService;)Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
                //     186: invokevirtual 93	com/android/server/PowerManagerService$UnsynchronizedWakeLock:acquire	()V
                //     189: aload 4
                //     191: monitorexit
                //     192: return
                //     193: aload_0
                //     194: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     197: bipush 255
                //     199: invokestatic 76	com/android/server/PowerManagerService:access$5902	(Lcom/android/server/PowerManagerService;I)I
                //     202: pop
                //     203: aload_0
                //     204: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     207: iload 12
                //     209: invokestatic 97	com/android/server/PowerManagerService:access$6000	(Lcom/android/server/PowerManagerService;Z)V
                //     212: goto -55 -> 157
                //     215: astore 5
                //     217: aload 4
                //     219: monitorexit
                //     220: aload 5
                //     222: athrow
                //     223: iload 14
                //     225: ifeq -36 -> 189
                //     228: iload 11
                //     230: ifne -41 -> 189
                //     233: aload_0
                //     234: getfield 14	com/android/server/PowerManagerService$12:this$0	Lcom/android/server/PowerManagerService;
                //     237: invokestatic 84	com/android/server/PowerManagerService:access$6100	(Lcom/android/server/PowerManagerService;)Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
                //     240: invokevirtual 100	com/android/server/PowerManagerService$UnsynchronizedWakeLock:release	()V
                //     243: goto -54 -> 189
                //     246: iconst_0
                //     247: istore 12
                //     249: goto -150 -> 99
                //     252: iconst_0
                //     253: istore 16
                //     255: goto -133 -> 122
                //
                // Exception table:
                //     from	to	target	type
                //     16	220	215	finally
                //     233	243	215	finally
            }
        };
        this.mLightListener = new SensorEventListener()
        {
            public void onAccuracyChanged(Sensor paramAnonymousSensor, int paramAnonymousInt)
            {
            }

            public void onSensorChanged(SensorEvent paramAnonymousSensorEvent)
            {
                synchronized (PowerManagerService.this.mLocks)
                {
                    if (!PowerManagerService.this.isScreenTurningOffLocked())
                    {
                        PowerManagerService.this.handleLightSensorValue((int)paramAnonymousSensorEvent.values[0], PowerManagerService.this.mWaitingForFirstLightSensor);
                        if ((PowerManagerService.this.mWaitingForFirstLightSensor) && (!PowerManagerService.this.mPreparingForScreenOn))
                            PowerManagerService.access$7202(PowerManagerService.this, false);
                    }
                }
            }
        };
        long l = Binder.clearCallingIdentity();
        this.MY_UID = Process.myUid();
        this.MY_PID = Process.myPid();
        Binder.restoreCallingIdentity(l);
        this.mPowerState = 0;
        this.mUserState = 0;
        Watchdog.getInstance().addMonitor(this);
        nativeInit();
    }

    private int applyButtonState(int paramInt)
    {
        int i = -1;
        if ((paramInt & 0x10) != 0);
        while (true)
        {
            return paramInt;
            if (this.mButtonBrightnessOverride >= 0);
            for (i = this.mButtonBrightnessOverride; ; i = this.mLightSensorButtonBrightness)
                do
                {
                    if (i <= 0)
                        break label57;
                    paramInt |= 4;
                    break;
                }
                while ((this.mLightSensorButtonBrightness < 0) || (!this.mUseSoftwareAutoBrightness));
            label57: if (i == 0)
                paramInt &= -5;
        }
    }

    private int applyKeyboardState(int paramInt)
    {
        int i = -1;
        if ((paramInt & 0x10) != 0);
        while (true)
        {
            return paramInt;
            if (!this.mKeyboardVisible)
                i = 0;
            while (true)
            {
                if (i <= 0)
                    break label70;
                paramInt |= 8;
                break;
                if (this.mButtonBrightnessOverride >= 0)
                    i = this.mButtonBrightnessOverride;
                else if ((this.mLightSensorKeyboardBrightness >= 0) && (this.mUseSoftwareAutoBrightness))
                    i = this.mLightSensorKeyboardBrightness;
            }
            label70: if (i == 0)
                paramInt &= -9;
        }
    }

    private boolean batteryIsLow()
    {
        if ((!this.mIsPowered) && (this.mBatteryService.getBatteryLevel() <= 10));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    private void cancelTimerLocked()
    {
        this.mHandler.removeCallbacks(this.mTimeoutTask);
        this.mTimeoutTask.nextState = -1;
    }

    private void disableProximityLockLocked()
    {
        long l;
        if (this.mProximitySensorEnabled)
            l = Binder.clearCallingIdentity();
        try
        {
            this.mSensorManager.unregisterListener(this.mProximityListener);
            this.mHandler.removeCallbacks(this.mProximityTask);
            if (this.mProximityPartialLock.isHeld())
                this.mProximityPartialLock.release();
            this.mProximitySensorEnabled = false;
            Binder.restoreCallingIdentity(l);
            if (this.mProximitySensorActive)
            {
                this.mProximitySensorActive = false;
                if (!this.mProxIgnoredBecauseScreenTurnedOff)
                    forceUserActivityLocked();
            }
            return;
        }
        finally
        {
            Binder.restoreCallingIdentity(l);
        }
    }

    private void dockStateChanged(int paramInt)
    {
        boolean bool = false;
        LockList localLockList = this.mLocks;
        if (paramInt != 0)
            bool = true;
        try
        {
            this.mIsDocked = bool;
            if (this.mIsDocked)
                this.mHighestLightSensorValue = -1;
            if ((0x1 & this.mPowerState) != 0)
            {
                int i = (int)this.mLightSensorValue;
                this.mLightSensorValue = -1.0F;
                lightSensorChangedLocked(i, false);
            }
            return;
        }
        finally
        {
            localObject = finally;
            throw localObject;
        }
    }

    private static String dumpPowerState(int paramInt)
    {
        StringBuilder localStringBuilder1 = new StringBuilder();
        String str1;
        String str2;
        label36: String str3;
        label55: StringBuilder localStringBuilder4;
        if ((paramInt & 0x8) != 0)
        {
            str1 = "KEYBOARD_BRIGHT_BIT ";
            StringBuilder localStringBuilder2 = localStringBuilder1.append(str1);
            if ((paramInt & 0x2) == 0)
                break label94;
            str2 = "SCREEN_BRIGHT_BIT ";
            StringBuilder localStringBuilder3 = localStringBuilder2.append(str2);
            if ((paramInt & 0x1) == 0)
                break label102;
            str3 = "SCREEN_ON_BIT ";
            localStringBuilder4 = localStringBuilder3.append(str3);
            if ((paramInt & 0x10) == 0)
                break label110;
        }
        label94: label102: label110: for (String str4 = "BATTERY_LOW_BIT "; ; str4 = "")
        {
            return str4;
            str1 = "";
            break;
            str2 = "";
            break label36;
            str3 = "";
            break label55;
        }
    }

    private void enableLightSensorLocked(boolean paramBoolean)
    {
        if (!this.mAutoBrightessEnabled)
            paramBoolean = false;
        long l;
        if ((this.mSensorManager != null) && (this.mLightSensorEnabled != paramBoolean))
        {
            this.mLightSensorEnabled = paramBoolean;
            l = Binder.clearCallingIdentity();
            if (!paramBoolean)
                break label92;
        }
        try
        {
            this.mHighestLightSensorValue = -1;
            int i = (int)this.mLightSensorValue;
            if (i >= 0)
            {
                this.mLightSensorValue = -1.0F;
                handleLightSensorValue(i, true);
            }
            this.mSensorManager.registerListener(this.mLightListener, this.mLightSensor, 1000000);
            while (true)
            {
                return;
                label92: this.mSensorManager.unregisterListener(this.mLightListener);
                this.mHandler.removeCallbacks(this.mAutoBrightnessTask);
                this.mLightSensorPendingDecrease = false;
                this.mLightSensorPendingIncrease = false;
            }
        }
        finally
        {
            Binder.restoreCallingIdentity(l);
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    private void enableProximityLockLocked()
    {
        long l;
        if (!this.mProximitySensorEnabled)
            l = Binder.clearCallingIdentity();
        while (true)
        {
            try
            {
                this.mSensorManager.registerListener(this.mProximityListener, this.mProximitySensor, 3);
                this.mProximitySensorEnabled = true;
                return;
            }
            finally
            {
                Binder.restoreCallingIdentity(l);
            }
            proximityChangedLocked(this.mProximitySensorActive);
        }
    }

    private void forceReenableScreen()
    {
        if (!this.mPreventScreenOn)
            Slog.w("PowerManagerService", "forceReenableScreen: mPreventScreenOn is false, nothing to do");
        while (true)
        {
            return;
            Slog.w("PowerManagerService", "App called preventScreenOn(true) but didn't promptly reenable the screen! Forcing the screen back on...");
            preventScreenOn(false);
        }
    }

    private void forceUserActivityLocked()
    {
        if (isScreenTurningOffLocked())
            this.mScreenBrightnessAnimator.cancelAnimation();
        boolean bool = this.mUserActivityAllowed;
        this.mUserActivityAllowed = true;
        userActivity(SystemClock.uptimeMillis(), false);
        this.mUserActivityAllowed = bool;
    }

    private int getAutoBrightnessValue(int paramInt, int[] paramArrayOfInt)
    {
        for (int i = 0; ; i++)
        {
            int j;
            try
            {
                if ((i < this.mAutoBrightnessLevels.length) && (paramInt >= this.mAutoBrightnessLevels[i]))
                    continue;
                int k = paramArrayOfInt[0];
                int m = paramArrayOfInt[this.mAutoBrightnessLevels.length];
                int n = 20 + (m - k);
                float f1 = (10 + (paramArrayOfInt[i] - k)) / n;
                float f2;
                if ((this.mLightSensorAdjustSetting > 0.0F) && (this.mLightSensorAdjustSetting <= 1.0F))
                {
                    f2 = (float)Math.sqrt(1.0F - this.mLightSensorAdjustSetting);
                    if (f2 <= 1.E-05D)
                        f1 = 1.0F;
                }
                while (true)
                {
                    j = -10 + (int)((f1 + this.mLightSensorAdjustSetting / 8.0F) * n + k);
                    if (j >= k)
                        break;
                    j = k;
                    break label229;
                    f1 /= f2;
                    continue;
                    if ((this.mLightSensorAdjustSetting < 0.0F) && (this.mLightSensorAdjustSetting >= -1.0F))
                    {
                        double d = Math.sqrt(1.0F + this.mLightSensorAdjustSetting);
                        f1 *= (float)d;
                    }
                }
                if (j > m)
                    j = m;
            }
            catch (Exception localException)
            {
                Slog.e("PowerManagerService", "Values array must be non-empty and must be one element longer than the auto-brightness levels array.    Check config.xml.", localException);
                j = 255;
            }
            label229: return j;
        }
    }

    private int getPreferredBrightness()
    {
        int i;
        if (this.mScreenBrightnessOverride >= 0)
            i = this.mScreenBrightnessOverride;
        while (true)
        {
            return i;
            if ((this.mLightSensorScreenBrightness >= 0) && (this.mUseSoftwareAutoBrightness) && (this.mAutoBrightessEnabled))
                i = this.mLightSensorScreenBrightness;
            else
                i = Math.max(this.mScreenBrightnessSetting, this.mScreenBrightnessDim);
        }
    }

    private void goToSleepLocked(long paramLong, int paramInt)
    {
        if (this.mLastEventTime <= paramLong)
        {
            this.mLastEventTime = paramLong;
            this.mWakeLockState = 0;
            int i = this.mLocks.size();
            int j = 0;
            int k = 0;
            int m = 0;
            if (m < i)
            {
                WakeLock localWakeLock = (WakeLock)this.mLocks.get(m);
                if (isScreenLock(localWakeLock.flags))
                {
                    if (((0x3F & localWakeLock.flags) != 32) || (paramInt != 4))
                        break label97;
                    k = 1;
                }
                while (true)
                {
                    m++;
                    break;
                    label97: ((WakeLock)this.mLocks.get(m)).activated = false;
                    j++;
                }
            }
            if (k == 0)
                this.mProxIgnoredBecauseScreenTurnedOff = true;
            EventLog.writeEvent(2724, j);
            this.mStillNeedSleepNotification = true;
            this.mUserState = 0;
            setPowerState(0, false, paramInt);
            cancelTimerLocked();
        }
    }

    private void handleLightSensorValue(int paramInt, boolean paramBoolean)
    {
        boolean bool1 = true;
        long l = SystemClock.elapsedRealtime();
        if ((this.mLightSensorValue == -1.0F) || (l < this.mLastScreenOnTime + this.mLightSensorWarmupTime) || (this.mWaitingForFirstLightSensor))
        {
            this.mHandler.removeCallbacks(this.mAutoBrightnessTask);
            this.mLightSensorPendingDecrease = false;
            this.mLightSensorPendingIncrease = false;
            lightSensorChangedLocked(paramInt, paramBoolean);
        }
        while (true)
        {
            return;
            if (((paramInt > this.mLightSensorValue) && (this.mLightSensorPendingDecrease)) || ((paramInt < this.mLightSensorValue) && (this.mLightSensorPendingIncrease)) || (paramInt == this.mLightSensorValue) || ((!this.mLightSensorPendingDecrease) && (!this.mLightSensorPendingIncrease)))
            {
                this.mHandler.removeCallbacks(this.mAutoBrightnessTask);
                boolean bool2;
                if (paramInt < this.mLightSensorValue)
                {
                    bool2 = bool1;
                    label151: this.mLightSensorPendingDecrease = bool2;
                    if (paramInt <= this.mLightSensorValue)
                        break label216;
                }
                while (true)
                {
                    this.mLightSensorPendingIncrease = bool1;
                    if ((!this.mLightSensorPendingDecrease) && (!this.mLightSensorPendingIncrease))
                        break;
                    this.mLightSensorPendingValue = paramInt;
                    this.mHandler.postDelayed(this.mAutoBrightnessTask, 2000L);
                    break;
                    bool2 = false;
                    break label151;
                    label216: bool1 = false;
                }
            }
            this.mLightSensorPendingValue = paramInt;
        }
    }

    private boolean isScreenLock(int paramInt)
    {
        int i = paramInt & 0x3F;
        if ((i == 26) || (i == 10) || (i == 6) || (i == 32));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    private boolean isScreenTurningOffLocked()
    {
        if ((this.mScreenBrightnessAnimator.isAnimating()) && (this.mScreenBrightnessAnimator.endValue == 0) && ((0x2 & this.mScreenBrightnessAnimator.currentMask) != 0));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    private void lightSensorChangedLocked(int paramInt, boolean paramBoolean)
    {
        if ((0x1 & this.mPowerState) == 0);
        do
        {
            return;
            if (this.mLightSensorValue == paramInt)
                break;
            this.mLightSensorValue = paramInt;
        }
        while ((0x10 & this.mPowerState) != 0);
        int i = getAutoBrightnessValue(paramInt, this.mLcdBacklightValues);
        int j = getAutoBrightnessValue(paramInt, this.mButtonBacklightValues);
        if (this.mKeyboardVisible);
        int m;
        for (int k = getAutoBrightnessValue(paramInt, this.mKeyboardBacklightValues); ; k = 0)
        {
            this.mLightSensorScreenBrightness = i;
            this.mLightSensorButtonBrightness = j;
            this.mLightSensorKeyboardBrightness = k;
            if ((this.mAutoBrightessEnabled) && (this.mScreenBrightnessOverride < 0) && (!this.mSkippedScreenOn) && (!this.mInitialAnimation))
            {
                if (!paramBoolean)
                    break label190;
                m = 4;
                this.mScreenBrightnessAnimator.animateTo(i, paramInt, 2, m * 16);
            }
            if (this.mButtonBrightnessOverride < 0)
                this.mButtonLight.setBrightness(j);
            if ((this.mButtonBrightnessOverride >= 0) && (this.mKeyboardVisible))
                break;
            this.mKeyboardLight.setBrightness(k);
            break;
            break;
        }
        while (true)
        {
            label190: synchronized (this.mScreenBrightnessAnimator)
            {
                if (this.mScreenBrightnessAnimator.currentValue <= i)
                    m = 120;
            }
            m = 120;
        }
    }

    private static String lockType(int paramInt)
    {
        String str;
        switch (paramInt)
        {
        default:
            str = "???                                                     ";
        case 26:
        case 10:
        case 6:
        case 1:
        case 32:
        }
        while (true)
        {
            return str;
            str = "FULL_WAKE_LOCK                                ";
            continue;
            str = "SCREEN_BRIGHT_WAKE_LOCK             ";
            continue;
            str = "SCREEN_DIM_WAKE_LOCK                    ";
            continue;
            str = "PARTIAL_WAKE_LOCK                         ";
            continue;
            str = "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
        }
    }

    public static void lowLevelReboot(String paramString)
        throws IOException
    {
        nativeReboot(paramString);
    }

    public static void lowLevelShutdown()
    {
        nativeShutdown();
    }

    private static native void nativeAcquireWakeLock(int paramInt, String paramString);

    private native void nativeInit();

    private static native void nativeReboot(String paramString)
        throws IOException;

    private static native void nativeReleaseWakeLock(String paramString);

    private native void nativeSetPowerState(boolean paramBoolean1, boolean paramBoolean2);

    private static native int nativeSetScreenState(boolean paramBoolean);

    private static native void nativeShutdown();

    private native void nativeStartSurfaceFlingerAnimation(int paramInt);

    private void proximityChangedLocked(boolean paramBoolean)
    {
        if (!this.mProximitySensorEnabled)
            Slog.d("PowerManagerService", "Ignoring proximity change after sensor is disabled");
        while (true)
        {
            return;
            if (paramBoolean)
            {
                if (!this.mProxIgnoredBecauseScreenTurnedOff)
                    goToSleepLocked(SystemClock.uptimeMillis(), 4);
                this.mProximitySensorActive = true;
            }
            else
            {
                this.mProximitySensorActive = false;
                if (!this.mProxIgnoredBecauseScreenTurnedOff)
                    forceUserActivityLocked();
                if (this.mProximityWakeLockCount == 0)
                    disableProximityLockLocked();
            }
        }
    }

    private void releaseWakeLockLocked(IBinder paramIBinder, int paramInt, boolean paramBoolean)
    {
        WakeLock localWakeLock = this.mLocks.removeLock(paramIBinder);
        if (localWakeLock == null)
            return;
        if (isScreenLock(localWakeLock.flags))
            if ((0x3F & localWakeLock.flags) == 32)
            {
                this.mProximityWakeLockCount = (-1 + this.mProximityWakeLockCount);
                if ((this.mProximityWakeLockCount == 0) && ((!this.mProximitySensorActive) || ((paramInt & 0x1) == 0)));
            }
        while (true)
        {
            localWakeLock.binder.unlinkToDeath(localWakeLock, 0);
            noteStopWakeLocked(localWakeLock, localWakeLock.ws);
            break;
            disableProximityLockLocked();
            continue;
            this.mWakeLockState = this.mLocks.gatherState();
            if ((0x20000000 & localWakeLock.flags) != 0)
                userActivity(SystemClock.uptimeMillis(), -1L, false, 0, false, true);
            setPowerState(this.mWakeLockState | this.mUserState);
            continue;
            if ((0x3F & localWakeLock.flags) == 1)
            {
                this.mPartialCount = (-1 + this.mPartialCount);
                if (this.mPartialCount == 0)
                    nativeReleaseWakeLock("PowerManagerService");
            }
        }
    }

    private int screenOffFinishedAnimatingLocked(int paramInt)
    {
        Object[] arrayOfObject = new Object[4];
        arrayOfObject[0] = Integer.valueOf(0);
        arrayOfObject[1] = Integer.valueOf(paramInt);
        arrayOfObject[2] = Long.valueOf(this.mTotalTouchDownTime);
        arrayOfObject[3] = Integer.valueOf(this.mTouchCycles);
        EventLog.writeEvent(2728, arrayOfObject);
        this.mLastTouchDown = 0L;
        int i = setScreenStateLocked(false);
        if (i == 0)
        {
            this.mScreenOffReason = paramInt;
            sendNotificationLocked(false, paramInt);
        }
        return i;
    }

    private void sendNotificationLocked(boolean paramBoolean, int paramInt)
    {
        if (!this.mInitialized)
            return;
        if (!paramBoolean)
            this.mStillNeedSleepNotification = false;
        for (int i = 0; this.mBroadcastQueue[i] != -1; i++);
        int[] arrayOfInt1 = this.mBroadcastQueue;
        int j;
        label49: int[] arrayOfInt2;
        if (paramBoolean)
        {
            j = 1;
            arrayOfInt1[i] = j;
            this.mBroadcastWhy[i] = paramInt;
            if (i == 2)
            {
                if ((!paramBoolean) && (this.mBroadcastWhy[0] > paramInt))
                    this.mBroadcastWhy[0] = paramInt;
                arrayOfInt2 = this.mBroadcastQueue;
                if (!paramBoolean)
                    break label347;
            }
        }
        label347: for (int k = 1; ; k = 0)
        {
            arrayOfInt2[0] = k;
            this.mBroadcastQueue[1] = -1;
            this.mBroadcastQueue[2] = -1;
            Object[] arrayOfObject2 = new Object[2];
            arrayOfObject2[0] = Integer.valueOf(1);
            arrayOfObject2[1] = Integer.valueOf(this.mBroadcastWakeLock.mCount);
            EventLog.writeEvent(2727, arrayOfObject2);
            this.mBroadcastWakeLock.release();
            Object[] arrayOfObject3 = new Object[2];
            arrayOfObject3[0] = Integer.valueOf(1);
            arrayOfObject3[1] = Integer.valueOf(this.mBroadcastWakeLock.mCount);
            EventLog.writeEvent(2727, arrayOfObject3);
            this.mBroadcastWakeLock.release();
            i = 0;
            if ((i == 1) && (!paramBoolean))
            {
                this.mBroadcastQueue[0] = -1;
                this.mBroadcastQueue[1] = -1;
                i = -1;
                Object[] arrayOfObject1 = new Object[2];
                arrayOfObject1[0] = Integer.valueOf(1);
                arrayOfObject1[1] = Integer.valueOf(this.mBroadcastWakeLock.mCount);
                EventLog.writeEvent(2727, arrayOfObject1);
                this.mBroadcastWakeLock.release();
            }
            if (this.mSkippedScreenOn)
                updateLightsLocked(this.mPowerState, 1);
            if (i < 0)
                break;
            this.mBroadcastWakeLock.acquire();
            EventLog.writeEvent(2725, this.mBroadcastWakeLock.mCount);
            this.mHandler.post(this.mNotificationTask);
            break;
            j = 0;
            break label49;
        }
    }

    private void setLightBrightness(int paramInt1, int paramInt2)
    {
        this.mScreenBrightnessAnimator.animateTo(paramInt2, paramInt1, 0);
    }

    private void setPowerState(int paramInt)
    {
        setPowerState(paramInt, false, 3);
    }

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    private void setPowerState(int paramInt1, boolean paramBoolean, int paramInt2)
    {
        LockList localLockList = this.mLocks;
        int i3;
        if (paramBoolean)
            i3 = paramInt1 & 0xFFFFFFF1;
        while (true)
        {
            int m;
            try
            {
                paramInt1 = i3 | 0xE & this.mPowerState;
                if ((!this.mProximitySensorActive) || (batteryIsLow()))
                {
                    i = paramInt1 | 0x10;
                    int j = this.mPowerState;
                    if ((i == j) && (this.mInitialized))
                        continue;
                    if ((!this.mBootCompleted) && (!this.mUseSoftwareAutoBrightness))
                        i |= 15;
                    if ((0x1 & this.mPowerState) != 0)
                    {
                        k = 1;
                        break label609;
                        if (this.mPowerState == i)
                            continue;
                        n = 1;
                        if ((n == 0) || (paramInt2 != 3) || (this.mPolicy == null) || (!this.mPolicy.isScreenSaverEnabled()) || (!this.mPolicy.startScreenSaver()))
                            continue;
                    }
                }
            }
            finally
            {
                throw localObject1;
                int k = 0;
                break label609;
                m = 0;
                continue;
                int n = 0;
                continue;
                if (k != m)
                {
                    if (m != 0)
                    {
                        if (this.mStillNeedSleepNotification)
                            sendNotificationLocked(false, 2);
                        int i1 = 1;
                        if (this.mPreventScreenOn)
                            i1 = 0;
                        int i2;
                        if (i1 != 0)
                        {
                            i2 = setScreenStateLocked(true);
                            long l2 = Binder.clearCallingIdentity();
                            try
                            {
                                this.mBatteryStats.noteScreenBrightness(getPreferredBrightness());
                                this.mBatteryStats.noteScreenOn();
                                Binder.restoreCallingIdentity(l2);
                                this.mLastTouchDown = 0L;
                                this.mTotalTouchDownTime = 0L;
                                this.mTouchCycles = 0;
                                Object[] arrayOfObject = new Object[4];
                                arrayOfObject[0] = Integer.valueOf(1);
                                arrayOfObject[1] = Integer.valueOf(paramInt2);
                                arrayOfObject[2] = Long.valueOf(this.mTotalTouchDownTime);
                                arrayOfObject[3] = Integer.valueOf(this.mTouchCycles);
                                EventLog.writeEvent(2728, arrayOfObject);
                                if (i2 == 0)
                                {
                                    sendNotificationLocked(true, -1);
                                    if (n != 0)
                                        updateLightsLocked(i, 0);
                                    this.mPowerState = (0x1 | this.mPowerState);
                                }
                                this.mPowerState = (0xFFFFFFF1 & this.mPowerState | i & 0xE);
                                updateNativePowerStateLocked();
                            }
                            catch (RemoteException localRemoteException2)
                            {
                                Slog.w("PowerManagerService", "RemoteException calling noteScreenOn on BatteryStatsService", localRemoteException2);
                                Binder.restoreCallingIdentity(l2);
                                continue;
                            }
                            finally
                            {
                                Binder.restoreCallingIdentity(l2);
                            }
                        }
                        else
                        {
                            setScreenStateLocked(false);
                            i2 = 0;
                            continue;
                        }
                    }
                    else
                    {
                        this.mScreenOffReason = paramInt2;
                        if (n != 0)
                            updateLightsLocked(i, 0);
                        this.mHandler.removeCallbacks(this.mAutoBrightnessTask);
                        this.mLightSensorPendingDecrease = false;
                        this.mLightSensorPendingIncrease = false;
                        this.mScreenOffTime = SystemClock.elapsedRealtime();
                        long l1 = Binder.clearCallingIdentity();
                        try
                        {
                            this.mBatteryStats.noteScreenOff();
                            Binder.restoreCallingIdentity(l1);
                            this.mPowerState = (0xFFFFFFFE & this.mPowerState);
                            if (!this.mScreenBrightnessAnimator.isAnimating())
                            {
                                screenOffFinishedAnimatingLocked(paramInt2);
                                continue;
                            }
                        }
                        catch (RemoteException localRemoteException1)
                        {
                            Slog.w("PowerManagerService", "RemoteException calling noteScreenOff on BatteryStatsService", localRemoteException1);
                            Binder.restoreCallingIdentity(l1);
                            continue;
                        }
                        finally
                        {
                            Binder.restoreCallingIdentity(l1);
                        }
                        this.mLastTouchDown = 0L;
                        continue;
                    }
                }
                else
                {
                    if (n == 0)
                        continue;
                    updateLightsLocked(i, 0);
                }
            }
            int i = paramInt1 & 0xFFFFFFEF;
            continue;
            label609: if ((i & 0x1) != 0)
                m = 1;
        }
    }

    private void setScreenBrightnessMode(int paramInt)
    {
        int i = 1;
        LockList localLockList = this.mLocks;
        int j;
        if (paramInt == i)
            j = i;
        while (true)
        {
            try
            {
                if ((this.mUseSoftwareAutoBrightness) && (this.mAutoBrightessEnabled != j))
                {
                    this.mAutoBrightessEnabled = j;
                    if ((!this.mAutoBrightessEnabled) || (!isScreenOn()))
                        break label74;
                    enableLightSensorLocked(i);
                }
                return;
            }
            finally
            {
                localObject = finally;
                throw localObject;
            }
            j = 0;
            continue;
            label74: i = 0;
        }
    }

    private void setScreenOffTimeoutsLocked()
    {
        if ((0x2 & this.mPokey) != 0)
        {
            this.mKeylightDelay = this.mShortKeylightDelay;
            this.mDimDelay = -1;
            this.mScreenOffDelay = 0;
        }
        while (true)
        {
            return;
            if ((0x4 & this.mPokey) != 0)
            {
                this.mKeylightDelay = 15000;
                this.mDimDelay = -1;
                this.mScreenOffDelay = 0;
            }
            else
            {
                int i = this.mScreenOffTimeoutSetting;
                if (i > this.mMaximumScreenOffTimeout)
                    i = this.mMaximumScreenOffTimeout;
                this.mKeylightDelay = 6000;
                if (i < 0)
                    this.mScreenOffDelay = this.mMaximumScreenOffTimeout;
                while (true)
                {
                    if ((!this.mDimScreen) || (i < 13000))
                        break label161;
                    this.mDimDelay = (-7000 + this.mScreenOffDelay);
                    this.mScreenOffDelay = 7000;
                    break;
                    if (this.mKeylightDelay < i)
                        this.mScreenOffDelay = (i - this.mKeylightDelay);
                    else
                        this.mScreenOffDelay = 0;
                }
                label161: this.mDimDelay = -1;
            }
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    private int setScreenStateLocked(boolean paramBoolean)
    {
        if ((paramBoolean) && (this.mInitialized) && (((0x1 & this.mPowerState) == 0) || (this.mSkippedScreenOn)))
            Injector.animateTo(this, this.mScreenBrightnessAnimator, 0, 2, 0);
        int i = nativeSetScreenState(paramBoolean);
        long l;
        if (i == 0)
        {
            if (!paramBoolean)
                break label86;
            l = SystemClock.elapsedRealtime();
            this.mLastScreenOnTime = l;
            if (this.mUseSoftwareAutoBrightness)
            {
                enableLightSensorLocked(paramBoolean);
                if (!paramBoolean)
                    break label91;
                this.mWaitingForFirstLightSensor = this.mAutoBrightessEnabled;
            }
        }
        while (true)
        {
            return i;
            label86: l = 0L;
            break;
            label91: this.mButtonLight.turnOff();
            this.mKeyboardLight.turnOff();
        }
    }

    private void setTimeoutLocked(long paramLong, int paramInt)
    {
        setTimeoutLocked(paramLong, -1L, paramInt);
    }

    private void setTimeoutLocked(long paramLong1, long paramLong2, int paramInt)
    {
        long l1 = paramLong2;
        if (this.mBootCompleted)
        {
            LockList localLockList1 = this.mLocks;
            long l2;
            if (l1 <= 0L)
                switch (paramInt)
                {
                case 2:
                default:
                    l2 = paramLong1;
                case 3:
                case 1:
                case 0:
                }
            while (true)
            {
                long l3;
                try
                {
                    this.mHandler.removeCallbacks(this.mTimeoutTask);
                    this.mTimeoutTask.nextState = paramInt;
                    TimeoutTask localTimeoutTask = this.mTimeoutTask;
                    if (l1 <= 0L)
                        break label330;
                    l3 = paramLong2 - l1;
                    localTimeoutTask.remainingTimeoutOverride = l3;
                    this.mHandler.postAtTime(this.mTimeoutTask, l2);
                    this.mNextTimeout = l2;
                    break;
                    l2 = paramLong1 + this.mKeylightDelay;
                    continue;
                    if (this.mDimDelay >= 0)
                    {
                        l2 = paramLong1 + this.mDimDelay;
                        continue;
                    }
                    Slog.w("PowerManagerService", "mDimDelay=" + this.mDimDelay + " while trying to dim");
                    synchronized (this.mLocks)
                    {
                        l2 = paramLong1 + this.mScreenOffDelay;
                    }
                }
                finally
                {
                }
                if (l1 <= this.mScreenOffDelay)
                {
                    l2 = paramLong1 + l1;
                    paramInt = 0;
                }
                else
                {
                    l1 -= this.mScreenOffDelay;
                    if (this.mDimDelay >= 0)
                    {
                        if (l1 <= this.mDimDelay)
                        {
                            l2 = paramLong1 + l1;
                            paramInt = 1;
                        }
                        else
                        {
                            int i = this.mDimDelay;
                            l1 -= i;
                        }
                    }
                    else
                    {
                        l2 = paramLong1 + l1;
                        paramInt = 3;
                        continue;
                        label330: l3 = -1L;
                    }
                }
            }
        }
    }

    private boolean shouldDeferScreenOnLocked()
    {
        int i = 1;
        if (this.mPreparingForScreenOn);
        while (true)
        {
            return i;
            for (int k = 0; ; k++)
            {
                if (k >= this.mBroadcastQueue.length)
                    break label38;
                if (this.mBroadcastQueue[k] == i)
                    break;
            }
            label38: int j = 0;
        }
    }

    // ERROR //
    private boolean shouldLog(long paramLong)
    {
        // Byte code:
        //     0: iconst_1
        //     1: istore_3
        //     2: aload_0
        //     3: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     6: astore 4
        //     8: aload 4
        //     10: monitorenter
        //     11: lload_1
        //     12: ldc2_w 1059
        //     15: aload_0
        //     16: getfield 1062	com/android/server/PowerManagerService:mWarningSpewThrottleTime	J
        //     19: ladd
        //     20: lcmp
        //     21: ifle +19 -> 40
        //     24: aload_0
        //     25: lload_1
        //     26: putfield 1062	com/android/server/PowerManagerService:mWarningSpewThrottleTime	J
        //     29: aload_0
        //     30: iconst_0
        //     31: putfield 1064	com/android/server/PowerManagerService:mWarningSpewThrottleCount	I
        //     34: aload 4
        //     36: monitorexit
        //     37: goto +41 -> 78
        //     40: aload_0
        //     41: getfield 1064	com/android/server/PowerManagerService:mWarningSpewThrottleCount	I
        //     44: bipush 30
        //     46: if_icmpge +27 -> 73
        //     49: aload_0
        //     50: iconst_1
        //     51: aload_0
        //     52: getfield 1064	com/android/server/PowerManagerService:mWarningSpewThrottleCount	I
        //     55: iadd
        //     56: putfield 1064	com/android/server/PowerManagerService:mWarningSpewThrottleCount	I
        //     59: aload 4
        //     61: monitorexit
        //     62: goto +16 -> 78
        //     65: astore 5
        //     67: aload 4
        //     69: monitorexit
        //     70: aload 5
        //     72: athrow
        //     73: aload 4
        //     75: monitorexit
        //     76: iconst_0
        //     77: istore_3
        //     78: iload_3
        //     79: ireturn
        //
        // Exception table:
        //     from	to	target	type
        //     11	70	65	finally
        //     73	76	65	finally
    }

    // ERROR //
    private void updateLightsLocked(int paramInt1, int paramInt2)
    {
        // Byte code:
        //     0: aload_0
        //     1: getfield 444	com/android/server/PowerManagerService:mPowerState	I
        //     4: istore_3
        //     5: iload_3
        //     6: iconst_1
        //     7: iand
        //     8: ifeq +10 -> 18
        //     11: aload_0
        //     12: getfield 311	com/android/server/PowerManagerService:mSkippedScreenOn	Z
        //     15: ifeq +25 -> 40
        //     18: aload_0
        //     19: invokespecial 1066	com/android/server/PowerManagerService:shouldDeferScreenOnLocked	()Z
        //     22: istore 4
        //     24: aload_0
        //     25: iload 4
        //     27: putfield 311	com/android/server/PowerManagerService:mSkippedScreenOn	Z
        //     30: iload 4
        //     32: ifeq +8 -> 40
        //     35: iload_1
        //     36: bipush 252
        //     38: iand
        //     39: istore_1
        //     40: iload_1
        //     41: iconst_1
        //     42: iand
        //     43: ifeq +13 -> 56
        //     46: aload_0
        //     47: aload_0
        //     48: iload_1
        //     49: invokespecial 1068	com/android/server/PowerManagerService:applyButtonState	(I)I
        //     52: invokespecial 1070	com/android/server/PowerManagerService:applyKeyboardState	(I)I
        //     55: istore_1
        //     56: iload_1
        //     57: iload_3
        //     58: ixor
        //     59: istore 5
        //     61: iload 5
        //     63: iload_2
        //     64: ior
        //     65: istore 6
        //     67: iload 6
        //     69: ifne +4 -> 73
        //     72: return
        //     73: iconst_0
        //     74: istore 7
        //     76: iconst_0
        //     77: istore 8
        //     79: aload_0
        //     80: invokespecial 1001	com/android/server/PowerManagerService:getPreferredBrightness	()I
        //     83: istore 9
        //     85: iload 6
        //     87: bipush 8
        //     89: iand
        //     90: ifeq +16 -> 106
        //     93: iload_1
        //     94: bipush 8
        //     96: iand
        //     97: ifne +287 -> 384
        //     100: iconst_0
        //     101: bipush 8
        //     103: ior
        //     104: istore 7
        //     106: iload 6
        //     108: iconst_4
        //     109: iand
        //     110: ifeq +15 -> 125
        //     113: iload_1
        //     114: iconst_4
        //     115: iand
        //     116: ifne +277 -> 393
        //     119: iload 7
        //     121: iconst_4
        //     122: ior
        //     123: istore 7
        //     125: iload 6
        //     127: iconst_3
        //     128: iand
        //     129: ifeq +168 -> 297
        //     132: iload 5
        //     134: iconst_3
        //     135: iand
        //     136: ifeq +44 -> 180
        //     139: iload_3
        //     140: iconst_3
        //     141: iand
        //     142: tableswitch	default:+30 -> 172, 0:+271->413, 1:+263->405, 2:+30->172, 3:+260->402
        //     173: getfield 757	com/android/server/PowerManagerService:mScreenBrightnessAnimator	Lcom/android/server/PowerManagerService$ScreenBrightnessAnimator;
        //     176: invokevirtual 1073	com/android/server/PowerManagerService$ScreenBrightnessAnimator:getCurrentBrightness	()I
        //     179: pop
        //     180: iload 9
        //     182: istore 12
        //     184: bipush 60
        //     186: istore 13
        //     188: iload_1
        //     189: iconst_2
        //     190: iand
        //     191: ifne +43 -> 234
        //     194: aload_0
        //     195: getfield 793	com/android/server/PowerManagerService:mScreenBrightnessDim	I
        //     198: i2f
        //     199: iload 9
        //     201: i2f
        //     202: fdiv
        //     203: fstore 19
        //     205: fload 19
        //     207: fconst_1
        //     208: fcmpl
        //     209: ifle +6 -> 215
        //     212: fconst_1
        //     213: fstore 19
        //     215: iload_1
        //     216: iconst_1
        //     217: iand
        //     218: ifne +214 -> 432
        //     221: iload_3
        //     222: iconst_2
        //     223: iand
        //     224: ifeq +192 -> 416
        //     227: bipush 60
        //     229: istore 13
        //     231: iconst_0
        //     232: istore 12
        //     234: aload_0
        //     235: getfield 342	com/android/server/PowerManagerService:mWaitingForFirstLightSensor	Z
        //     238: ifeq +12 -> 250
        //     241: iload_1
        //     242: iconst_1
        //     243: iand
        //     244: ifeq +6 -> 250
        //     247: iconst_4
        //     248: istore 13
        //     250: invokestatic 425	android/os/Binder:clearCallingIdentity	()J
        //     253: lstore 14
        //     255: aload_0
        //     256: getfield 999	com/android/server/PowerManagerService:mBatteryStats	Lcom/android/internal/app/IBatteryStats;
        //     259: iload 12
        //     261: invokeinterface 1006 2 0
        //     266: lload 14
        //     268: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     271: aload_0
        //     272: getfield 311	com/android/server/PowerManagerService:mSkippedScreenOn	Z
        //     275: ifne +22 -> 297
        //     278: iload 13
        //     280: bipush 16
        //     282: imul
        //     283: istore 18
        //     285: aload_0
        //     286: getfield 757	com/android/server/PowerManagerService:mScreenBrightnessAnimator	Lcom/android/server/PowerManagerService$ScreenBrightnessAnimator;
        //     289: iload 12
        //     291: iconst_2
        //     292: iload 18
        //     294: invokevirtual 983	com/android/server/PowerManagerService$ScreenBrightnessAnimator:animateTo	(III)V
        //     297: iload 7
        //     299: ifeq +10 -> 309
        //     302: aload_0
        //     303: iload 7
        //     305: iconst_0
        //     306: invokespecial 1075	com/android/server/PowerManagerService:setLightBrightness	(II)V
        //     309: iconst_0
        //     310: ifeq +34 -> 344
        //     313: aload_0
        //     314: getfield 793	com/android/server/PowerManagerService:mScreenBrightnessDim	I
        //     317: istore 11
        //     319: iload_1
        //     320: bipush 16
        //     322: iand
        //     323: ifeq +14 -> 337
        //     326: iload 11
        //     328: bipush 10
        //     330: if_icmple +7 -> 337
        //     333: bipush 10
        //     335: istore 11
        //     337: aload_0
        //     338: iconst_0
        //     339: iload 11
        //     341: invokespecial 1075	com/android/server/PowerManagerService:setLightBrightness	(II)V
        //     344: iload 8
        //     346: ifeq -274 -> 72
        //     349: aload_0
        //     350: invokespecial 1001	com/android/server/PowerManagerService:getPreferredBrightness	()I
        //     353: istore 10
        //     355: iload_1
        //     356: bipush 16
        //     358: iand
        //     359: ifeq +14 -> 373
        //     362: iload 10
        //     364: bipush 10
        //     366: if_icmple +7 -> 373
        //     369: bipush 10
        //     371: istore 10
        //     373: aload_0
        //     374: iload 8
        //     376: iload 10
        //     378: invokespecial 1075	com/android/server/PowerManagerService:setLightBrightness	(II)V
        //     381: goto -309 -> 72
        //     384: iconst_0
        //     385: bipush 8
        //     387: ior
        //     388: istore 8
        //     390: goto -284 -> 106
        //     393: iload 8
        //     395: iconst_4
        //     396: ior
        //     397: istore 8
        //     399: goto -274 -> 125
        //     402: goto -222 -> 180
        //     405: aload_0
        //     406: getfield 793	com/android/server/PowerManagerService:mScreenBrightnessDim	I
        //     409: pop
        //     410: goto -230 -> 180
        //     413: goto -233 -> 180
        //     416: ldc_w 1076
        //     419: ldc_w 1077
        //     422: fload 19
        //     424: fmul
        //     425: fmul
        //     426: f2i
        //     427: istore 13
        //     429: goto -198 -> 231
        //     432: iload_3
        //     433: iconst_1
        //     434: iand
        //     435: ifeq +57 -> 492
        //     438: ldc_w 1076
        //     441: ldc_w 1077
        //     444: fconst_1
        //     445: fload 19
        //     447: fsub
        //     448: fmul
        //     449: fmul
        //     450: f2i
        //     451: istore 13
        //     453: aload_0
        //     454: invokevirtual 1080	com/android/server/PowerManagerService:getStayOnConditionsLocked	()I
        //     457: istore 20
        //     459: iload 20
        //     461: ifeq +22 -> 483
        //     464: aload_0
        //     465: getfield 654	com/android/server/PowerManagerService:mBatteryService	Lcom/android/server/BatteryService;
        //     468: iload 20
        //     470: invokevirtual 1083	com/android/server/BatteryService:isPowered	(I)Z
        //     473: ifeq +10 -> 483
        //     476: aload_0
        //     477: invokestatic 827	android/os/SystemClock:elapsedRealtime	()J
        //     480: putfield 1018	com/android/server/PowerManagerService:mScreenOffTime	J
        //     483: aload_0
        //     484: getfield 793	com/android/server/PowerManagerService:mScreenBrightnessDim	I
        //     487: istore 12
        //     489: goto -255 -> 234
        //     492: ldc_w 1077
        //     495: fload 19
        //     497: fmul
        //     498: f2i
        //     499: istore 13
        //     501: goto -48 -> 453
        //     504: astore 17
        //     506: lload 14
        //     508: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     511: goto -240 -> 271
        //     514: astore 16
        //     516: lload 14
        //     518: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     521: aload 16
        //     523: athrow
        //
        // Exception table:
        //     from	to	target	type
        //     255	266	504	android/os/RemoteException
        //     255	266	514	finally
    }

    private void updateNativePowerStateLocked()
    {
        boolean bool1 = true;
        boolean bool2;
        if (!this.mHeadless)
        {
            if ((0x1 & this.mPowerState) == 0)
                break label37;
            bool2 = bool1;
            if ((0x3 & this.mPowerState) != 3)
                break label42;
        }
        while (true)
        {
            nativeSetPowerState(bool2, bool1);
            return;
            label37: bool2 = false;
            break;
            label42: bool1 = false;
        }
    }

    private void updateSettingsValues()
    {
        this.mShortKeylightDelay = Settings.Secure.getInt(this.mContext.getContentResolver(), "short_keylight_delay_ms", 6000);
    }

    private void updateWakeLockLocked()
    {
        int i = getStayOnConditionsLocked();
        if ((i != 0) && (this.mBatteryService.isPowered(i)))
        {
            this.mStayOnWhilePluggedInScreenDimLock.acquire();
            this.mStayOnWhilePluggedInPartialLock.acquire();
        }
        while (true)
        {
            return;
            this.mStayOnWhilePluggedInScreenDimLock.release();
            this.mStayOnWhilePluggedInPartialLock.release();
        }
    }

    // ERROR //
    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    private void userActivity(long paramLong1, long paramLong2, boolean paramBoolean1, int paramInt, boolean paramBoolean2, boolean paramBoolean3)
    {
        // Byte code:
        //     0: iconst_1
        //     1: aload_0
        //     2: getfield 371	com/android/server/PowerManagerService:mPokey	I
        //     5: iand
        //     6: ifeq +10 -> 16
        //     9: iload 6
        //     11: iconst_2
        //     12: if_icmpne +4 -> 16
        //     15: return
        //     16: aload_0
        //     17: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     20: astore 9
        //     22: aload 9
        //     24: monitorenter
        //     25: aload_0
        //     26: invokespecial 663	com/android/server/PowerManagerService:isScreenTurningOffLocked	()Z
        //     29: ifeq +26 -> 55
        //     32: ldc 128
        //     34: ldc_w 1105
        //     37: invokestatic 903	android/util/Slog:d	(Ljava/lang/String;Ljava/lang/String;)I
        //     40: pop
        //     41: aload 9
        //     43: monitorexit
        //     44: goto -29 -> 15
        //     47: astore 10
        //     49: aload 9
        //     51: monitorexit
        //     52: aload 10
        //     54: athrow
        //     55: iload 8
        //     57: ifeq +18 -> 75
        //     60: iconst_1
        //     61: aload_0
        //     62: getfield 444	com/android/server/PowerManagerService:mPowerState	I
        //     65: iand
        //     66: ifne +9 -> 75
        //     69: aload 9
        //     71: monitorexit
        //     72: goto -57 -> 15
        //     75: aload_0
        //     76: getfield 325	com/android/server/PowerManagerService:mProximitySensorActive	Z
        //     79: ifeq +10 -> 89
        //     82: aload_0
        //     83: getfield 321	com/android/server/PowerManagerService:mProximityWakeLockCount	I
        //     86: ifne +3 -> 89
        //     89: aload_0
        //     90: getfield 332	com/android/server/PowerManagerService:mLastEventTime	J
        //     93: lload_1
        //     94: lcmp
        //     95: ifle +8 -> 103
        //     98: iload 7
        //     100: ifeq +116 -> 216
        //     103: aload_0
        //     104: lload_1
        //     105: putfield 332	com/android/server/PowerManagerService:mLastEventTime	J
        //     108: aload_0
        //     109: getfield 319	com/android/server/PowerManagerService:mUserActivityAllowed	Z
        //     112: ifeq +166 -> 278
        //     115: getstatic 1108	com/android/server/PowerManagerService$Injector:FALSE	Z
        //     118: ifeq +6 -> 124
        //     121: goto +157 -> 278
        //     124: iload 6
        //     126: iconst_1
        //     127: if_icmpne +118 -> 245
        //     130: aload_0
        //     131: getfield 677	com/android/server/PowerManagerService:mUseSoftwareAutoBrightness	Z
        //     134: ifne +111 -> 245
        //     137: aload_0
        //     138: getfield 317	com/android/server/PowerManagerService:mKeyboardVisible	Z
        //     141: ifeq +97 -> 238
        //     144: bipush 15
        //     146: istore 16
        //     148: aload_0
        //     149: iload 16
        //     151: putfield 446	com/android/server/PowerManagerService:mUserState	I
        //     154: invokestatic 1111	android/os/Binder:getCallingUid	()I
        //     157: istore 11
        //     159: invokestatic 425	android/os/Binder:clearCallingIdentity	()J
        //     162: lstore 12
        //     164: aload_0
        //     165: getfield 999	com/android/server/PowerManagerService:mBatteryStats	Lcom/android/internal/app/IBatteryStats;
        //     168: iload 11
        //     170: iload 6
        //     172: invokeinterface 1114 3 0
        //     177: lload 12
        //     179: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     182: aload_0
        //     183: aload_0
        //     184: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     187: invokevirtual 1117	com/android/server/PowerManagerService$LockList:reactivateScreenLocksLocked	()I
        //     190: putfield 507	com/android/server/PowerManagerService:mWakeLockState	I
        //     193: aload_0
        //     194: aload_0
        //     195: getfield 446	com/android/server/PowerManagerService:mUserState	I
        //     198: aload_0
        //     199: getfield 507	com/android/server/PowerManagerService:mWakeLockState	I
        //     202: ior
        //     203: iload 5
        //     205: iconst_2
        //     206: invokespecial 822	com/android/server/PowerManagerService:setPowerState	(IZI)V
        //     209: aload_0
        //     210: lload_1
        //     211: lload_3
        //     212: iconst_3
        //     213: invokespecial 521	com/android/server/PowerManagerService:setTimeoutLocked	(JJI)V
        //     216: aload 9
        //     218: monitorexit
        //     219: aload_0
        //     220: getfield 989	com/android/server/PowerManagerService:mPolicy	Landroid/view/WindowManagerPolicy;
        //     223: ifnull -208 -> 15
        //     226: aload_0
        //     227: getfield 989	com/android/server/PowerManagerService:mPolicy	Landroid/view/WindowManagerPolicy;
        //     230: invokeinterface 1119 1 0
        //     235: goto -220 -> 15
        //     238: bipush 7
        //     240: istore 16
        //     242: goto -94 -> 148
        //     245: aload_0
        //     246: iconst_3
        //     247: aload_0
        //     248: getfield 446	com/android/server/PowerManagerService:mUserState	I
        //     251: ior
        //     252: putfield 446	com/android/server/PowerManagerService:mUserState	I
        //     255: goto -101 -> 154
        //     258: astore 15
        //     260: lload 12
        //     262: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     265: goto -83 -> 182
        //     268: astore 14
        //     270: lload 12
        //     272: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     275: aload 14
        //     277: athrow
        //     278: iload 7
        //     280: ifeq -64 -> 216
        //     283: goto -159 -> 124
        //
        // Exception table:
        //     from	to	target	type
        //     25	52	47	finally
        //     60	164	47	finally
        //     177	219	47	finally
        //     245	278	47	finally
        //     164	177	258	android/os/RemoteException
        //     164	177	268	finally
    }

    public void acquireWakeLock(int paramInt, IBinder paramIBinder, String paramString, WorkSource paramWorkSource)
    {
        int i = Binder.getCallingUid();
        int j = Binder.getCallingPid();
        if (i != Process.myUid())
            this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        if (paramWorkSource != null)
            enforceWakeSourcePermission(i, j);
        long l = Binder.clearCallingIdentity();
        try
        {
            synchronized (this.mLocks)
            {
                acquireWakeLockLocked(paramInt, paramIBinder, i, j, paramString, paramWorkSource);
                return;
            }
        }
        finally
        {
            Binder.restoreCallingIdentity(l);
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    public void acquireWakeLockLocked(int paramInt1, IBinder paramIBinder, int paramInt2, int paramInt3, String paramString, WorkSource paramWorkSource)
    {
        if ((paramWorkSource != null) && (paramWorkSource.size() == 0))
            paramWorkSource = null;
        int i = this.mLocks.getIndex(paramIBinder);
        WakeLock localWakeLock;
        label165: int j;
        boolean bool;
        WorkSource localWorkSource;
        if (i < 0)
        {
            localWakeLock = new WakeLock(paramInt1, paramIBinder, paramString, paramInt2, paramInt3);
            switch (0x3F & localWakeLock.flags)
            {
            default:
                Slog.e("PowerManagerService", "bad wakelock type for lock '" + paramString + "' " + " flags=" + paramInt1);
                return;
            case 26:
                if (this.mUseSoftwareAutoBrightness)
                    localWakeLock.minState = 3;
                break;
            case 1:
            case 32:
                this.mLocks.addLock(localWakeLock);
                if (paramWorkSource != null)
                    localWakeLock.ws = new WorkSource(paramWorkSource);
                j = 1;
                bool = false;
                localWorkSource = null;
                label202: if (isScreenLock(paramInt1))
                    if ((paramInt1 & 0x3F) == 32)
                    {
                        this.mProximityWakeLockCount = (1 + this.mProximityWakeLockCount);
                        if (this.mProximityWakeLockCount == 1)
                            enableProximityLockLocked();
                    }
                break;
            case 10:
            case 6:
            }
        }
        while (true)
        {
            if (bool)
                noteStopWakeLocked(localWakeLock, localWorkSource);
            if ((j == 0) && (!bool))
                break;
            noteStartWakeLocked(localWakeLock, paramWorkSource);
            break;
            if (this.mKeyboardVisible);
            for (int k = 15; ; k = 7)
            {
                localWakeLock.minState = k;
                break;
            }
            localWakeLock.minState = 3;
            break label165;
            localWakeLock.minState = 1;
            break label165;
            localWakeLock = (WakeLock)this.mLocks.get(i);
            j = 0;
            localWorkSource = localWakeLock.ws;
            if (localWorkSource != null)
                if (paramWorkSource == null)
                {
                    localWakeLock.ws = null;
                    bool = true;
                }
            while (true)
            {
                if (!bool)
                    break label413;
                localWakeLock.ws = new WorkSource(paramWorkSource);
                break;
                bool = localWorkSource.diff(paramWorkSource);
                continue;
                if (paramWorkSource != null)
                    bool = true;
                else
                    bool = false;
            }
            label413: break label202;
            if ((0x10000000 & localWakeLock.flags) != 0)
            {
                this.mWakeLockState = this.mLocks.reactivateScreenLocksLocked();
                if (((0x1 & this.mWakeLockState) == 0) || (!this.mProximitySensorActive) || (this.mProximityWakeLockCount != 0));
            }
            while (true)
            {
                setPowerState(this.mWakeLockState | this.mUserState);
                break;
                this.mWakeLockState = ((this.mUserState | this.mWakeLockState) & this.mLocks.gatherState());
            }
            if ((paramInt1 & 0x3F) == 1)
            {
                if (j != 0)
                {
                    this.mPartialCount = (1 + this.mPartialCount);
                    if (this.mPartialCount != 1);
                }
                nativeAcquireWakeLock(1, "PowerManagerService");
            }
        }
    }

    void bootCompleted()
    {
        Slog.d("PowerManagerService", "bootCompleted");
        synchronized (this.mLocks)
        {
            this.mBootCompleted = true;
            userActivity(SystemClock.uptimeMillis(), false, 1, true);
            updateWakeLockLocked();
            this.mLocks.notifyAll();
            return;
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    void callGoToSleepLocked(long paramLong, int paramInt)
    {
        goToSleepLocked(paramLong, paramInt);
    }

    public void clearUserActivityTimeout(long paramLong1, long paramLong2)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        Slog.i("PowerManagerService", "clearUserActivity for " + paramLong2 + "ms from now");
        userActivity(paramLong1, paramLong2, false, 0, false, false);
    }

    public void crash(final String paramString)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
        Thread local11 = new Thread("PowerManagerService.crash()")
        {
            public void run()
            {
                throw new RuntimeException(paramString);
            }
        };
        try
        {
            local11.start();
            local11.join();
            return;
        }
        catch (InterruptedException localInterruptedException)
        {
            while (true)
                Log.wtf("PowerManagerService", localInterruptedException);
        }
    }

    // ERROR //
    public void dump(java.io.FileDescriptor paramFileDescriptor, PrintWriter paramPrintWriter, String[] paramArrayOfString)
    {
        // Byte code:
        //     0: aload_0
        //     1: getfield 544	com/android/server/PowerManagerService:mContext	Landroid/content/Context;
        //     4: ldc_w 1229
        //     7: invokevirtual 1233	android/content/Context:checkCallingOrSelfPermission	(Ljava/lang/String;)I
        //     10: ifeq +42 -> 52
        //     13: aload_2
        //     14: new 711	java/lang/StringBuilder
        //     17: dup
        //     18: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     21: ldc_w 1235
        //     24: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     27: invokestatic 1124	android/os/Binder:getCallingPid	()I
        //     30: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     33: ldc_w 1237
        //     36: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     39: invokestatic 1111	android/os/Binder:getCallingUid	()I
        //     42: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     45: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     48: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     51: return
        //     52: invokestatic 765	android/os/SystemClock:uptimeMillis	()J
        //     55: lstore 4
        //     57: aload_0
        //     58: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     61: astore 6
        //     63: aload 6
        //     65: monitorenter
        //     66: aload_2
        //     67: ldc_w 1244
        //     70: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     73: aload_2
        //     74: new 711	java/lang/StringBuilder
        //     77: dup
        //     78: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     81: ldc_w 1246
        //     84: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     87: aload_0
        //     88: getfield 344	com/android/server/PowerManagerService:mIsPowered	Z
        //     91: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     94: ldc_w 1251
        //     97: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     100: aload_0
        //     101: getfield 444	com/android/server/PowerManagerService:mPowerState	I
        //     104: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     107: ldc_w 1253
        //     110: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     113: invokestatic 827	android/os/SystemClock:elapsedRealtime	()J
        //     116: aload_0
        //     117: getfield 1018	com/android/server/PowerManagerService:mScreenOffTime	J
        //     120: lsub
        //     121: invokevirtual 1196	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
        //     124: ldc_w 1255
        //     127: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     130: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     133: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     136: aload_2
        //     137: new 711	java/lang/StringBuilder
        //     140: dup
        //     141: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     144: ldc_w 1257
        //     147: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     150: aload_0
        //     151: getfield 315	com/android/server/PowerManagerService:mPartialCount	I
        //     154: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     157: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     160: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     163: aload_2
        //     164: new 711	java/lang/StringBuilder
        //     167: dup
        //     168: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     171: ldc_w 1259
        //     174: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     177: aload_0
        //     178: getfield 507	com/android/server/PowerManagerService:mWakeLockState	I
        //     181: invokestatic 606	com/android/server/PowerManagerService:dumpPowerState	(I)Ljava/lang/String;
        //     184: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     187: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     190: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     193: aload_2
        //     194: new 711	java/lang/StringBuilder
        //     197: dup
        //     198: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     201: ldc_w 1261
        //     204: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     207: aload_0
        //     208: getfield 446	com/android/server/PowerManagerService:mUserState	I
        //     211: invokestatic 606	com/android/server/PowerManagerService:dumpPowerState	(I)Ljava/lang/String;
        //     214: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     217: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     220: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     223: aload_2
        //     224: new 711	java/lang/StringBuilder
        //     227: dup
        //     228: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     231: ldc_w 1263
        //     234: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     237: aload_0
        //     238: getfield 444	com/android/server/PowerManagerService:mPowerState	I
        //     241: invokestatic 606	com/android/server/PowerManagerService:dumpPowerState	(I)Ljava/lang/String;
        //     244: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     247: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     250: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     253: aload_2
        //     254: new 711	java/lang/StringBuilder
        //     257: dup
        //     258: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     261: ldc_w 1265
        //     264: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     267: aload_0
        //     268: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     271: invokevirtual 932	com/android/server/PowerManagerService$LockList:gatherState	()I
        //     274: invokestatic 606	com/android/server/PowerManagerService:dumpPowerState	(I)Ljava/lang/String;
        //     277: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     280: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     283: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     286: aload_2
        //     287: new 711	java/lang/StringBuilder
        //     290: dup
        //     291: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     294: ldc_w 1267
        //     297: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     300: aload_0
        //     301: getfield 1048	com/android/server/PowerManagerService:mNextTimeout	J
        //     304: invokevirtual 1196	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
        //     307: ldc_w 1269
        //     310: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     313: lload 4
        //     315: invokevirtual 1196	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
        //     318: ldc_w 1271
        //     321: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     324: aload_0
        //     325: getfield 1048	com/android/server/PowerManagerService:mNextTimeout	J
        //     328: lload 4
        //     330: lsub
        //     331: ldc2_w 1272
        //     334: ldiv
        //     335: invokevirtual 1196	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
        //     338: ldc_w 1275
        //     341: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     344: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     347: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     350: aload_2
        //     351: new 711	java/lang/StringBuilder
        //     354: dup
        //     355: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     358: ldc_w 1277
        //     361: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     364: aload_0
        //     365: getfield 367	com/android/server/PowerManagerService:mDimScreen	Z
        //     368: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     371: ldc_w 1279
        //     374: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     377: aload_0
        //     378: getfield 303	com/android/server/PowerManagerService:mStayOnConditions	I
        //     381: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     384: ldc_w 1281
        //     387: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     390: aload_0
        //     391: getfield 309	com/android/server/PowerManagerService:mPreparingForScreenOn	Z
        //     394: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     397: ldc_w 1283
        //     400: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     403: aload_0
        //     404: getfield 311	com/android/server/PowerManagerService:mSkippedScreenOn	Z
        //     407: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     410: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     413: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     416: aload_2
        //     417: new 711	java/lang/StringBuilder
        //     420: dup
        //     421: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     424: ldc_w 1285
        //     427: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     430: aload_0
        //     431: getfield 601	com/android/server/PowerManagerService:mScreenOffReason	I
        //     434: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     437: ldc_w 1287
        //     440: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     443: aload_0
        //     444: getfield 446	com/android/server/PowerManagerService:mUserState	I
        //     447: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     450: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     453: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     456: aload_2
        //     457: new 711	java/lang/StringBuilder
        //     460: dup
        //     461: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     464: ldc_w 1289
        //     467: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     470: aload_0
        //     471: getfield 305	com/android/server/PowerManagerService:mBroadcastQueue	[I
        //     474: iconst_0
        //     475: iaload
        //     476: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     479: bipush 44
        //     481: invokevirtual 1292	java/lang/StringBuilder:append	(C)Ljava/lang/StringBuilder;
        //     484: aload_0
        //     485: getfield 305	com/android/server/PowerManagerService:mBroadcastQueue	[I
        //     488: iconst_1
        //     489: iaload
        //     490: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     493: bipush 44
        //     495: invokevirtual 1292	java/lang/StringBuilder:append	(C)Ljava/lang/StringBuilder;
        //     498: aload_0
        //     499: getfield 305	com/android/server/PowerManagerService:mBroadcastQueue	[I
        //     502: iconst_2
        //     503: iaload
        //     504: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     507: ldc_w 1294
        //     510: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     513: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     516: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     519: aload_2
        //     520: new 711	java/lang/StringBuilder
        //     523: dup
        //     524: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     527: ldc_w 1296
        //     530: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     533: aload_0
        //     534: getfield 307	com/android/server/PowerManagerService:mBroadcastWhy	[I
        //     537: iconst_0
        //     538: iaload
        //     539: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     542: bipush 44
        //     544: invokevirtual 1292	java/lang/StringBuilder:append	(C)Ljava/lang/StringBuilder;
        //     547: aload_0
        //     548: getfield 307	com/android/server/PowerManagerService:mBroadcastWhy	[I
        //     551: iconst_1
        //     552: iaload
        //     553: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     556: bipush 44
        //     558: invokevirtual 1292	java/lang/StringBuilder:append	(C)Ljava/lang/StringBuilder;
        //     561: aload_0
        //     562: getfield 307	com/android/server/PowerManagerService:mBroadcastWhy	[I
        //     565: iconst_2
        //     566: iaload
        //     567: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     570: ldc_w 1294
        //     573: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     576: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     579: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     582: aload_2
        //     583: new 711	java/lang/StringBuilder
        //     586: dup
        //     587: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     590: ldc_w 1298
        //     593: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     596: aload_0
        //     597: getfield 371	com/android/server/PowerManagerService:mPokey	I
        //     600: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     603: ldc_w 1300
        //     606: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     609: aload_0
        //     610: getfield 373	com/android/server/PowerManagerService:mPokeAwakeOnSet	Z
        //     613: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     616: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     619: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     622: aload_2
        //     623: new 711	java/lang/StringBuilder
        //     626: dup
        //     627: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     630: ldc_w 1302
        //     633: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     636: aload_0
        //     637: getfield 317	com/android/server/PowerManagerService:mKeyboardVisible	Z
        //     640: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     643: ldc_w 1304
        //     646: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     649: aload_0
        //     650: getfield 319	com/android/server/PowerManagerService:mUserActivityAllowed	Z
        //     653: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     656: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     659: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     662: aload_2
        //     663: new 711	java/lang/StringBuilder
        //     666: dup
        //     667: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     670: ldc_w 1306
        //     673: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     676: aload_0
        //     677: getfield 1030	com/android/server/PowerManagerService:mKeylightDelay	I
        //     680: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     683: ldc_w 1308
        //     686: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     689: aload_0
        //     690: getfield 514	com/android/server/PowerManagerService:mDimDelay	I
        //     693: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     696: ldc_w 1310
        //     699: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     702: aload_0
        //     703: getfield 1032	com/android/server/PowerManagerService:mScreenOffDelay	I
        //     706: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     709: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     712: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     715: aload_2
        //     716: new 711	java/lang/StringBuilder
        //     719: dup
        //     720: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     723: ldc_w 1312
        //     726: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     729: aload_0
        //     730: getfield 742	com/android/server/PowerManagerService:mPreventScreenOn	Z
        //     733: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     736: ldc_w 1314
        //     739: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     742: aload_0
        //     743: getfield 384	com/android/server/PowerManagerService:mScreenBrightnessOverride	I
        //     746: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     749: ldc_w 1316
        //     752: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     755: aload_0
        //     756: getfield 386	com/android/server/PowerManagerService:mButtonBrightnessOverride	I
        //     759: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     762: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     765: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     768: aload_2
        //     769: new 711	java/lang/StringBuilder
        //     772: dup
        //     773: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     776: ldc_w 1318
        //     779: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     782: aload_0
        //     783: getfield 478	com/android/server/PowerManagerService:mScreenOffTimeoutSetting	I
        //     786: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     789: ldc_w 1320
        //     792: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     795: aload_0
        //     796: getfield 330	com/android/server/PowerManagerService:mMaximumScreenOffTimeout	I
        //     799: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     802: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     805: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     808: aload_2
        //     809: new 711	java/lang/StringBuilder
        //     812: dup
        //     813: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     816: ldc_w 1322
        //     819: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     822: aload_0
        //     823: getfield 829	com/android/server/PowerManagerService:mLastScreenOnTime	J
        //     826: invokevirtual 1196	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
        //     829: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     832: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     835: aload_2
        //     836: new 711	java/lang/StringBuilder
        //     839: dup
        //     840: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     843: ldc_w 1324
        //     846: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     849: aload_0
        //     850: getfield 535	com/android/server/PowerManagerService:mBroadcastWakeLock	Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
        //     853: invokevirtual 1327	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
        //     856: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     859: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     862: aload_2
        //     863: new 711	java/lang/StringBuilder
        //     866: dup
        //     867: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     870: ldc_w 1329
        //     873: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     876: aload_0
        //     877: getfield 1101	com/android/server/PowerManagerService:mStayOnWhilePluggedInScreenDimLock	Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
        //     880: invokevirtual 1327	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
        //     883: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     886: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     889: aload_2
        //     890: new 711	java/lang/StringBuilder
        //     893: dup
        //     894: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     897: ldc_w 1331
        //     900: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     903: aload_0
        //     904: getfield 1103	com/android/server/PowerManagerService:mStayOnWhilePluggedInPartialLock	Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
        //     907: invokevirtual 1327	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
        //     910: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     913: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     916: aload_2
        //     917: new 711	java/lang/StringBuilder
        //     920: dup
        //     921: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     924: ldc_w 1333
        //     927: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     930: aload_0
        //     931: getfield 1335	com/android/server/PowerManagerService:mPreventScreenOnPartialLock	Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
        //     934: invokevirtual 1327	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
        //     937: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     940: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     943: aload_2
        //     944: new 711	java/lang/StringBuilder
        //     947: dup
        //     948: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     951: ldc_w 1337
        //     954: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     957: aload_0
        //     958: getfield 624	com/android/server/PowerManagerService:mProximityPartialLock	Lcom/android/server/PowerManagerService$UnsynchronizedWakeLock;
        //     961: invokevirtual 1327	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
        //     964: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     967: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     970: aload_2
        //     971: new 711	java/lang/StringBuilder
        //     974: dup
        //     975: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     978: ldc_w 1339
        //     981: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     984: aload_0
        //     985: getfield 321	com/android/server/PowerManagerService:mProximityWakeLockCount	I
        //     988: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     991: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     994: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     997: aload_2
        //     998: new 711	java/lang/StringBuilder
        //     1001: dup
        //     1002: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1005: ldc_w 1341
        //     1008: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1011: aload_0
        //     1012: getfield 323	com/android/server/PowerManagerService:mProximitySensorEnabled	Z
        //     1015: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1018: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1021: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1024: aload_2
        //     1025: new 711	java/lang/StringBuilder
        //     1028: dup
        //     1029: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1032: ldc_w 1343
        //     1035: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1038: aload_0
        //     1039: getfield 325	com/android/server/PowerManagerService:mProximitySensorActive	Z
        //     1042: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1045: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1048: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1051: aload_2
        //     1052: new 711	java/lang/StringBuilder
        //     1055: dup
        //     1056: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1059: ldc_w 1345
        //     1062: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1065: aload_0
        //     1066: getfield 327	com/android/server/PowerManagerService:mProximityPendingValue	I
        //     1069: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1072: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1075: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1078: aload_2
        //     1079: new 711	java/lang/StringBuilder
        //     1082: dup
        //     1083: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1086: ldc_w 1347
        //     1089: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1092: aload_0
        //     1093: getfield 646	com/android/server/PowerManagerService:mLastProximityEventTime	J
        //     1096: invokevirtual 1196	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
        //     1099: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1102: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1105: aload_2
        //     1106: new 711	java/lang/StringBuilder
        //     1109: dup
        //     1110: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1113: ldc_w 1349
        //     1116: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1119: aload_0
        //     1120: getfield 733	com/android/server/PowerManagerService:mLightSensorEnabled	Z
        //     1123: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1126: ldc_w 1351
        //     1129: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1132: aload_0
        //     1133: getfield 359	com/android/server/PowerManagerService:mLightSensorAdjustSetting	F
        //     1136: invokevirtual 1354	java/lang/StringBuilder:append	(F)Ljava/lang/StringBuilder;
        //     1139: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1142: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1145: aload_2
        //     1146: new 711	java/lang/StringBuilder
        //     1149: dup
        //     1150: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1153: ldc_w 1356
        //     1156: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1159: aload_0
        //     1160: getfield 347	com/android/server/PowerManagerService:mLightSensorValue	F
        //     1163: invokevirtual 1354	java/lang/StringBuilder:append	(F)Ljava/lang/StringBuilder;
        //     1166: ldc_w 1358
        //     1169: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1172: aload_0
        //     1173: getfield 357	com/android/server/PowerManagerService:mLightSensorPendingValue	F
        //     1176: invokevirtual 1354	java/lang/StringBuilder:append	(F)Ljava/lang/StringBuilder;
        //     1179: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1182: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1185: aload_2
        //     1186: new 711	java/lang/StringBuilder
        //     1189: dup
        //     1190: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1193: ldc_w 1360
        //     1196: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1199: aload_0
        //     1200: getfield 351	com/android/server/PowerManagerService:mHighestLightSensorValue	I
        //     1203: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1206: ldc_w 1362
        //     1209: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1212: aload_0
        //     1213: getfield 342	com/android/server/PowerManagerService:mWaitingForFirstLightSensor	Z
        //     1216: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1219: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1222: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1225: aload_2
        //     1226: new 711	java/lang/StringBuilder
        //     1229: dup
        //     1230: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1233: ldc_w 1364
        //     1236: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1239: aload_0
        //     1240: getfield 353	com/android/server/PowerManagerService:mLightSensorPendingDecrease	Z
        //     1243: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1246: ldc_w 1366
        //     1249: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1252: aload_0
        //     1253: getfield 355	com/android/server/PowerManagerService:mLightSensorPendingIncrease	Z
        //     1256: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1259: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1262: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1265: aload_2
        //     1266: new 711	java/lang/StringBuilder
        //     1269: dup
        //     1270: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1273: ldc_w 1368
        //     1276: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1279: aload_0
        //     1280: getfield 361	com/android/server/PowerManagerService:mLightSensorScreenBrightness	I
        //     1283: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1286: ldc_w 1370
        //     1289: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1292: aload_0
        //     1293: getfield 363	com/android/server/PowerManagerService:mLightSensorButtonBrightness	I
        //     1296: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1299: ldc_w 1372
        //     1302: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1305: aload_0
        //     1306: getfield 365	com/android/server/PowerManagerService:mLightSensorKeyboardBrightness	I
        //     1309: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1312: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1315: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1318: aload_2
        //     1319: new 711	java/lang/StringBuilder
        //     1322: dup
        //     1323: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1326: ldc_w 1374
        //     1329: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1332: aload_0
        //     1333: getfield 677	com/android/server/PowerManagerService:mUseSoftwareAutoBrightness	Z
        //     1336: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1339: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1342: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1345: aload_2
        //     1346: new 711	java/lang/StringBuilder
        //     1349: dup
        //     1350: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1353: ldc_w 1376
        //     1356: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1359: aload_0
        //     1360: getfield 576	com/android/server/PowerManagerService:mAutoBrightessEnabled	Z
        //     1363: invokevirtual 1249	java/lang/StringBuilder:append	(Z)Ljava/lang/StringBuilder;
        //     1366: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1369: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1372: aload_0
        //     1373: getfield 757	com/android/server/PowerManagerService:mScreenBrightnessAnimator	Lcom/android/server/PowerManagerService$ScreenBrightnessAnimator;
        //     1376: aload_2
        //     1377: ldc_w 1378
        //     1380: invokevirtual 1381	com/android/server/PowerManagerService$ScreenBrightnessAnimator:dump	(Ljava/io/PrintWriter;Ljava/lang/String;)V
        //     1383: aload_0
        //     1384: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     1387: invokevirtual 802	com/android/server/PowerManagerService$LockList:size	()I
        //     1390: istore 8
        //     1392: aload_2
        //     1393: invokevirtual 1383	java/io/PrintWriter:println	()V
        //     1396: aload_2
        //     1397: new 711	java/lang/StringBuilder
        //     1400: dup
        //     1401: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1404: ldc_w 1385
        //     1407: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1410: iload 8
        //     1412: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1415: ldc_w 1387
        //     1418: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1421: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1424: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1427: iconst_0
        //     1428: istore 9
        //     1430: iload 9
        //     1432: iload 8
        //     1434: if_icmpge +179 -> 1613
        //     1437: aload_0
        //     1438: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     1441: iload 9
        //     1443: invokevirtual 806	com/android/server/PowerManagerService$LockList:get	(I)Ljava/lang/Object;
        //     1446: checkcast 48	com/android/server/PowerManagerService$WakeLock
        //     1449: astore 18
        //     1451: bipush 63
        //     1453: aload 18
        //     1455: getfield 809	com/android/server/PowerManagerService$WakeLock:flags	I
        //     1458: iand
        //     1459: invokestatic 1389	com/android/server/PowerManagerService:lockType	(I)Ljava/lang/String;
        //     1462: astore 19
        //     1464: ldc_w 730
        //     1467: astore 20
        //     1469: ldc_w 1175
        //     1472: aload 18
        //     1474: getfield 809	com/android/server/PowerManagerService$WakeLock:flags	I
        //     1477: iand
        //     1478: ifeq +8 -> 1486
        //     1481: ldc_w 1391
        //     1484: astore 20
        //     1486: ldc_w 730
        //     1489: astore 21
        //     1491: aload 18
        //     1493: getfield 812	com/android/server/PowerManagerService$WakeLock:activated	Z
        //     1496: ifeq +8 -> 1504
        //     1499: ldc_w 1393
        //     1502: astore 21
        //     1504: aload_2
        //     1505: new 711	java/lang/StringBuilder
        //     1508: dup
        //     1509: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1512: ldc_w 1395
        //     1515: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1518: aload 19
        //     1520: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1523: ldc_w 1397
        //     1526: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1529: aload 18
        //     1531: getfield 1400	com/android/server/PowerManagerService$WakeLock:tag	Ljava/lang/String;
        //     1534: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1537: ldc_w 1402
        //     1540: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1543: aload 20
        //     1545: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1548: aload 21
        //     1550: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1553: ldc_w 1404
        //     1556: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1559: aload 18
        //     1561: getfield 1158	com/android/server/PowerManagerService$WakeLock:minState	I
        //     1564: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1567: ldc_w 1237
        //     1570: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1573: aload 18
        //     1575: getfield 1407	com/android/server/PowerManagerService$WakeLock:uid	I
        //     1578: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1581: ldc_w 1409
        //     1584: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1587: aload 18
        //     1589: getfield 1412	com/android/server/PowerManagerService$WakeLock:pid	I
        //     1592: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1595: ldc_w 1414
        //     1598: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1601: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1604: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1607: iinc 9 1
        //     1610: goto -180 -> 1430
        //     1613: aload_2
        //     1614: invokevirtual 1383	java/io/PrintWriter:println	()V
        //     1617: aload_2
        //     1618: new 711	java/lang/StringBuilder
        //     1621: dup
        //     1622: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1625: ldc_w 1416
        //     1628: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1631: aload_0
        //     1632: getfield 380	com/android/server/PowerManagerService:mPokeLocks	Ljava/util/HashMap;
        //     1635: invokevirtual 1417	java/util/HashMap:size	()I
        //     1638: invokevirtual 1053	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //     1641: ldc_w 1387
        //     1644: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1647: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1650: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1653: aload_0
        //     1654: getfield 380	com/android/server/PowerManagerService:mPokeLocks	Ljava/util/HashMap;
        //     1657: invokevirtual 1421	java/util/HashMap:values	()Ljava/util/Collection;
        //     1660: invokeinterface 1427 1 0
        //     1665: astore 10
        //     1667: aload 10
        //     1669: invokeinterface 1432 1 0
        //     1674: ifeq +140 -> 1814
        //     1677: aload 10
        //     1679: invokeinterface 1436 1 0
        //     1684: checkcast 45	com/android/server/PowerManagerService$PokeLock
        //     1687: astore 11
        //     1689: new 711	java/lang/StringBuilder
        //     1692: dup
        //     1693: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     1696: ldc_w 1438
        //     1699: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1702: aload 11
        //     1704: getfield 1439	com/android/server/PowerManagerService$PokeLock:tag	Ljava/lang/String;
        //     1707: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1710: ldc_w 1441
        //     1713: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1716: astore 12
        //     1718: iconst_1
        //     1719: aload 11
        //     1721: getfield 1444	com/android/server/PowerManagerService$PokeLock:pokey	I
        //     1724: iand
        //     1725: ifeq +81 -> 1806
        //     1728: ldc_w 1446
        //     1731: astore 13
        //     1733: aload 12
        //     1735: aload 13
        //     1737: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1740: astore 14
        //     1742: iconst_2
        //     1743: aload 11
        //     1745: getfield 1444	com/android/server/PowerManagerService$PokeLock:pokey	I
        //     1748: iand
        //     1749: ifeq +75 -> 1824
        //     1752: ldc_w 1448
        //     1755: astore 15
        //     1757: aload 14
        //     1759: aload 15
        //     1761: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1764: astore 16
        //     1766: iconst_4
        //     1767: aload 11
        //     1769: getfield 1444	com/android/server/PowerManagerService$PokeLock:pokey	I
        //     1772: iand
        //     1773: ifeq +59 -> 1832
        //     1776: ldc_w 1450
        //     1779: astore 17
        //     1781: aload_2
        //     1782: aload 16
        //     1784: aload 17
        //     1786: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     1789: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     1792: invokevirtual 1242	java/io/PrintWriter:println	(Ljava/lang/String;)V
        //     1795: goto -128 -> 1667
        //     1798: astore 7
        //     1800: aload 6
        //     1802: monitorexit
        //     1803: aload 7
        //     1805: athrow
        //     1806: ldc_w 730
        //     1809: astore 13
        //     1811: goto -78 -> 1733
        //     1814: aload_2
        //     1815: invokevirtual 1383	java/io/PrintWriter:println	()V
        //     1818: aload 6
        //     1820: monitorexit
        //     1821: goto -1770 -> 51
        //     1824: ldc_w 730
        //     1827: astore 15
        //     1829: goto -72 -> 1757
        //     1832: ldc_w 730
        //     1835: astore 17
        //     1837: goto -56 -> 1781
        //
        // Exception table:
        //     from	to	target	type
        //     66	1803	1798	finally
        //     1806	1821	1798	finally
    }

    public void enableUserActivity(boolean paramBoolean)
    {
        synchronized (this.mLocks)
        {
            this.mUserActivityAllowed = paramBoolean;
            if (!paramBoolean)
                setTimeoutLocked(SystemClock.uptimeMillis(), 0);
            return;
        }
    }

    void enforceWakeSourcePermission(int paramInt1, int paramInt2)
    {
        if (paramInt1 == Process.myUid());
        while (true)
        {
            return;
            this.mContext.enforcePermission("android.permission.UPDATE_DEVICE_STATS", paramInt2, paramInt1, null);
        }
    }

    WindowManagerPolicy getPolicyLocked()
    {
        while ((this.mPolicy == null) || (!this.mDoneBooting))
            try
            {
                this.mLocks.wait();
            }
            catch (InterruptedException localInterruptedException)
            {
            }
        return this.mPolicy;
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    int getPowerState()
    {
        return this.mPowerState;
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    boolean getProximitySensorActive()
    {
        return this.mProximitySensorActive;
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    Handler getScreenBrightnessHandler()
    {
        return this.mScreenBrightnessHandler;
    }

    int getStayOnConditionsLocked()
    {
        if ((this.mMaximumScreenOffTimeout <= 0) || (this.mMaximumScreenOffTimeout == 2147483647));
        for (int i = this.mStayOnConditions; ; i = 0)
            return i;
    }

    public int getSupportedWakeLockFlags()
    {
        int i = 31;
        if (this.mProximitySensor != null)
            i |= 32;
        return i;
    }

    public void goToSleep(long paramLong)
    {
        goToSleepWithReason(paramLong, 2);
    }

    public void goToSleepWithReason(long paramLong, int paramInt)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        synchronized (this.mLocks)
        {
            goToSleepLocked(paramLong, paramInt);
            return;
        }
    }

    void init(Context paramContext, LightsService paramLightsService, IActivityManager paramIActivityManager, BatteryService paramBatteryService)
    {
        this.mLightsService = paramLightsService;
        this.mContext = paramContext;
        this.mActivityService = paramIActivityManager;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mBatteryService = paramBatteryService;
        this.mLcdLight = paramLightsService.getLight(0);
        this.mButtonLight = paramLightsService.getLight(2);
        this.mKeyboardLight = paramLightsService.getLight(1);
        this.mAttentionLight = paramLightsService.getLight(5);
        this.mHeadless = "1".equals(SystemProperties.get("ro.config.headless", "0"));
        this.mInitComplete = false;
        this.mScreenBrightnessAnimator = new ScreenBrightnessAnimator("mScreenBrightnessUpdaterThread", -4);
        this.mScreenBrightnessAnimator.start();
        synchronized (this.mScreenBrightnessAnimator)
        {
            while (true)
            {
                boolean bool1 = this.mInitComplete;
                if (bool1)
                    break;
                try
                {
                    this.mScreenBrightnessAnimator.wait();
                }
                catch (InterruptedException localInterruptedException2)
                {
                }
            }
            this.mInitComplete = false;
            this.mHandlerThread = new HandlerThread("PowerManagerService")
            {
                protected void onLooperPrepared()
                {
                    super.onLooperPrepared();
                    PowerManagerService.this.initInThread();
                }
            };
            this.mHandlerThread.start();
        }
        synchronized (this.mHandlerThread)
        {
            while (true)
            {
                boolean bool2 = this.mInitComplete;
                if (bool2)
                    break;
                try
                {
                    this.mHandlerThread.wait();
                }
                catch (InterruptedException localInterruptedException1)
                {
                }
            }
            localObject1 = finally;
            throw localObject1;
        }
        synchronized (this.mLocks)
        {
            updateNativePowerStateLocked();
            forceUserActivityLocked();
            this.mInitialized = true;
            return;
            localObject2 = finally;
            throw localObject2;
        }
    }

    void initInThread()
    {
        this.mHandler = new Handler();
        this.mBroadcastWakeLock = new UnsynchronizedWakeLock(1, "sleep_broadcast", true);
        this.mStayOnWhilePluggedInScreenDimLock = new UnsynchronizedWakeLock(6, "StayOnWhilePluggedIn Screen Dim", false);
        this.mStayOnWhilePluggedInPartialLock = new UnsynchronizedWakeLock(1, "StayOnWhilePluggedIn Partial", false);
        this.mPreventScreenOnPartialLock = new UnsynchronizedWakeLock(1, "PreventScreenOn Partial", false);
        this.mProximityPartialLock = new UnsynchronizedWakeLock(1, "Proximity Partial", false);
        this.mScreenOnIntent = new Intent("android.intent.action.SCREEN_ON");
        this.mScreenOnIntent.addFlags(1342177280);
        this.mScreenOffIntent = new Intent("android.intent.action.SCREEN_OFF");
        this.mScreenOffIntent.addFlags(1342177280);
        Resources localResources = this.mContext.getResources();
        this.mAnimateScreenLights = localResources.getBoolean(17891348);
        this.mUnplugTurnsOnScreen = localResources.getBoolean(17891347);
        this.mScreenBrightnessDim = localResources.getInteger(17694754);
        this.mUseSoftwareAutoBrightness = localResources.getBoolean(17891345);
        if (this.mUseSoftwareAutoBrightness)
        {
            this.mAutoBrightnessLevels = localResources.getIntArray(17236007);
            this.mLcdBacklightValues = localResources.getIntArray(17236008);
            this.mButtonBacklightValues = localResources.getIntArray(17236009);
            this.mKeyboardBacklightValues = localResources.getIntArray(17236010);
            this.mLightSensorWarmupTime = localResources.getInteger(17694755);
        }
        ContentResolver localContentResolver = this.mContext.getContentResolver();
        Uri localUri = Settings.System.CONTENT_URI;
        String[] arrayOfString = new String[7];
        arrayOfString[0] = "stay_on_while_plugged_in";
        arrayOfString[1] = "screen_off_timeout";
        arrayOfString[2] = "dim_screen";
        arrayOfString[3] = "screen_brightness";
        arrayOfString[4] = "screen_brightness_mode";
        arrayOfString[5] = "window_animation_scale";
        arrayOfString[6] = "transition_animation_scale";
        this.mSettings = new ContentQueryMap(localContentResolver.query(localUri, null, "(name=?) or (name=?) or (name=?) or (name=?) or (name=?) or (name=?) or (name=?) or (name=?)", arrayOfString, null), "name", true, this.mHandler);
        SettingsObserver localSettingsObserver = new SettingsObserver(null);
        this.mSettings.addObserver(localSettingsObserver);
        localSettingsObserver.update(this.mSettings, null);
        IntentFilter localIntentFilter1 = new IntentFilter();
        localIntentFilter1.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(new BatteryReceiver(null), localIntentFilter1);
        IntentFilter localIntentFilter2 = new IntentFilter();
        localIntentFilter2.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(new BootCompletedReceiver(null), localIntentFilter2);
        IntentFilter localIntentFilter3 = new IntentFilter();
        localIntentFilter3.addAction("android.intent.action.DOCK_EVENT");
        this.mContext.registerReceiver(new DockReceiver(null), localIntentFilter3);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.CONTENT_URI, true, new ContentObserver(new Handler())
        {
            public void onChange(boolean paramAnonymousBoolean)
            {
                PowerManagerService.this.updateSettingsValues();
            }
        });
        updateSettingsValues();
        synchronized (this.mHandlerThread)
        {
            this.mInitComplete = true;
            this.mHandlerThread.notifyAll();
            return;
        }
    }

    boolean isScreenBright()
    {
        while (true)
        {
            synchronized (this.mLocks)
            {
                if ((0x3 & this.mPowerState) == 3)
                {
                    bool = true;
                    return bool;
                }
            }
            boolean bool = false;
        }
    }

    public boolean isScreenOn()
    {
        while (true)
        {
            synchronized (this.mLocks)
            {
                if ((0x1 & this.mPowerState) != 0)
                {
                    bool = true;
                    return bool;
                }
            }
            boolean bool = false;
        }
    }

    void logPointerDownEvent()
    {
        if (this.mLastTouchDown == 0L)
        {
            this.mLastTouchDown = SystemClock.elapsedRealtime();
            this.mTouchCycles = (1 + this.mTouchCycles);
        }
    }

    void logPointerUpEvent()
    {
        this.mTotalTouchDownTime += SystemClock.elapsedRealtime() - this.mLastTouchDown;
        this.mLastTouchDown = 0L;
    }

    public void monitor()
    {
        synchronized (this.mLocks)
        {
        }
    }

    void noteStartWakeLocked(WakeLock paramWakeLock, WorkSource paramWorkSource)
    {
        long l;
        if (paramWakeLock.monitorType >= 0)
        {
            l = Binder.clearCallingIdentity();
            if (paramWorkSource == null)
                break label42;
        }
        try
        {
            this.mBatteryStats.noteStartWakelockFromSource(paramWorkSource, paramWakeLock.pid, paramWakeLock.tag, paramWakeLock.monitorType);
            while (true)
            {
                return;
                label42: this.mBatteryStats.noteStartWakelock(paramWakeLock.uid, paramWakeLock.pid, paramWakeLock.tag, paramWakeLock.monitorType);
            }
        }
        catch (RemoteException localRemoteException)
        {
            while (true)
                Binder.restoreCallingIdentity(l);
        }
        finally
        {
            Binder.restoreCallingIdentity(l);
        }
    }

    void noteStopWakeLocked(WakeLock paramWakeLock, WorkSource paramWorkSource)
    {
        long l;
        if (paramWakeLock.monitorType >= 0)
        {
            l = Binder.clearCallingIdentity();
            if (paramWorkSource == null)
                break label42;
        }
        try
        {
            this.mBatteryStats.noteStopWakelockFromSource(paramWorkSource, paramWakeLock.pid, paramWakeLock.tag, paramWakeLock.monitorType);
            while (true)
            {
                return;
                label42: this.mBatteryStats.noteStopWakelock(paramWakeLock.uid, paramWakeLock.pid, paramWakeLock.tag, paramWakeLock.monitorType);
            }
        }
        catch (RemoteException localRemoteException)
        {
            while (true)
                Binder.restoreCallingIdentity(l);
        }
        finally
        {
            Binder.restoreCallingIdentity(l);
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    public void preventScreenOn(boolean paramBoolean)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        LockList localLockList = this.mLocks;
        if (paramBoolean);
        try
        {
            this.mPreventScreenOnPartialLock.acquire();
            this.mHandler.removeCallbacks(this.mForceReenableScreenTask);
            this.mHandler.postDelayed(this.mForceReenableScreenTask, 5000L);
            this.mPreventScreenOn = true;
            return;
            this.mPreventScreenOn = false;
            this.mHandler.removeCallbacks(this.mForceReenableScreenTask);
            Injector.sleepIfProximitySensorActive(this);
            if ((!this.mProximitySensorActive) && ((0x1 & this.mPowerState) != 0))
            {
                int i = setScreenStateLocked(true);
                if (i != 0)
                    Slog.w("PowerManagerService", "preventScreenOn: error from setScreenStateLocked(): " + i);
            }
            this.mPreventScreenOnPartialLock.release();
        }
        finally
        {
        }
    }

    public void reboot(final String paramString)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
        if ((this.mHandler == null) || (!ActivityManagerNative.isSystemReady()))
            throw new IllegalStateException("Too early to call reboot()");
        Runnable local10 = new Runnable()
        {
            public void run()
            {
                try
                {
                    ShutdownThread.reboot(PowerManagerService.this.mContext, paramString, false);
                    return;
                }
                finally
                {
                    localObject = finally;
                    throw localObject;
                }
            }
        };
        this.mHandler.post(local10);
        try
        {
            while (true)
                label56: local10.wait();
        }
        catch (InterruptedException localInterruptedException)
        {
            break label56;
        }
        finally
        {
        }
    }

    public void releaseWakeLock(IBinder paramIBinder, int paramInt)
    {
        if (Binder.getCallingUid() != Process.myUid())
            this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        synchronized (this.mLocks)
        {
            releaseWakeLockLocked(paramIBinder, paramInt, false);
            return;
        }
    }

    public void setAttentionLight(boolean paramBoolean, int paramInt)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        LightsService.Light localLight = this.mAttentionLight;
        if (paramBoolean);
        for (int i = 3; ; i = 0)
        {
            localLight.setFlashing(paramInt, 2, i, 0);
            return;
        }
    }

    public void setAutoBrightnessAdjustment(float paramFloat)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        synchronized (this.mLocks)
        {
            this.mLightSensorAdjustSetting = paramFloat;
            long l;
            if ((this.mSensorManager != null) && (this.mLightSensorEnabled))
                l = Binder.clearCallingIdentity();
            try
            {
                if (this.mLightSensorValue >= 0.0F)
                {
                    int i = (int)this.mLightSensorValue;
                    this.mLightSensorValue = -1.0F;
                    handleLightSensorValue(i, true);
                }
                Binder.restoreCallingIdentity(l);
                return;
            }
            finally
            {
                localObject2 = finally;
                Binder.restoreCallingIdentity(l);
                throw localObject2;
            }
        }
    }

    public void setBacklightBrightness(int paramInt)
    {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        int j;
        long l;
        synchronized (this.mLocks)
        {
            j = Math.max(paramInt, this.mScreenBrightnessDim);
            this.mLcdLight.setBrightness(j);
            LightsService.Light localLight = this.mKeyboardLight;
            if (this.mKeyboardVisible)
                i = j;
            localLight.setBrightness(i);
            this.mButtonLight.setBrightness(j);
            l = Binder.clearCallingIdentity();
        }
        try
        {
            this.mBatteryStats.noteScreenBrightness(j);
            Binder.restoreCallingIdentity(l);
            this.mScreenBrightnessAnimator.animateTo(j, 2, 0);
            return;
        }
        catch (RemoteException localRemoteException)
        {
            while (true)
            {
                Slog.w("PowerManagerService", "RemoteException calling noteScreenBrightness on BatteryStatsService", localRemoteException);
                Binder.restoreCallingIdentity(l);
            }
            localObject1 = finally;
            throw localObject1;
        }
        finally
        {
            Binder.restoreCallingIdentity(l);
        }
    }

    public void setButtonBrightnessOverride(int paramInt)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        synchronized (this.mLocks)
        {
            if (this.mButtonBrightnessOverride != paramInt)
            {
                this.mButtonBrightnessOverride = paramInt;
                if (isScreenOn())
                    updateLightsLocked(this.mPowerState, 12);
            }
            return;
        }
    }

    public void setKeyboardVisibility(boolean paramBoolean)
    {
        synchronized (this.mLocks)
        {
            if (this.mKeyboardVisible != paramBoolean)
            {
                this.mKeyboardVisible = paramBoolean;
                if ((0x1 & this.mPowerState) != 0)
                {
                    if ((this.mUseSoftwareAutoBrightness) && (this.mLightSensorValue >= 0.0F))
                    {
                        int i = (int)this.mLightSensorValue;
                        this.mLightSensorValue = -1.0F;
                        lightSensorChangedLocked(i, false);
                    }
                    userActivity(SystemClock.uptimeMillis(), false, 1, true);
                }
            }
            return;
        }
    }

    public void setMaximumScreenOffTimeount(int paramInt)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS", null);
        synchronized (this.mLocks)
        {
            this.mMaximumScreenOffTimeout = paramInt;
            setScreenOffTimeoutsLocked();
            return;
        }
    }

    // ERROR //
    public void setPokeLock(int paramInt, IBinder paramIBinder, String paramString)
    {
        // Byte code:
        //     0: aload_0
        //     1: getfield 544	com/android/server/PowerManagerService:mContext	Landroid/content/Context;
        //     4: ldc_w 1191
        //     7: aconst_null
        //     8: invokevirtual 1130	android/content/Context:enforceCallingOrSelfPermission	(Ljava/lang/String;Ljava/lang/String;)V
        //     11: aload_2
        //     12: ifnonnull +36 -> 48
        //     15: ldc 128
        //     17: new 711	java/lang/StringBuilder
        //     20: dup
        //     21: invokespecial 712	java/lang/StringBuilder:<init>	()V
        //     24: ldc_w 1710
        //     27: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     30: aload_3
        //     31: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     34: ldc_w 1402
        //     37: invokevirtual 718	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //     40: invokevirtual 728	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //     43: invokestatic 1155	android/util/Slog:e	(Ljava/lang/String;Ljava/lang/String;)I
        //     46: pop
        //     47: return
        //     48: iload_1
        //     49: bipush 6
        //     51: iand
        //     52: bipush 6
        //     54: if_icmpne +14 -> 68
        //     57: new 1712	java/lang/IllegalArgumentException
        //     60: dup
        //     61: ldc_w 1714
        //     64: invokespecial 1715	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
        //     67: athrow
        //     68: aload_0
        //     69: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     72: astore 4
        //     74: aload 4
        //     76: monitorenter
        //     77: iload_1
        //     78: ifeq +179 -> 257
        //     81: aload_0
        //     82: getfield 380	com/android/server/PowerManagerService:mPokeLocks	Ljava/util/HashMap;
        //     85: aload_2
        //     86: invokevirtual 1718	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
        //     89: checkcast 45	com/android/server/PowerManagerService$PokeLock
        //     92: astore 13
        //     94: iconst_0
        //     95: istore 14
        //     97: aload 13
        //     99: ifnull +123 -> 222
        //     102: aload 13
        //     104: getfield 1444	com/android/server/PowerManagerService$PokeLock:pokey	I
        //     107: istore 14
        //     109: aload 13
        //     111: iload_1
        //     112: putfield 1444	com/android/server/PowerManagerService$PokeLock:pokey	I
        //     115: iload 14
        //     117: bipush 6
        //     119: iand
        //     120: istore 16
        //     122: iload_1
        //     123: bipush 6
        //     125: iand
        //     126: istore 17
        //     128: iconst_1
        //     129: aload_0
        //     130: getfield 444	com/android/server/PowerManagerService:mPowerState	I
        //     133: iand
        //     134: ifne +16 -> 150
        //     137: iload 16
        //     139: iload 17
        //     141: if_icmpeq +9 -> 150
        //     144: aload 13
        //     146: iconst_1
        //     147: putfield 1721	com/android/server/PowerManagerService$PokeLock:awakeOnSet	Z
        //     150: aload_0
        //     151: getfield 371	com/android/server/PowerManagerService:mPokey	I
        //     154: istore 8
        //     156: iconst_0
        //     157: istore 9
        //     159: iconst_0
        //     160: istore 10
        //     162: aload_0
        //     163: getfield 380	com/android/server/PowerManagerService:mPokeLocks	Ljava/util/HashMap;
        //     166: invokevirtual 1421	java/util/HashMap:values	()Ljava/util/Collection;
        //     169: invokeinterface 1427 1 0
        //     174: astore 11
        //     176: aload 11
        //     178: invokeinterface 1432 1 0
        //     183: ifeq +105 -> 288
        //     186: aload 11
        //     188: invokeinterface 1436 1 0
        //     193: checkcast 45	com/android/server/PowerManagerService$PokeLock
        //     196: astore 12
        //     198: iload 9
        //     200: aload 12
        //     202: getfield 1444	com/android/server/PowerManagerService$PokeLock:pokey	I
        //     205: ior
        //     206: istore 9
        //     208: aload 12
        //     210: getfield 1721	com/android/server/PowerManagerService$PokeLock:awakeOnSet	Z
        //     213: ifeq -37 -> 176
        //     216: iconst_1
        //     217: istore 10
        //     219: goto -43 -> 176
        //     222: new 45	com/android/server/PowerManagerService$PokeLock
        //     225: dup
        //     226: aload_0
        //     227: iload_1
        //     228: aload_2
        //     229: aload_3
        //     230: invokespecial 1724	com/android/server/PowerManagerService$PokeLock:<init>	(Lcom/android/server/PowerManagerService;ILandroid/os/IBinder;Ljava/lang/String;)V
        //     233: astore 13
        //     235: aload_0
        //     236: getfield 380	com/android/server/PowerManagerService:mPokeLocks	Ljava/util/HashMap;
        //     239: aload_2
        //     240: aload 13
        //     242: invokevirtual 1728	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
        //     245: pop
        //     246: goto -131 -> 115
        //     249: astore 7
        //     251: aload 4
        //     253: monitorexit
        //     254: aload 7
        //     256: athrow
        //     257: aload_0
        //     258: getfield 380	com/android/server/PowerManagerService:mPokeLocks	Ljava/util/HashMap;
        //     261: aload_2
        //     262: invokevirtual 1731	java/util/HashMap:remove	(Ljava/lang/Object;)Ljava/lang/Object;
        //     265: checkcast 45	com/android/server/PowerManagerService$PokeLock
        //     268: astore 5
        //     270: aload 5
        //     272: ifnull -122 -> 150
        //     275: aload_2
        //     276: aload 5
        //     278: iconst_0
        //     279: invokeinterface 921 3 0
        //     284: pop
        //     285: goto -135 -> 150
        //     288: aload_0
        //     289: iload 9
        //     291: putfield 371	com/android/server/PowerManagerService:mPokey	I
        //     294: aload_0
        //     295: iload 10
        //     297: putfield 373	com/android/server/PowerManagerService:mPokeAwakeOnSet	Z
        //     300: iload 8
        //     302: bipush 6
        //     304: iand
        //     305: iload_1
        //     306: bipush 6
        //     308: iand
        //     309: if_icmpeq +21 -> 330
        //     312: aload_0
        //     313: invokespecial 489	com/android/server/PowerManagerService:setScreenOffTimeoutsLocked	()V
        //     316: aload_0
        //     317: invokestatic 765	android/os/SystemClock:uptimeMillis	()J
        //     320: aload_0
        //     321: getfield 340	com/android/server/PowerManagerService:mTimeoutTask	Lcom/android/server/PowerManagerService$TimeoutTask;
        //     324: getfield 694	com/android/server/PowerManagerService$TimeoutTask:nextState	I
        //     327: invokespecial 1453	com/android/server/PowerManagerService:setTimeoutLocked	(JI)V
        //     330: aload 4
        //     332: monitorexit
        //     333: goto -286 -> 47
        //
        // Exception table:
        //     from	to	target	type
        //     81	254	249	finally
        //     257	333	249	finally
    }

    public void setPolicy(WindowManagerPolicy paramWindowManagerPolicy)
    {
        synchronized (this.mLocks)
        {
            this.mPolicy = paramWindowManagerPolicy;
            this.mLocks.notifyAll();
            return;
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    void setProxIgnoredBecauseScreenTurnedOff(boolean paramBoolean)
    {
        this.mProxIgnoredBecauseScreenTurnedOff = paramBoolean;
    }

    public void setScreenBrightnessOverride(int paramInt)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        synchronized (this.mLocks)
        {
            if (this.mScreenBrightnessOverride != paramInt)
            {
                this.mScreenBrightnessOverride = paramInt;
                if (isScreenOn())
                    updateLightsLocked(this.mPowerState, 1);
            }
            return;
        }
    }

    public void setStayOnSetting(int paramInt)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SETTINGS", null);
        Settings.System.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", paramInt);
    }

    // ERROR //
    void systemReady()
    {
        // Byte code:
        //     0: iconst_1
        //     1: istore_1
        //     2: aload_0
        //     3: new 1745	android/hardware/SystemSensorManager
        //     6: dup
        //     7: aload_0
        //     8: getfield 1521	com/android/server/PowerManagerService:mHandlerThread	Landroid/os/HandlerThread;
        //     11: invokevirtual 1749	android/os/HandlerThread:getLooper	()Landroid/os/Looper;
        //     14: invokespecial 1752	android/hardware/SystemSensorManager:<init>	(Landroid/os/Looper;)V
        //     17: putfield 697	com/android/server/PowerManagerService:mSensorManager	Landroid/hardware/SensorManager;
        //     20: aload_0
        //     21: aload_0
        //     22: getfield 697	com/android/server/PowerManagerService:mSensorManager	Landroid/hardware/SensorManager;
        //     25: bipush 8
        //     27: invokevirtual 1756	android/hardware/SensorManager:getDefaultSensor	(I)Landroid/hardware/Sensor;
        //     30: putfield 658	com/android/server/PowerManagerService:mProximitySensor	Landroid/hardware/Sensor;
        //     33: aload_0
        //     34: getfield 677	com/android/server/PowerManagerService:mUseSoftwareAutoBrightness	Z
        //     37: ifeq +15 -> 52
        //     40: aload_0
        //     41: aload_0
        //     42: getfield 697	com/android/server/PowerManagerService:mSensorManager	Landroid/hardware/SensorManager;
        //     45: iconst_5
        //     46: invokevirtual 1756	android/hardware/SensorManager:getDefaultSensor	(I)Landroid/hardware/Sensor;
        //     49: putfield 735	com/android/server/PowerManagerService:mLightSensor	Landroid/hardware/Sensor;
        //     52: aload_0
        //     53: getfield 677	com/android/server/PowerManagerService:mUseSoftwareAutoBrightness	Z
        //     56: ifeq +83 -> 139
        //     59: aload_0
        //     60: iconst_3
        //     61: invokespecial 511	com/android/server/PowerManagerService:setPowerState	(I)V
        //     64: aload_0
        //     65: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     68: astore_2
        //     69: aload_2
        //     70: monitorenter
        //     71: ldc 128
        //     73: ldc_w 1758
        //     76: invokestatic 903	android/util/Slog:d	(Ljava/lang/String;Ljava/lang/String;)I
        //     79: pop
        //     80: aload_0
        //     81: iconst_1
        //     82: putfield 297	com/android/server/PowerManagerService:mDoneBooting	Z
        //     85: aload_0
        //     86: getfield 677	com/android/server/PowerManagerService:mUseSoftwareAutoBrightness	Z
        //     89: ifeq +59 -> 148
        //     92: aload_0
        //     93: getfield 576	com/android/server/PowerManagerService:mAutoBrightessEnabled	Z
        //     96: ifeq +52 -> 148
        //     99: aload_0
        //     100: iload_1
        //     101: invokespecial 1028	com/android/server/PowerManagerService:enableLightSensorLocked	(Z)V
        //     104: invokestatic 425	android/os/Binder:clearCallingIdentity	()J
        //     107: lstore 5
        //     109: aload_0
        //     110: getfield 999	com/android/server/PowerManagerService:mBatteryStats	Lcom/android/internal/app/IBatteryStats;
        //     113: aload_0
        //     114: invokespecial 1001	com/android/server/PowerManagerService:getPreferredBrightness	()I
        //     117: invokeinterface 1006 2 0
        //     122: aload_0
        //     123: getfield 999	com/android/server/PowerManagerService:mBatteryStats	Lcom/android/internal/app/IBatteryStats;
        //     126: invokeinterface 1009 1 0
        //     131: lload 5
        //     133: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     136: aload_2
        //     137: monitorexit
        //     138: return
        //     139: aload_0
        //     140: bipush 15
        //     142: invokespecial 511	com/android/server/PowerManagerService:setPowerState	(I)V
        //     145: goto -81 -> 64
        //     148: iconst_0
        //     149: istore_1
        //     150: goto -51 -> 99
        //     153: astore 8
        //     155: lload 5
        //     157: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     160: goto -24 -> 136
        //     163: astore_3
        //     164: aload_2
        //     165: monitorexit
        //     166: aload_3
        //     167: athrow
        //     168: astore 7
        //     170: lload 5
        //     172: invokestatic 442	android/os/Binder:restoreCallingIdentity	(J)V
        //     175: aload 7
        //     177: athrow
        //
        // Exception table:
        //     from	to	target	type
        //     109	131	153	android/os/RemoteException
        //     71	109	163	finally
        //     131	138	163	finally
        //     155	166	163	finally
        //     170	178	163	finally
        //     109	131	168	finally
    }

    public long timeSinceScreenOn()
    {
        long l;
        synchronized (this.mLocks)
        {
            if ((0x1 & this.mPowerState) != 0)
                l = 0L;
            else
                l = SystemClock.elapsedRealtime() - this.mScreenOffTime;
        }
        return l;
    }

    // ERROR //
    public void updateWakeLockWorkSource(IBinder paramIBinder, WorkSource paramWorkSource)
    {
        // Byte code:
        //     0: invokestatic 1111	android/os/Binder:getCallingUid	()I
        //     3: istore_3
        //     4: invokestatic 1124	android/os/Binder:getCallingPid	()I
        //     7: istore 4
        //     9: aload_2
        //     10: ifnull +12 -> 22
        //     13: aload_2
        //     14: invokevirtual 1140	android/os/WorkSource:size	()I
        //     17: ifne +5 -> 22
        //     20: aconst_null
        //     21: astore_2
        //     22: aload_2
        //     23: ifnull +10 -> 33
        //     26: aload_0
        //     27: iload_3
        //     28: iload 4
        //     30: invokevirtual 1133	com/android/server/PowerManagerService:enforceWakeSourcePermission	(II)V
        //     33: aload_0
        //     34: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     37: astore 5
        //     39: aload 5
        //     41: monitorenter
        //     42: aload_0
        //     43: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     46: aload_1
        //     47: invokevirtual 1144	com/android/server/PowerManagerService$LockList:getIndex	(Landroid/os/IBinder;)I
        //     50: istore 7
        //     52: iload 7
        //     54: ifge +22 -> 76
        //     57: new 1712	java/lang/IllegalArgumentException
        //     60: dup
        //     61: ldc_w 1763
        //     64: invokespecial 1715	java/lang/IllegalArgumentException:<init>	(Ljava/lang/String;)V
        //     67: athrow
        //     68: astore 6
        //     70: aload 5
        //     72: monitorexit
        //     73: aload 6
        //     75: athrow
        //     76: aload_0
        //     77: getfield 337	com/android/server/PowerManagerService:mLocks	Lcom/android/server/PowerManagerService$LockList;
        //     80: iload 7
        //     82: invokevirtual 806	com/android/server/PowerManagerService$LockList:get	(I)Ljava/lang/Object;
        //     85: checkcast 48	com/android/server/PowerManagerService$WakeLock
        //     88: astore 8
        //     90: aload 8
        //     92: getfield 925	com/android/server/PowerManagerService$WakeLock:ws	Landroid/os/WorkSource;
        //     95: astore 9
        //     97: aload_2
        //     98: ifnull +39 -> 137
        //     101: new 1139	android/os/WorkSource
        //     104: dup
        //     105: aload_2
        //     106: invokespecial 1165	android/os/WorkSource:<init>	(Landroid/os/WorkSource;)V
        //     109: astore 10
        //     111: aload 8
        //     113: aload 10
        //     115: putfield 925	com/android/server/PowerManagerService$WakeLock:ws	Landroid/os/WorkSource;
        //     118: aload_0
        //     119: aload 8
        //     121: aload 9
        //     123: invokevirtual 929	com/android/server/PowerManagerService:noteStopWakeLocked	(Lcom/android/server/PowerManagerService$WakeLock;Landroid/os/WorkSource;)V
        //     126: aload_0
        //     127: aload 8
        //     129: aload_2
        //     130: invokevirtual 1170	com/android/server/PowerManagerService:noteStartWakeLocked	(Lcom/android/server/PowerManagerService$WakeLock;Landroid/os/WorkSource;)V
        //     133: aload 5
        //     135: monitorexit
        //     136: return
        //     137: aconst_null
        //     138: astore 10
        //     140: goto -29 -> 111
        //
        // Exception table:
        //     from	to	target	type
        //     42	73	68	finally
        //     76	136	68	finally
    }

    public void userActivity(long paramLong, boolean paramBoolean)
    {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0)
            if (shouldLog(paramLong))
                Slog.w("PowerManagerService", "Caller does not have DEVICE_POWER permission.    pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid());
        while (true)
        {
            return;
            userActivity(paramLong, -1L, paramBoolean, 0, false, false);
        }
    }

    public void userActivity(long paramLong, boolean paramBoolean, int paramInt)
    {
        userActivity(paramLong, -1L, paramBoolean, paramInt, false, false);
    }

    public void userActivity(long paramLong, boolean paramBoolean1, int paramInt, boolean paramBoolean2)
    {
        userActivity(paramLong, -1L, paramBoolean1, paramInt, paramBoolean2, false);
    }

    public void userActivityWithForce(long paramLong, boolean paramBoolean1, boolean paramBoolean2)
    {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        userActivity(paramLong, -1L, paramBoolean1, 0, paramBoolean2, false);
    }

    private class LockList extends ArrayList<PowerManagerService.WakeLock>
    {
        private LockList()
        {
        }

        void addLock(PowerManagerService.WakeLock paramWakeLock)
        {
            if (getIndex(paramWakeLock.binder) < 0)
                add(paramWakeLock);
        }

        int gatherState()
        {
            int i = 0;
            int j = size();
            for (int k = 0; k < j; k++)
            {
                PowerManagerService.WakeLock localWakeLock = (PowerManagerService.WakeLock)get(k);
                if ((localWakeLock.activated) && (PowerManagerService.this.isScreenLock(localWakeLock.flags)))
                    i |= localWakeLock.minState;
            }
            return i;
        }

        int getIndex(IBinder paramIBinder)
        {
            int i = size();
            int j = 0;
            if (j < i)
                if (((PowerManagerService.WakeLock)get(j)).binder != paramIBinder);
            while (true)
            {
                return j;
                j++;
                break;
                j = -1;
            }
        }

        int reactivateScreenLocksLocked()
        {
            int i = 0;
            int j = size();
            for (int k = 0; k < j; k++)
            {
                PowerManagerService.WakeLock localWakeLock = (PowerManagerService.WakeLock)get(k);
                if (PowerManagerService.this.isScreenLock(localWakeLock.flags))
                {
                    localWakeLock.activated = true;
                    i |= localWakeLock.minState;
                }
            }
            PowerManagerService.access$6702(PowerManagerService.this, false);
            return i;
        }

        PowerManagerService.WakeLock removeLock(IBinder paramIBinder)
        {
            int i = getIndex(paramIBinder);
            if (i >= 0);
            for (PowerManagerService.WakeLock localWakeLock = (PowerManagerService.WakeLock)remove(i); ; localWakeLock = null)
                return localWakeLock;
        }
    }

    class ScreenBrightnessAnimator extends HandlerThread
    {
        static final int ANIMATE_LIGHTS = 10;
        static final int ANIMATE_POWER_OFF = 11;
        private int currentMask;
        volatile int currentValue;
        private int duration;
        volatile int endSensorValue;
        volatile int endValue;
        private final String prefix;
        volatile int startSensorValue;
        private long startTimeMillis;
        volatile int startValue;

        public ScreenBrightnessAnimator(String paramInt, int arg3)
        {
            super(i);
            this.prefix = paramInt;
        }

        // ERROR //
        private void animateInternal(int paramInt1, boolean paramBoolean, int paramInt2)
        {
            // Byte code:
            //     0: iconst_0
            //     1: istore 4
            //     3: aload_0
            //     4: monitorenter
            //     5: aload_0
            //     6: getfield 48	com/android/server/PowerManagerService$ScreenBrightnessAnimator:currentValue	I
            //     9: aload_0
            //     10: getfield 50	com/android/server/PowerManagerService$ScreenBrightnessAnimator:endValue	I
            //     13: if_icmpeq +242 -> 255
            //     16: invokestatic 56	android/os/SystemClock:elapsedRealtime	()J
            //     19: aload_0
            //     20: getfield 58	com/android/server/PowerManagerService$ScreenBrightnessAnimator:startTimeMillis	J
            //     23: lsub
            //     24: l2i
            //     25: istore 6
            //     27: iload 6
            //     29: aload_0
            //     30: getfield 60	com/android/server/PowerManagerService$ScreenBrightnessAnimator:duration	I
            //     33: if_icmpge +225 -> 258
            //     36: aload_0
            //     37: getfield 50	com/android/server/PowerManagerService$ScreenBrightnessAnimator:endValue	I
            //     40: aload_0
            //     41: getfield 62	com/android/server/PowerManagerService$ScreenBrightnessAnimator:startValue	I
            //     44: isub
            //     45: istore 13
            //     47: sipush 255
            //     50: iconst_0
            //     51: aload_0
            //     52: getfield 62	com/android/server/PowerManagerService$ScreenBrightnessAnimator:startValue	I
            //     55: iload 13
            //     57: iload 6
            //     59: imul
            //     60: aload_0
            //     61: getfield 60	com/android/server/PowerManagerService$ScreenBrightnessAnimator:duration	I
            //     64: idiv
            //     65: iadd
            //     66: invokestatic 68	java/lang/Math:max	(II)I
            //     69: invokestatic 71	java/lang/Math:min	(II)I
            //     72: istore 7
            //     74: iload_3
            //     75: ifle +49 -> 124
            //     78: iload 7
            //     80: aload_0
            //     81: getfield 48	com/android/server/PowerManagerService$ScreenBrightnessAnimator:currentValue	I
            //     84: if_icmpne +40 -> 124
            //     87: aload_0
            //     88: getfield 60	com/android/server/PowerManagerService$ScreenBrightnessAnimator:duration	I
            //     91: iload 13
            //     93: invokestatic 75	java/lang/Math:abs	(I)I
            //     96: idiv
            //     97: istore 16
            //     99: aload_0
            //     100: getfield 60	com/android/server/PowerManagerService$ScreenBrightnessAnimator:duration	I
            //     103: iload 6
            //     105: isub
            //     106: iload 16
            //     108: invokestatic 71	java/lang/Math:min	(II)I
            //     111: istore_3
            //     112: iload 13
            //     114: ifge +214 -> 328
            //     117: bipush 255
            //     119: istore 17
            //     121: goto +197 -> 318
            //     124: aload_0
            //     125: getfield 77	com/android/server/PowerManagerService$ScreenBrightnessAnimator:endSensorValue	I
            //     128: aload_0
            //     129: getfield 79	com/android/server/PowerManagerService$ScreenBrightnessAnimator:startSensorValue	I
            //     132: isub
            //     133: istore 14
            //     135: aload_0
            //     136: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     139: aload_0
            //     140: getfield 79	com/android/server/PowerManagerService$ScreenBrightnessAnimator:startSensorValue	I
            //     143: iload 14
            //     145: iload 6
            //     147: imul
            //     148: aload_0
            //     149: getfield 60	com/android/server/PowerManagerService$ScreenBrightnessAnimator:duration	I
            //     152: idiv
            //     153: iadd
            //     154: invokestatic 83	com/android/server/PowerManagerService:access$5302	(Lcom/android/server/PowerManagerService;I)I
            //     157: pop
            //     158: iload_2
            //     159: ifeq +52 -> 211
            //     162: aload_0
            //     163: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     166: invokestatic 87	com/android/server/PowerManagerService:access$5400	(Lcom/android/server/PowerManagerService;)Z
            //     169: ifne +42 -> 211
            //     172: aload_0
            //     173: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     176: getfield 91	com/android/server/PowerManagerService:mAnimateScreenLights	Z
            //     179: ifne +32 -> 211
            //     182: aload_0
            //     183: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     186: invokestatic 95	com/android/server/PowerManagerService:access$5500	(Lcom/android/server/PowerManagerService;)I
            //     189: iconst_4
            //     190: if_icmpne +112 -> 302
            //     193: aload_0
            //     194: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     197: invokestatic 99	com/android/server/PowerManagerService:access$4400	(Lcom/android/server/PowerManagerService;)Landroid/os/Handler;
            //     200: bipush 11
            //     202: iload 4
            //     204: iconst_0
            //     205: invokevirtual 105	android/os/Handler:obtainMessage	(III)Landroid/os/Message;
            //     208: invokevirtual 111	android/os/Message:sendToTarget	()V
            //     211: aload_0
            //     212: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     215: invokestatic 99	com/android/server/PowerManagerService:access$4400	(Lcom/android/server/PowerManagerService;)Landroid/os/Handler;
            //     218: bipush 10
            //     220: invokevirtual 115	android/os/Handler:removeMessages	(I)V
            //     223: aload_0
            //     224: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     227: invokestatic 99	com/android/server/PowerManagerService:access$4400	(Lcom/android/server/PowerManagerService;)Landroid/os/Handler;
            //     230: bipush 10
            //     232: iload_1
            //     233: iload 7
            //     235: invokevirtual 105	android/os/Handler:obtainMessage	(III)Landroid/os/Message;
            //     238: astore 10
            //     240: aload_0
            //     241: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     244: invokestatic 99	com/android/server/PowerManagerService:access$4400	(Lcom/android/server/PowerManagerService;)Landroid/os/Handler;
            //     247: aload 10
            //     249: iload_3
            //     250: i2l
            //     251: invokevirtual 119	android/os/Handler:sendMessageDelayed	(Landroid/os/Message;J)Z
            //     254: pop
            //     255: aload_0
            //     256: monitorexit
            //     257: return
            //     258: aload_0
            //     259: getfield 50	com/android/server/PowerManagerService$ScreenBrightnessAnimator:endValue	I
            //     262: istore 7
            //     264: aload_0
            //     265: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     268: aload_0
            //     269: getfield 77	com/android/server/PowerManagerService$ScreenBrightnessAnimator:endSensorValue	I
            //     272: invokestatic 83	com/android/server/PowerManagerService:access$5302	(Lcom/android/server/PowerManagerService;I)I
            //     275: pop
            //     276: aload_0
            //     277: getfield 50	com/android/server/PowerManagerService$ScreenBrightnessAnimator:endValue	I
            //     280: ifle -122 -> 158
            //     283: aload_0
            //     284: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     287: iconst_0
            //     288: invokestatic 123	com/android/server/PowerManagerService:access$4602	(Lcom/android/server/PowerManagerService;Z)Z
            //     291: pop
            //     292: goto -134 -> 158
            //     295: astore 5
            //     297: aload_0
            //     298: monitorexit
            //     299: aload 5
            //     301: athrow
            //     302: aload_0
            //     303: getfield 31	com/android/server/PowerManagerService$ScreenBrightnessAnimator:this$0	Lcom/android/server/PowerManagerService;
            //     306: invokestatic 126	com/android/server/PowerManagerService:access$2000	(Lcom/android/server/PowerManagerService;)I
            //     309: istore 12
            //     311: iload 12
            //     313: istore 4
            //     315: goto -122 -> 193
            //     318: iload 7
            //     320: iload 17
            //     322: iadd
            //     323: istore 7
            //     325: goto -201 -> 124
            //     328: iconst_1
            //     329: istore 17
            //     331: goto -13 -> 318
            //
            // Exception table:
            //     from	to	target	type
            //     5	299	295	finally
            //     302	311	295	finally
        }

        public void animateTo(int paramInt1, int paramInt2, int paramInt3)
        {
            animateTo(paramInt1, PowerManagerService.this.mHighestLightSensorValue, paramInt2, paramInt3);
        }

        public void animateTo(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
        {
            boolean bool1 = true;
            if (((paramInt3 & 0x2) != 0) || ((paramInt3 & 0x4) != 0));
            while (true)
            {
                try
                {
                    PowerManagerService.this.mButtonLight.setBrightness(paramInt1);
                    if ((paramInt3 & 0x8) != 0)
                        PowerManagerService.this.mKeyboardLight.setBrightness(paramInt1);
                    break;
                    if ((isAnimating()) && ((paramInt3 ^ this.currentMask) != 0))
                        cancelAnimation();
                    if (PowerManagerService.this.mInitialAnimation)
                    {
                        paramInt4 = 0;
                        if (paramInt1 > 0)
                            PowerManagerService.access$4602(PowerManagerService.this, false);
                    }
                    this.startValue = this.currentValue;
                    this.endValue = paramInt1;
                    this.startSensorValue = PowerManagerService.this.mHighestLightSensorValue;
                    this.endSensorValue = paramInt2;
                    this.currentMask = paramInt3;
                    this.duration = ((int)(PowerManagerService.this.mWindowScaleAnimation * paramInt4));
                    this.startTimeMillis = SystemClock.elapsedRealtime();
                    if (paramInt1 != this.currentValue)
                    {
                        if ((paramInt3 & 0x3) == 0)
                            break label247;
                        bool2 = bool1;
                        if (this.endValue != 0)
                            break label253;
                        if ((bool1) && (bool2))
                        {
                            PowerManagerService.this.mScreenBrightnessHandler.removeCallbacksAndMessages(null);
                            PowerManagerService.this.screenOffFinishedAnimatingLocked(PowerManagerService.this.mScreenOffReason);
                            this.duration = 200;
                        }
                        if (bool2)
                            animateInternal(paramInt3, bool1, 0);
                    }
                    break;
                }
                finally
                {
                    localObject = finally;
                    throw localObject;
                }
                label247: boolean bool2 = false;
                continue;
                label253: bool1 = false;
            }
        }

        public void cancelAnimation()
        {
            animateTo(this.endValue, this.currentMask, 0);
        }

        public void dump(PrintWriter paramPrintWriter, String paramString)
        {
            paramPrintWriter.println(paramString);
            paramPrintWriter.println("    animating: start:" + this.startValue + ", end:" + this.endValue + ", duration:" + this.duration + ", current:" + this.currentValue);
            paramPrintWriter.println("    startSensorValue:" + this.startSensorValue + " endSensorValue:" + this.endSensorValue);
            paramPrintWriter.println("    startTimeMillis:" + this.startTimeMillis + " now:" + SystemClock.elapsedRealtime());
            paramPrintWriter.println("    currentMask:" + PowerManagerService.dumpPowerState(this.currentMask));
        }

        public int getCurrentBrightness()
        {
            try
            {
                int i = this.currentValue;
                return i;
            }
            finally
            {
                localObject = finally;
                throw localObject;
            }
        }

        public boolean isAnimating()
        {
            while (true)
            {
                try
                {
                    if (this.currentValue != this.endValue)
                    {
                        bool = true;
                        return bool;
                    }
                }
                finally
                {
                    localObject = finally;
                    throw localObject;
                }
                boolean bool = false;
            }
        }

        protected void onLooperPrepared()
        {
            PowerManagerService.access$4402(PowerManagerService.this, new Handler()
            {
                public void handleMessage(Message paramAnonymousMessage)
                {
                    int i;
                    int k;
                    int m;
                    int n;
                    if ((PowerManagerService.this.mAutoBrightessEnabled) && (!PowerManagerService.this.mInitialAnimation))
                    {
                        i = 1;
                        if (paramAnonymousMessage.what != 10)
                            break label232;
                        k = paramAnonymousMessage.arg1;
                        m = paramAnonymousMessage.arg2;
                        long l1 = SystemClock.uptimeMillis();
                        if ((k & 0x2) != 0)
                            PowerManagerService.this.mLcdLight.setBrightness(m, i);
                        long l2 = SystemClock.uptimeMillis() - l1;
                        if ((k & 0x4) != 0)
                            PowerManagerService.this.mButtonLight.setBrightness(m);
                        if ((k & 0x8) != 0)
                            PowerManagerService.this.mKeyboardLight.setBrightness(m);
                        if (l2 > 100L)
                            Slog.e("PowerManagerService", "Excessive delay setting brightness: " + l2 + "ms, mask=" + k);
                        if (l2 >= 16L)
                            break label219;
                        n = 16;
                    }
                    while (true)
                    {
                        try
                        {
                            label188: PowerManagerService.ScreenBrightnessAnimator.this.currentValue = m;
                            PowerManagerService.ScreenBrightnessAnimator.this.animateInternal(k, false, n);
                            return;
                            i = 0;
                            break;
                            label219: n = 1;
                            break label188;
                        }
                        finally
                        {
                        }
                        label232: if (paramAnonymousMessage.what == 11)
                        {
                            int j = paramAnonymousMessage.arg1;
                            PowerManagerService.this.nativeStartSurfaceFlingerAnimation(j);
                        }
                    }
                }
            });
            try
            {
                PowerManagerService.access$5202(PowerManagerService.this, true);
                notifyAll();
                return;
            }
            finally
            {
                localObject = finally;
                throw localObject;
            }
        }
    }

    private class TimeoutTask
        implements Runnable
    {
        int nextState;
        long remainingTimeoutOverride;

        private TimeoutTask()
        {
        }

        // ERROR //
        public void run()
        {
            // Byte code:
            //     0: aload_0
            //     1: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     4: invokestatic 30	com/android/server/PowerManagerService:access$500	(Lcom/android/server/PowerManagerService;)Lcom/android/server/PowerManagerService$LockList;
            //     7: astore_1
            //     8: aload_1
            //     9: monitorenter
            //     10: aload_0
            //     11: getfield 32	com/android/server/PowerManagerService$TimeoutTask:nextState	I
            //     14: bipush 255
            //     16: if_icmpne +8 -> 24
            //     19: aload_1
            //     20: monitorexit
            //     21: goto +125 -> 146
            //     24: aload_0
            //     25: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     28: aload_0
            //     29: getfield 32	com/android/server/PowerManagerService$TimeoutTask:nextState	I
            //     32: invokestatic 36	com/android/server/PowerManagerService:access$2602	(Lcom/android/server/PowerManagerService;I)I
            //     35: pop
            //     36: aload_0
            //     37: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     40: aload_0
            //     41: getfield 32	com/android/server/PowerManagerService$TimeoutTask:nextState	I
            //     44: aload_0
            //     45: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     48: invokestatic 40	com/android/server/PowerManagerService:access$2700	(Lcom/android/server/PowerManagerService;)I
            //     51: ior
            //     52: invokestatic 44	com/android/server/PowerManagerService:access$2800	(Lcom/android/server/PowerManagerService;I)V
            //     55: invokestatic 50	android/os/SystemClock:uptimeMillis	()J
            //     58: lstore 4
            //     60: aload_0
            //     61: getfield 32	com/android/server/PowerManagerService$TimeoutTask:nextState	I
            //     64: tableswitch	default:+28 -> 92, 1:+65->129, 2:+28->92, 3:+38->102
            //     93: monitorexit
            //     94: goto +52 -> 146
            //     97: astore_2
            //     98: aload_1
            //     99: monitorexit
            //     100: aload_2
            //     101: athrow
            //     102: aload_0
            //     103: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     106: invokestatic 53	com/android/server/PowerManagerService:access$2900	(Lcom/android/server/PowerManagerService;)I
            //     109: iflt +20 -> 129
            //     112: aload_0
            //     113: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     116: lload 4
            //     118: aload_0
            //     119: getfield 55	com/android/server/PowerManagerService$TimeoutTask:remainingTimeoutOverride	J
            //     122: iconst_1
            //     123: invokestatic 59	com/android/server/PowerManagerService:access$3000	(Lcom/android/server/PowerManagerService;JJI)V
            //     126: goto -34 -> 92
            //     129: aload_0
            //     130: getfield 19	com/android/server/PowerManagerService$TimeoutTask:this$0	Lcom/android/server/PowerManagerService;
            //     133: lload 4
            //     135: aload_0
            //     136: getfield 55	com/android/server/PowerManagerService$TimeoutTask:remainingTimeoutOverride	J
            //     139: iconst_0
            //     140: invokestatic 59	com/android/server/PowerManagerService:access$3000	(Lcom/android/server/PowerManagerService;JJI)V
            //     143: goto -51 -> 92
            //     146: return
            //
            // Exception table:
            //     from	to	target	type
            //     10	100	97	finally
            //     102	143	97	finally
        }
    }

    private class PokeLock
        implements IBinder.DeathRecipient
    {
        boolean awakeOnSet;
        IBinder binder;
        int pokey;
        String tag;

        PokeLock(int paramIBinder, IBinder paramString, String arg4)
        {
            this.pokey = paramIBinder;
            this.binder = paramString;
            Object localObject;
            this.tag = localObject;
            try
            {
                paramString.linkToDeath(this, 0);
                return;
            }
            catch (RemoteException localRemoteException)
            {
                while (true)
                    binderDied();
            }
        }

        public void binderDied()
        {
            PowerManagerService.this.setPokeLock(0, this.binder, this.tag);
        }
    }

    private class WakeLock
        implements IBinder.DeathRecipient
    {
        boolean activated = true;
        final IBinder binder;
        final int flags;
        int minState;
        final int monitorType;
        final int pid;
        final String tag;
        final int uid;
        WorkSource ws;

        WakeLock(int paramIBinder, IBinder paramString, String paramInt1, int paramInt2, int arg6)
        {
            this.flags = paramIBinder;
            this.binder = paramString;
            this.tag = paramInt1;
            int j;
            if (paramInt2 == PowerManagerService.this.MY_UID)
                j = 1000;
            while (true)
            {
                this.uid = j;
                int i;
                this.pid = i;
                int k;
                if ((paramInt2 != PowerManagerService.this.MY_UID) || ((!"KEEP_SCREEN_ON_FLAG".equals(this.tag)) && (!"KeyInputQueue".equals(this.tag))))
                    if ((paramIBinder & 0x3F) == 1)
                    {
                        k = 0;
                        label100: this.monitorType = k;
                    }
                try
                {
                    while (true)
                    {
                        paramString.linkToDeath(this, 0);
                        return;
                        j = paramInt2;
                        break;
                        k = 1;
                        break label100;
                        this.monitorType = -1;
                    }
                }
                catch (RemoteException localRemoteException)
                {
                    while (true)
                        binderDied();
                }
            }
        }

        public void binderDied()
        {
            synchronized (PowerManagerService.this.mLocks)
            {
                PowerManagerService.this.releaseWakeLockLocked(this.binder, 0, true);
                return;
            }
        }
    }

    private class SettingsObserver
        implements Observer
    {
        private SettingsObserver()
        {
        }

        private float getFloat(String paramString, float paramFloat)
        {
            ContentValues localContentValues = PowerManagerService.this.mSettings.getValues(paramString);
            if (localContentValues != null);
            for (Float localFloat = localContentValues.getAsFloat("value"); ; localFloat = null)
            {
                if (localFloat != null)
                    paramFloat = localFloat.floatValue();
                return paramFloat;
            }
        }

        private int getInt(String paramString, int paramInt)
        {
            ContentValues localContentValues = PowerManagerService.this.mSettings.getValues(paramString);
            if (localContentValues != null);
            for (Integer localInteger = localContentValues.getAsInteger("value"); ; localInteger = null)
            {
                if (localInteger != null)
                    paramInt = localInteger.intValue();
                return paramInt;
            }
        }

        public void update(Observable paramObservable, Object paramObject)
        {
            while (true)
            {
                float f;
                synchronized (PowerManagerService.this.mLocks)
                {
                    PowerManagerService.access$1302(PowerManagerService.this, getInt("stay_on_while_plugged_in", 1));
                    PowerManagerService.this.updateWakeLockLocked();
                    PowerManagerService.access$1402(PowerManagerService.this, getInt("screen_off_timeout", 15000));
                    PowerManagerService.access$1502(PowerManagerService.this, getInt("screen_brightness", 192));
                    PowerManagerService.access$1602(PowerManagerService.this, 0.0F);
                    PowerManagerService.this.setScreenBrightnessMode(getInt("screen_brightness_mode", 0));
                    PowerManagerService.this.setScreenOffTimeoutsLocked();
                    PowerManagerService.access$1902(PowerManagerService.this, getFloat("window_animation_scale", 1.0F));
                    f = getFloat("transition_animation_scale", 1.0F);
                    PowerManagerService.access$2002(PowerManagerService.this, 0);
                    if (PowerManagerService.this.mWindowScaleAnimation > 0.5F)
                    {
                        PowerManagerService.access$2076(PowerManagerService.this, 16);
                        break label165;
                        return;
                    }
                }
                label165: if (f <= 0.5F);
            }
        }
    }

    private final class DockReceiver extends BroadcastReceiver
    {
        private DockReceiver()
        {
        }

        public void onReceive(Context paramContext, Intent paramIntent)
        {
            int i = paramIntent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
            PowerManagerService.this.dockStateChanged(i);
        }
    }

    private final class BootCompletedReceiver extends BroadcastReceiver
    {
        private BootCompletedReceiver()
        {
        }

        public void onReceive(Context paramContext, Intent paramIntent)
        {
            PowerManagerService.this.bootCompleted();
        }
    }

    private final class BatteryReceiver extends BroadcastReceiver
    {
        private BatteryReceiver()
        {
        }

        public void onReceive(Context paramContext, Intent paramIntent)
        {
            synchronized (PowerManagerService.this.mLocks)
            {
                boolean bool = PowerManagerService.this.mIsPowered;
                PowerManagerService.access$602(PowerManagerService.this, PowerManagerService.this.mBatteryService.isPowered());
                PowerManagerService.LockList localLockList2;
                if (PowerManagerService.this.mIsPowered != bool)
                {
                    PowerManagerService.this.updateWakeLockLocked();
                    localLockList2 = PowerManagerService.this.mLocks;
                    if (!bool);
                }
                try
                {
                    if (((0x1 & PowerManagerService.this.mPowerState) != 0) || (PowerManagerService.this.mUnplugTurnsOnScreen))
                        PowerManagerService.this.forceUserActivityLocked();
                    return;
                }
                finally
                {
                }
            }
        }
    }

    private class UnsynchronizedWakeLock
    {
        int mCount = 0;
        int mFlags;
        boolean mHeld;
        boolean mRefCounted;
        String mTag;
        IBinder mToken;

        UnsynchronizedWakeLock(int paramString, String paramBoolean, boolean arg4)
        {
            this.mFlags = paramString;
            this.mTag = paramBoolean;
            this.mToken = new Binder();
            boolean bool;
            this.mRefCounted = bool;
        }

        public void acquire()
        {
            long l;
            if (this.mRefCounted)
            {
                int i = this.mCount;
                this.mCount = (i + 1);
                if (i != 0);
            }
            else
            {
                l = Binder.clearCallingIdentity();
            }
            try
            {
                PowerManagerService.this.acquireWakeLockLocked(this.mFlags, this.mToken, PowerManagerService.this.MY_UID, PowerManagerService.this.MY_PID, this.mTag, null);
                this.mHeld = true;
                return;
            }
            finally
            {
                Binder.restoreCallingIdentity(l);
            }
        }

        public boolean isHeld()
        {
            return this.mHeld;
        }

        public void release()
        {
            if (this.mRefCounted)
            {
                int i = -1 + this.mCount;
                this.mCount = i;
                if (i != 0);
            }
            else
            {
                PowerManagerService.this.releaseWakeLockLocked(this.mToken, 0, false);
                this.mHeld = false;
            }
            if (this.mCount < 0)
                throw new RuntimeException("WakeLock under-locked " + this.mTag);
        }

        public String toString()
        {
            return "UnsynchronizedWakeLock(mFlags=0x" + Integer.toHexString(this.mFlags) + " mCount=" + this.mCount + " mHeld=" + this.mHeld + ")";
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_CLASS)
    static class Injector
    {
        static boolean FALSE = false;

        static void animateTo(PowerManagerService paramPowerManagerService, PowerManagerService.ScreenBrightnessAnimator paramScreenBrightnessAnimator, int paramInt1, int paramInt2, int paramInt3)
        {
            try
            {
                paramScreenBrightnessAnimator.endValue = paramInt1;
                paramScreenBrightnessAnimator.currentValue = paramInt1;
                paramPowerManagerService.getScreenBrightnessHandler().removeCallbacksAndMessages(null);
                return;
            }
            finally
            {
            }
        }

        static void sleepIfProximitySensorActive(PowerManagerService paramPowerManagerService)
        {
            if ((paramPowerManagerService.getProximitySensorActive()) && ((0x1 & paramPowerManagerService.getPowerState()) != 0))
            {
                paramPowerManagerService.callGoToSleepLocked(SystemClock.uptimeMillis(), 4);
                paramPowerManagerService.setProxIgnoredBecauseScreenTurnedOff(false);
            }
        }
    }
}

/* Location:                     /home/lithium/miui/chameleon/2.11.16/services_dex2jar.jar
 * Qualified Name:         com.android.server.PowerManagerService
 * JD-Core Version:        0.6.2
 */
