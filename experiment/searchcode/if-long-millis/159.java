package de.codecentric.android.timer.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import de.codecentric.android.timer.R;
import de.codecentric.android.timer.activity.ShowAlarmActivity;
import de.codecentric.android.timer.persistence.Timer;
import de.codecentric.android.timer.util.PreferencesKeysValues;
import de.codecentric.android.timer.util.TimeParts;

/**
 * This service encapsulates the countdown timer, it is responsible for keeping
 * all state information before, during and after the countdown. This state
 * information includes if a countdown is active or paused and the remaining
 * time for the countdown.
 * 
 * Any instance can only be used for one countdown at a time, not for multiple
 * concurrent countdowns. However, once it has finished counting down or has
 * been stopped by the user, it can be resetted to its initial state and is then
 * ready to be started again.
 * 
 * @author Bastian Krol
 */
public class CountdownService extends Service {

	static final String TAG = CountdownService.class.getName();

	static final int COUNTDOWN_TICK_INTERVALL = 300;
	static final int DELAY_TIME = COUNTDOWN_TICK_INTERVALL / 2;
	public static final int GUI_UPDATE_INTERVALL = COUNTDOWN_TICK_INTERVALL / 4;

	static final int ALARM_NOTIFICATION_ID = 1;

	private static final BigDecimal THOUSAND = new BigDecimal(1000);

	private PreferencesKeysValues preferencesKeysValues;

	private IBinder countdownServiceBinder;
	private CountDownTimer countdownTimer;
	private CountDownTimer maxAlarmDurationTimer;
	private ServiceState serviceState;

	private BigDecimal initialSecondsRoundedUp;
	private long remainingMilliseconds;

	private NotificationManager notificationManager;

	private SoundGizmo soundGizmo;
	private boolean soundIsPlaying;

	public CountdownService() {
		super();
		Log.d(TAG, "CountdownService instance created");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		this.preferencesKeysValues = new PreferencesKeysValues(this);
		this.serviceState = ServiceState.WAITING;
		this.initialSecondsRoundedUp = null;
		this.remainingMilliseconds = Long.MAX_VALUE;
		this.countdownServiceBinder = new CountdownServiceBinder(this);
		this.getNotificationManager();
		this.soundGizmo = new SoundGizmo();
		this.soundIsPlaying = false;
	}

	private void getNotificationManager() {
		String notificationService = Context.NOTIFICATION_SERVICE;
		this.notificationManager = (NotificationManager) super
				.getSystemService(notificationService);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind(Intent)");
		return this.countdownServiceBinder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		this.stopCountdownTimer();
		super.onDestroy();
	}

	/**
	 * Starts the countdown. State transition from {@link ServiceState#WAITING}
	 * to {@link ServiceState#COUNTING_DOWN}.
	 * 
	 * @param timer
	 *            the timer object to read the the number of milliseconds from
	 */
	public void startCountdown(Timer timer) {
		this.startCountdown(timer.getMillis());
	}

	/**
	 * Starts the countdown. State transition from {@link ServiceState#WAITING}
	 * to {@link ServiceState#COUNTING_DOWN}.
	 * 
	 * @param millis
	 *            the number of milliseconds to count down
	 */
	public void startCountdown(long millis) {
		Log.d(TAG, "startCountdown(" + millis + ")");
		this.checkStatePrecondition(ServiceState.WAITING);
		this.initialSecondsRoundedUp = BigDecimal.valueOf(millis).divide(
				THOUSAND, 0, RoundingMode.UP);
		this.remainingMilliseconds = millis;
		this.startOrContinue(millis);
	}

	/**
	 * Pauses the countdown. State transition from
	 * {@link ServiceState#COUNTING_DOWN} to {@link ServiceState#PAUSED}. The
	 * countdown might later be resumed by calling {@link #continueCountdown()}.
	 */
	public void pauseCountdown() {
		Log.d(TAG, "pauseCountdown()");
		this.checkStatePrecondition(ServiceState.COUNTING_DOWN);
		this.stopCountdownTimer();
		this.serviceState = ServiceState.PAUSED;
	}

	/**
	 * Continues the countdown after it has been stopped by
	 * {@link #pauseCountdown()}. State transition from
	 * {@link ServiceState#PAUSED} to {@link ServiceState#COUNTING_DOWN}. The
	 * countdown might later be resumed by calling {@link #continueCountdown()}.
	 */
	public void continueCountdown() {
		Log.d(TAG, "continueCountdown()");
		this.checkStatePrecondition(ServiceState.PAUSED);
		this.startOrContinue(this.remainingMilliseconds);
	}

	/**
	 * Stops the alarm sound. State transition from {@link ServiceState#BEEPING}
	 * to {@link ServiceState#FINISHED}. The countdown might later be started
	 * again by calling Clients can start a new countdown by calling
	 * {@link #resetToWaiting()} followed by {@link #startCountdown(long)}.
	 */
	public void stopAlarmSound() {
		Log.d(TAG, "notifyAlarmSoundHasStopped()");

		// The timer might just have been stopped automatically while the user
		// pressed Stop Alarm more or less in the same moment.
		if (this.serviceState == ServiceState.FINISHED_AUTOMATICALLY) {
			return;
		}
		this.stopAlarmInternally();
		this.serviceState = ServiceState.FINISHED;
	}

	/**
	 * Stops/aborts the countdown completely. State transition from any state to
	 * {@link ServiceState#FINISHED}. The countdown might later be started again
	 * by calling Clients can start a new countdown by calling
	 * {@link #resetToWaiting()} followed by {@link #startCountdown(long)}.
	 */
	public void stopCountdown() {
		Log.d(TAG, "stopCountdown()");
		// stopCountdown can be called regardless of the current state, so there
		// is no checkStatePrecondition here.
		this.stopCountdownTimer();
		this.cancelAlarmSound();
		this.serviceState = ServiceState.FINISHED;
	}

	/**
	 * Prepares the service to for a new countdown, after the last countdown has
	 * been stopped. State transition from {@link ServiceState#FINISHED} or
	 * {@link ServiceState#FINISHED_AUTOMATICALLY} to
	 * {@link ServiceState#WAITING}.
	 */
	public void resetToWaiting() {
		Log.d(TAG, "resetToWaiting()");
		this.checkStatePrecondition(ServiceState.FINISHED,
				ServiceState.FINISHED_AUTOMATICALLY);
		this.serviceState = ServiceState.WAITING;
		this.initialSecondsRoundedUp = null;
		this.remainingMilliseconds = Long.MAX_VALUE;
	}

	/**
	 * Signals that the user wants to leave the application. State transition
	 * from any state to {@link ServiceState#EXIT}. Also implicitly stops the
	 * countdown (if counting down) and the alarm sound (if ringing).
	 */
	public void exit() {
		Log.d(TAG, "exit()");
		this.stopCountdown();
		this.serviceState = ServiceState.EXIT;
	}

	/**
	 * Stops this service by calling {@link Service#stopSelf()}.
	 */
	public void stopService() {
		Log.d(TAG, "stopService()");
		super.stopSelf();
	}

	private void checkStatePrecondition(ServiceState... validStates) {
		for (ServiceState serviceState : validStates) {
			if (this.serviceState == serviceState) {
				return;
			}
		}
		throw new IllegalStateException("Actual state: " + this.serviceState
				+ ", expected on of: " + validStates);
	}

	/**
	 * @return {@code true} if and only if the current state is
	 *         {@link ServiceState#WAITING}
	 */
	public boolean isWaiting() {
		return this.serviceState == ServiceState.WAITING;
	}

	/**
	 * @return {@code true} if and only if the current state is
	 *         {@link ServiceState#COUNTING_DOWN}
	 */
	public boolean isCountingDown() {
		return this.serviceState == ServiceState.COUNTING_DOWN;
	}

	/**
	 * @return {@code true} if and only if the current state is
	 *         {@link ServiceState#PAUSED}
	 */
	public boolean isPaused() {
		return this.serviceState == ServiceState.PAUSED;
	}

	/**
	 * @return {@code true} if and only if the current state is
	 *         {@link ServiceState#BEEPING}
	 */
	public boolean isBeeping() {
		return this.serviceState == ServiceState.BEEPING;
	}

	/**
	 * @return {@code true} if and only if the current state is
	 *         {@link ServiceState#FINISHED}
	 */
	public boolean isFinished() {
		return this.serviceState == ServiceState.FINISHED
				|| this.serviceState == ServiceState.FINISHED_AUTOMATICALLY;
	}

	/**
	 * @return {@code true} if and only if the current state is
	 *         {@link ServiceState#EXIT}
	 */
	public boolean isExit() {
		return this.serviceState == ServiceState.EXIT;
	}

	/**
	 * @return the current state
	 */
	public ServiceState getState() {
		return this.serviceState;
	}

	private void startOrContinue(long millis) {
		this.serviceState = ServiceState.COUNTING_DOWN;
		this.startCountdownTimer(millis);
	}

	/**
	 * Creates and starts the internal countdown timer.
	 * 
	 * @param millis
	 *            the milliseconds for the timer
	 */
	private void startCountdownTimer(final long millis) {
		Log.d(TAG, "startCountdownTimer(" + millis + ")");
		this.countdownTimer = new CountDownTimer(millis,
				COUNTDOWN_TICK_INTERVALL) {
			public void onTick(long millisUntilFinished) {
				CountdownService.this.onCountdownTimerTick(millisUntilFinished);
			}

			public void onFinish() {
				CountdownService.this.onCountdownTimerFinish();
			}
		}.start();
	}

	/**
	 * Stops the internal countdown timer.
	 */
	private void stopCountdownTimer() {
		if (this.countdownTimer != null) {
			this.countdownTimer.cancel();
		}
	}

	private void onCountdownTimerTick(long remainingMilliseconds) {
		Log.v(TAG, "countdown tick - remainingMilliseconds: "
				+ remainingMilliseconds);
		this.remainingMilliseconds = remainingMilliseconds;
	}

	private void onCountdownTimerFinish() {
		Log.d(TAG, "countdown finished");
		this.remainingMilliseconds = 0;
		this.startDelayAlarmTimer();
	}

	private void startDelayAlarmTimer() {
		// Instead of ringing the alarm directly, we wait a few more
		// milliseconds to give ShowCountdownActivity a chance to show 00:00
		// and an empty pie chart.
		new CountDownTimer(DELAY_TIME, DELAY_TIME) {
			public void onTick(long millisUntilFinished) {
				// nothing to do
			}

			public void onFinish() {
				CountdownService.this.startAlarm();
			}
		}.start();
	}

	/**
	 * Kicks off the alarm.
	 */
	private void startAlarm() {
		this.serviceState = ServiceState.BEEPING;
		this.addStatusBarNotification();
		this.ringAlarmSound();
		this.startShowAlarmActivityFromService();
		this.startMaxDurationTimer();

	}

	private void addStatusBarNotification() {
		Log.d(TAG, "addStatusBarNotification()");
		int icon = R.drawable.ic_stat_notify_alarm_ringing;
		CharSequence tickerText = super
				.getString(R.string.notification_alarm_ticker_text);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = super
				.getString(R.string.notification_alarm_title);
		CharSequence contentText = super
				.getString(R.string.notification_alarm_text);
		Intent notificationIntent = new Intent(this, ShowAlarmActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		this.notificationManager.notify(TAG, ALARM_NOTIFICATION_ID,
				notification);
	}

	private void removeStatusBarNotification() {
		Log.d(TAG, "removeStatusBarNotification()");
		this.notificationManager.cancel(TAG, ALARM_NOTIFICATION_ID);
	}

	void startShowAlarmActivityFromService() {
		Log.d(TAG, "startShowAlarmActivity()");
		Intent showAlarmIntent = new Intent(getBaseContext(),
				ShowAlarmActivity.class);
		showAlarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(showAlarmIntent);
	}

	private void ringAlarmSound() {
		Log.d(TAG, "ringAlarm()");
		if (!this.soundIsPlaying) {
			Log.d(TAG, "starting new sound");
			this.soundGizmo.playAlarmSound(this);
		} else {
			Log.e(TAG, "sound should already be playing");
		}
	}

	/**
	 * This makes sure the alarm only rings for the configured duration (if it
	 * isn't cancelled by the user first).
	 */
	private void startMaxDurationTimer() {
		this.cancelMaxAlarmDurationTimer();
		long maxAlarmDurationSeconds = this
				.loadMaximumDurationFromPreferences();
		long maxAlarmDurationMilliseconds = maxAlarmDurationSeconds * 1000;
		this.maxAlarmDurationTimer = new CountDownTimer(
				maxAlarmDurationMilliseconds, maxAlarmDurationMilliseconds) {
			public void onTick(long millisUntilFinished) {
				// nothing to do
			}

			public void onFinish() {
				CountdownService.this.onMaxDurationTimerFinished();
			}
		}.start();
	}

	private void onMaxDurationTimerFinished() {
		Log.d(TAG, "onMaxDurationTimerFinished()");
		this.stopAlarmInternally();
		this.serviceState = ServiceState.FINISHED_AUTOMATICALLY;
		this.stopShowAlarmActivityFromService();
	}

	private void stopAlarmInternally() {
		Log.d(TAG, "stopAlarmInternally()");
		this.checkStatePrecondition(ServiceState.BEEPING);
		this.cancelMaxAlarmDurationTimer();
		this.cancelAlarmSound();
		this.removeStatusBarNotification();
	}

	private void cancelMaxAlarmDurationTimer() {
		Log.d(TAG, "cancelMaxAlarmDurationTimer()");
		if (this.maxAlarmDurationTimer != null) {
			this.maxAlarmDurationTimer.cancel();
			this.maxAlarmDurationTimer = null;
		}
	}

	void stopShowAlarmActivityFromService() {
		Log.d(TAG, "stopShowAlarmActivityFromService()");
		// actually, we gonna *start* an activity (doesn't matter which) and
		// rely on the navigation automatism to go to the right activity. I
		// would not know how to *stop* an activity from a service directly?
		Intent arbitraryActivity = new Intent(getBaseContext(),
				ShowAlarmActivity.class);
		arbitraryActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(arbitraryActivity);
	}

	private void cancelAlarmSound() {
		Log.d(TAG, "stopBeeping()");
		this.soundGizmo.stopAlarm();
		this.soundIsPlaying = false;
	}

	public long getRemainingMilliseconds() {
		return this.remainingMilliseconds;
	}

	/**
	 * Returns the fraction of initial milliseconds (the value used as a
	 * parameter for {@link #startCountdown(long)} divided by remaining
	 * milliseconds (the value returned by {@link #getRemainingMilliseconds()}.
	 * The remaining milliseconds are rounded up to the next full second so that
	 * the value returned by this method only changes approximately once a
	 * second.
	 * 
	 * @return fraction of initial milliseconds divided by remaining
	 *         milliseconds
	 */
	public float getRemainingFractionRoundedUpToFullSeconds() {
		Log.d(TAG, "getRemainingFractionRoundedUpToFullSeconds()");
		if (this.initialSecondsRoundedUp != null
				&& BigDecimal.ZERO.compareTo(this.initialSecondsRoundedUp) < 0) {
			BigDecimal remainingSecondsRoundedUp = new BigDecimal(
					this.remainingMilliseconds).divide(THOUSAND, 0,
					RoundingMode.UP);
			Log.d(TAG, "remainingSecondsRoundedUp: "
					+ remainingSecondsRoundedUp);
			BigDecimal fraction = remainingSecondsRoundedUp.divide(
					this.initialSecondsRoundedUp, 3, RoundingMode.DOWN);
			Log.d(TAG, "fractionRoundedUp: " + fraction);
			return fraction.floatValue();
		} else {
			return 0f;
		}
	}

	private long loadMaximumDurationFromPreferences() {
		Log.d(TAG, "loadPreferences()");
		SharedPreferences preferences = this.getPreferences();
		String valueAsString = preferences.getString(
				this.preferencesKeysValues.keyAlarmDuration,
				String.valueOf(TimeParts.FIVE_MINUTES.getSecondsTotal()));
		try {
			return Long.parseLong(valueAsString);
		} catch (NumberFormatException e) {
			return TimeParts.FIVE_MINUTES.getSecondsTotal();
		}
	}

	SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}
