package com.WazaBe.HoloEverywhere.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import org.bitbucket.fredgrott.gwslaf.R;
import com.WazaBe.HoloEverywhere.internal.NumberPickerEditText;
import com.WazaBe.HoloEverywhere.util.Arrays;
import com.WazaBe.HoloEverywhere.widget.CalendarView.OnDateChangeListener;
import com.WazaBe.HoloEverywhere.widget.NumberPicker.OnValueChangeListener;

// TODO: Auto-generated Javadoc
/**
 * The Class DatePicker.
 */
public class DatePicker extends FrameLayout implements OnValueChangeListener,
		OnDateChangeListener {
	
	/**
	 * The listener interface for receiving onDateChanged events.
	 * The class that is interested in processing a onDateChanged
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnDateChangedListener<code> method. When
	 * the onDateChanged event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see OnDateChangedEvent
	 */
	public interface OnDateChangedListener {
		
		/**
		 * On date changed.
		 *
		 * @param view the view
		 * @param year the year
		 * @param monthOfYear the month of year
		 * @param dayOfMonth the day of month
		 */
		void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth);
	}

	/**
	 * The Class SavedState.
	 */
	private static class SavedState extends BaseSavedState {
		
		/** The Constant CREATOR. */
		@SuppressWarnings("all")
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		/** The m day. */
		private final int mYear, mMonth, mDay;

		/**
		 * Instantiates a new saved state.
		 *
		 * @param in the in
		 */
		private SavedState(Parcel in) {
			super(in);
			mYear = in.readInt();
			mMonth = in.readInt();
			mDay = in.readInt();
		}

		/**
		 * Instantiates a new saved state.
		 *
		 * @param superState the super state
		 * @param year the year
		 * @param month the month
		 * @param day the day
		 */
		private SavedState(Parcelable superState, int year, int month, int day) {
			super(superState);
			mYear = year;
			mMonth = month;
			mDay = day;
		}

		/**
		 * Write to parcel.
		 *
		 * @param dest the dest
		 * @param flags the flags
		 * @see android.view.AbsSavedState#writeToParcel(android.os.Parcel, int)
		 */
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(mYear);
			dest.writeInt(mMonth);
			dest.writeInt(mDay);
		}
	}

	/** The Constant DATE_FORMAT. */
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	
	/** The Constant LOG_TAG. */
	private static final String LOG_TAG = DatePicker.class.getSimpleName();
	
	/** The m calendar view. */
	private final CalendarView mCalendarView;
	
	/** The m context. */
	private Context mContext;
	
	/** The m current locale. */
	private Locale mCurrentLocale;
	
	/** The m date format. */
	private final java.text.DateFormat mDateFormat = new SimpleDateFormat(
			DATE_FORMAT);
	
	/** The m year spinner. */
	private final NumberPicker mDaySpinner, mMonthSpinner, mYearSpinner;
	
	/** The m year spinner input. */
	private final NumberPickerEditText mDaySpinnerInput, mMonthSpinnerInput,
			mYearSpinnerInput;
	
	/** The m is enabled. */
	private boolean mIsEnabled = true;
	
	/** The m number of months. */
	private int mNumberOfMonths;
	
	/** The m on date changed listener. */
	private OnDateChangedListener mOnDateChangedListener;
	
	/** The m short months. */
	private String[] mShortMonths;

	/** The m spinners. */
	private final LinearLayout mSpinners;

	/** The m current date. */
	private Calendar mTempDate, mMinDate, mMaxDate, mCurrentDate;

	/**
	 * Instantiates a new date picker.
	 *
	 * @param context the context
	 */
	public DatePicker(Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a new date picker.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public DatePicker(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.datePickerStyle);
	}

	/**
	 * Instantiates a new date picker.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @param defStyle the def style
	 */
	public DatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setCurrentLocale(Locale.getDefault());
		TypedArray attributesArray = context.obtainStyledAttributes(attrs,
				R.styleable.DatePicker, defStyle, 0);
		boolean spinnersShown = attributesArray.getBoolean(
				R.styleable.DatePicker_spinnersShown, true);
		boolean calendarViewShown = attributesArray.getBoolean(
				R.styleable.DatePicker_calendarViewShown, true);
		int startYear = attributesArray.getInt(
				R.styleable.DatePicker_startYear, 1900);
		int endYear = attributesArray.getInt(R.styleable.DatePicker_endYear,
				2100);
		String minDate = attributesArray
				.getString(R.styleable.DatePicker_minDate);
		String maxDate = attributesArray
				.getString(R.styleable.DatePicker_maxDate);
		int layoutResourceId = attributesArray.getResourceId(
				R.styleable.DatePicker_internalLayout,
				R.layout.date_picker_holo);
		attributesArray.recycle();
		LayoutInflater.inflate(context, layoutResourceId, this, true);
		mSpinners = (LinearLayout) findViewById(R.id.pickers);
		mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
		mCalendarView.setOnDateChangeListener(this);
		mDaySpinner = (NumberPicker) findViewById(R.id.day);
		mDaySpinner.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		mDaySpinner.setOnLongPressUpdateInterval(100);
		mDaySpinner.setOnValueChangedListener(this);
		mDaySpinnerInput = mDaySpinner.getInputField();
		mMonthSpinner = (NumberPicker) findViewById(R.id.month);
		mMonthSpinner.setMinValue(0);
		mMonthSpinner.setMaxValue(mNumberOfMonths - 1);
		mMonthSpinner.setDisplayedValues(mShortMonths);
		mMonthSpinner.setOnLongPressUpdateInterval(200);
		mMonthSpinner.setOnValueChangedListener(this);
		mMonthSpinnerInput = mMonthSpinner.getInputField();
		mYearSpinner = (NumberPicker) findViewById(R.id.year);
		mYearSpinner.setOnLongPressUpdateInterval(100);
		mYearSpinner.setOnValueChangedListener(this);
		mYearSpinnerInput = mYearSpinner.getInputField();
		if (!spinnersShown && !calendarViewShown) {
			setSpinnersShown(true);
		} else {
			setSpinnersShown(spinnersShown);
			setCalendarViewShown(calendarViewShown);
		}
		mTempDate.clear();
		if (!TextUtils.isEmpty(minDate)) {
			if (!parseDate(minDate, mTempDate)) {
				mTempDate.set(startYear, 0, 1);
			}
		} else {
			mTempDate.set(startYear, 0, 1);
		}
		setMinDate(mTempDate.getTimeInMillis());
		mTempDate.clear();
		if (!TextUtils.isEmpty(maxDate)) {
			if (!parseDate(maxDate, mTempDate)) {
				mTempDate.set(endYear, 11, 31);
			}
		} else {
			mTempDate.set(endYear, 11, 31);
		}
		setMaxDate(mTempDate.getTimeInMillis());
		mCurrentDate.setTimeInMillis(System.currentTimeMillis());
		init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
				mCurrentDate.get(Calendar.DAY_OF_MONTH), null);
		reorderSpinners();
		AccessibilityManager am = (AccessibilityManager) mContext
				.getSystemService(Context.ACCESSIBILITY_SERVICE);
		if (am.isEnabled()) {
			setContentDescriptions();
		}
	}

	/**
	 * Dispatch populate accessibility event.
	 *
	 * @param event the event
	 * @return true, if successful
	 * @see android.view.View#dispatchPopulateAccessibilityEvent(android.view.accessibility.AccessibilityEvent)
	 */
	@SuppressLint("NewApi")
	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		if (VERSION.SDK_INT >= 14) {
			onPopulateAccessibilityEvent(event);
			return true;
		} else {
			return super.dispatchPopulateAccessibilityEvent(event);
		}
	}

	/**
	 * Dispatch restore instance state.
	 *
	 * @param container the container
	 * @see android.view.ViewGroup#dispatchRestoreInstanceState(android.util.SparseArray)
	 */
	@Override
	protected void dispatchRestoreInstanceState(
			SparseArray<Parcelable> container) {
		dispatchThawSelfOnly(container);
	}

	/**
	 * Gets the calendar for locale.
	 *
	 * @param oldCalendar the old calendar
	 * @param locale the locale
	 * @return the calendar for locale
	 */
	private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
		if (oldCalendar == null) {
			return Calendar.getInstance(locale);
		} else {
			final long currentTimeMillis = oldCalendar.getTimeInMillis();
			Calendar newCalendar = Calendar.getInstance(locale);
			newCalendar.setTimeInMillis(currentTimeMillis);
			return newCalendar;
		}
	}

	/**
	 * Gets the calendar view.
	 *
	 * @return the calendar view
	 */
	public CalendarView getCalendarView() {
		return mCalendarView;
	}

	/**
	 * Gets the calendar view shown.
	 *
	 * @return the calendar view shown
	 */
	public boolean getCalendarViewShown() {
		return mCalendarView.isShown();
	}

	/**
	 * Gets the day of month.
	 *
	 * @return the day of month
	 */
	public int getDayOfMonth() {
		return mCurrentDate.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Gets the max date.
	 *
	 * @return the max date
	 */
	public long getMaxDate() {
		return mCalendarView.getMaxDate();
	}

	/**
	 * Gets the min date.
	 *
	 * @return the min date
	 */
	public long getMinDate() {
		return mCalendarView.getMinDate();
	}

	/**
	 * Gets the month.
	 *
	 * @return the month
	 */
	public int getMonth() {
		return mCurrentDate.get(Calendar.MONTH);
	}

	/**
	 * Gets the spinners shown.
	 *
	 * @return the spinners shown
	 */
	public boolean getSpinnersShown() {
		return mSpinners.isShown();
	}

	/**
	 * Gets the year.
	 *
	 * @return the year
	 */
	public int getYear() {
		return mCurrentDate.get(Calendar.YEAR);
	}

	/**
	 * Inits the.
	 *
	 * @param year the year
	 * @param monthOfYear the month of year
	 * @param dayOfMonth the day of month
	 * @param onDateChangedListener the on date changed listener
	 */
	public void init(int year, int monthOfYear, int dayOfMonth,
			OnDateChangedListener onDateChangedListener) {
		setDate(year, monthOfYear, dayOfMonth);
		updateSpinners();
		updateCalendarView();
		mOnDateChangedListener = onDateChangedListener;
	}

	/**
	 * Checks if is enabled.
	 *
	 * @return true, if is enabled
	 * @see android.view.View#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return mIsEnabled;
	}

	/**
	 * Checks if is new date.
	 *
	 * @param year the year
	 * @param month the month
	 * @param dayOfMonth the day of month
	 * @return true, if is new date
	 */
	private boolean isNewDate(int year, int month, int dayOfMonth) {
		return mCurrentDate.get(Calendar.YEAR) != year
				|| mCurrentDate.get(Calendar.MONTH) != dayOfMonth
				|| mCurrentDate.get(Calendar.DAY_OF_MONTH) != month;
	}

	/**
	 * Notify date changed.
	 */
	private void notifyDateChanged() {
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
		if (mOnDateChangedListener != null) {
			mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(),
					getDayOfMonth());
		}
	}

	/**
	 * On configuration changed.
	 *
	 * @param newConfig the new config
	 * @see android.view.View#onConfigurationChanged(android.content.res.Configuration)
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setCurrentLocale(newConfig.locale);
	}

	/**
	 * On populate accessibility event.
	 *
	 * @param event the event
	 * @see android.view.View#onPopulateAccessibilityEvent(android.view.accessibility.AccessibilityEvent)
	 */
	@Override
	@SuppressLint("NewApi")
	public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
		super.onPopulateAccessibilityEvent(event);
		final int flags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_YEAR;
		String selectedDateUtterance = DateUtils.formatDateTime(mContext,
				mCurrentDate.getTimeInMillis(), flags);
		event.getText().add(selectedDateUtterance);
	}

	/**
	 * On restore instance state.
	 *
	 * @param state the state
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setDate(ss.mYear, ss.mMonth, ss.mDay);
		updateSpinners();
		updateCalendarView();
	}

	/**
	 * On save instance state.
	 *
	 * @return the parcelable
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, getYear(), getMonth(),
				getDayOfMonth());
	}

	/**
	 * On selected day change.
	 *
	 * @param view the view
	 * @param year the year
	 * @param month the month
	 * @param monthDay the month day
	 * @see com.WazaBe.HoloEverywhere.widget.CalendarView.OnDateChangeListener#onSelectedDayChange(com.WazaBe.HoloEverywhere.widget.CalendarView, int, int, int)
	 */
	@Override
	public void onSelectedDayChange(CalendarView view, int year, int month,
			int monthDay) {
		setDate(year, month, monthDay);
		updateSpinners();
		notifyDateChanged();
	}

	/**
	 * On value change.
	 *
	 * @param picker the picker
	 * @param oldVal the old val
	 * @param newVal the new val
	 * @see com.WazaBe.HoloEverywhere.widget.NumberPicker.OnValueChangeListener#onValueChange(com.WazaBe.HoloEverywhere.widget.NumberPicker, int, int)
	 */
	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		updateInputState();
		mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
		if (picker == mDaySpinner) {
			int maxDayOfMonth = mTempDate
					.getActualMaximum(Calendar.DAY_OF_MONTH);
			if (oldVal == maxDayOfMonth && newVal == 1) {
				mTempDate.add(Calendar.DAY_OF_MONTH, 1);
			} else if (oldVal == 1 && newVal == maxDayOfMonth) {
				mTempDate.add(Calendar.DAY_OF_MONTH, -1);
			} else {
				mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
			}
		} else if (picker == mMonthSpinner) {
			if (oldVal == 11 && newVal == 0) {
				mTempDate.add(Calendar.MONTH, 1);
			} else if (oldVal == 0 && newVal == 11) {
				mTempDate.add(Calendar.MONTH, -1);
			} else {
				mTempDate.add(Calendar.MONTH, newVal - oldVal);
			}
		} else if (picker == mYearSpinner) {
			mTempDate.set(Calendar.YEAR, newVal);
		} else {
			throw new IllegalArgumentException();
		}
		setDate(mTempDate.get(Calendar.YEAR), mTempDate.get(Calendar.MONTH),
				mTempDate.get(Calendar.DAY_OF_MONTH));
		updateSpinners();
		updateCalendarView();
		notifyDateChanged();
	}

	/**
	 * Parses the date.
	 *
	 * @param date the date
	 * @param outDate the out date
	 * @return true, if successful
	 */
	private boolean parseDate(String date, Calendar outDate) {
		try {
			outDate.setTime(mDateFormat.parse(date));
			return true;
		} catch (ParseException e) {
			Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
			return false;
		}
	}

	/**
	 * Reorder spinners.
	 */
	private void reorderSpinners() {
		mSpinners.removeAllViews();
		char[] order = DateFormat.getDateFormatOrder(getContext());
		final int spinnerCount = order.length;
		for (int i = 0; i < spinnerCount; i++) {
			switch (order[i]) {
			case DateFormat.DATE:
				mSpinners.addView(mDaySpinner);
				setImeOptions(mDaySpinner, spinnerCount, i);
				break;
			case DateFormat.MONTH:
				mSpinners.addView(mMonthSpinner);
				setImeOptions(mMonthSpinner, spinnerCount, i);
				break;
			case DateFormat.YEAR:
				mSpinners.addView(mYearSpinner);
				setImeOptions(mYearSpinner, spinnerCount, i);
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Sets the calendar view shown.
	 *
	 * @param shown the new calendar view shown
	 */
	public void setCalendarViewShown(boolean shown) {
		mCalendarView.setVisibility(shown ? VISIBLE : GONE);
	}

	/**
	 * Sets the content descriptions.
	 */
	private void setContentDescriptions() {
		String text;
		text = mContext.getString(R.string.date_picker_increment_day_button);
		mDaySpinner.findViewById(R.id.increment).setContentDescription(text);
		text = mContext.getString(R.string.date_picker_decrement_day_button);
		mDaySpinner.findViewById(R.id.decrement).setContentDescription(text);
		text = mContext.getString(R.string.date_picker_increment_month_button);
		mMonthSpinner.findViewById(R.id.increment).setContentDescription(text);
		text = mContext.getString(R.string.date_picker_decrement_month_button);
		mMonthSpinner.findViewById(R.id.decrement).setContentDescription(text);
		text = mContext.getString(R.string.date_picker_increment_year_button);
		mYearSpinner.findViewById(R.id.increment).setContentDescription(text);
		text = mContext.getString(R.string.date_picker_decrement_year_button);
		mYearSpinner.findViewById(R.id.decrement).setContentDescription(text);
	}

	/**
	 * Sets the current locale.
	 *
	 * @param locale the new current locale
	 */
	private void setCurrentLocale(Locale locale) {
		if (locale.equals(mCurrentLocale)) {
			return;
		}
		mCurrentLocale = locale;
		mTempDate = getCalendarForLocale(mTempDate, locale);
		mMinDate = getCalendarForLocale(mMinDate, locale);
		mMaxDate = getCalendarForLocale(mMaxDate, locale);
		mCurrentDate = getCalendarForLocale(mCurrentDate, locale);
		mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1;
		mShortMonths = new String[mNumberOfMonths];
		for (int i = 0; i < mNumberOfMonths; i++) {
			mShortMonths[i] = DateUtils.getMonthString(Calendar.JANUARY + i,
					DateUtils.LENGTH_MEDIUM);
		}
	}

	/**
	 * Sets the date.
	 *
	 * @param year the year
	 * @param month the month
	 * @param dayOfMonth the day of month
	 */
	private void setDate(int year, int month, int dayOfMonth) {
		mCurrentDate.set(year, month, dayOfMonth);
		if (mCurrentDate.before(mMinDate)) {
			mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
		} else if (mCurrentDate.after(mMaxDate)) {
			mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
		}
	}

	/**
	 * Sets the enabled.
	 *
	 * @param enabled the new enabled
	 * @see android.view.View#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (mIsEnabled == enabled) {
			return;
		}
		super.setEnabled(enabled);
		mDaySpinner.setEnabled(enabled);
		mMonthSpinner.setEnabled(enabled);
		mYearSpinner.setEnabled(enabled);
		mCalendarView.setEnabled(enabled);
		mIsEnabled = enabled;
	}

	/**
	 * Sets the ime options.
	 *
	 * @param spinner the spinner
	 * @param spinnerCount the spinner count
	 * @param spinnerIndex the spinner index
	 */
	private void setImeOptions(NumberPicker spinner, int spinnerCount,
			int spinnerIndex) {
		final int imeOptions;
		if (spinnerIndex < spinnerCount - 1) {
			imeOptions = EditorInfo.IME_ACTION_NEXT;
		} else {
			imeOptions = EditorInfo.IME_ACTION_DONE;
		}
		NumberPickerEditText input = spinner.getInputField();
		input.setImeOptions(imeOptions);
	}

	/**
	 * Sets the max date.
	 *
	 * @param maxDate the new max date
	 */
	public void setMaxDate(long maxDate) {
		mTempDate.setTimeInMillis(maxDate);
		if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
				&& mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate
						.get(Calendar.DAY_OF_YEAR)) {
			return;
		}
		mMaxDate.setTimeInMillis(maxDate);
		mCalendarView.setMaxDate(maxDate);
		if (mCurrentDate.after(mMaxDate)) {
			mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
			updateCalendarView();
		}
		updateSpinners();
	}

	/**
	 * Sets the min date.
	 *
	 * @param minDate the new min date
	 */
	public void setMinDate(long minDate) {
		mTempDate.setTimeInMillis(minDate);
		if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
				&& mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate
						.get(Calendar.DAY_OF_YEAR)) {
			return;
		}
		mMinDate.setTimeInMillis(minDate);
		mCalendarView.setMinDate(minDate);
		if (mCurrentDate.before(mMinDate)) {
			mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
			updateCalendarView();
		}
		updateSpinners();
	}

	/**
	 * Sets the spinners shown.
	 *
	 * @param shown the new spinners shown
	 */
	public void setSpinnersShown(boolean shown) {
		mSpinners.setVisibility(shown ? VISIBLE : GONE);
	}

	/**
	 * Update calendar view.
	 */
	private void updateCalendarView() {
		mCalendarView.setDate(mCurrentDate.getTimeInMillis(), false, false);
	}

	/**
	 * Update date.
	 *
	 * @param year the year
	 * @param month the month
	 * @param dayOfMonth the day of month
	 */
	public void updateDate(int year, int month, int dayOfMonth) {
		if (!isNewDate(year, month, dayOfMonth)) {
			return;
		}
		setDate(year, month, dayOfMonth);
		updateSpinners();
		updateCalendarView();
		notifyDateChanged();
	}

	/**
	 * Update input state.
	 */
	private void updateInputState() {
		InputMethodManager inputMethodManager = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			if (inputMethodManager.isActive(mYearSpinnerInput)) {
				mYearSpinnerInput.clearFocus();
				inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
			} else if (inputMethodManager.isActive(mMonthSpinnerInput)) {
				mMonthSpinnerInput.clearFocus();
				inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
			} else if (inputMethodManager.isActive(mDaySpinnerInput)) {
				mDaySpinnerInput.clearFocus();
				inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
			}
		}
	}

	/**
	 * Update spinners.
	 */
	private void updateSpinners() {
		if (mCurrentDate.equals(mMinDate)) {
			mDaySpinner.setMinValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
			mDaySpinner.setMaxValue(mCurrentDate
					.getActualMaximum(Calendar.DAY_OF_MONTH));
			mDaySpinner.setWrapSelectorWheel(false);
			mMonthSpinner.setMinValue(mCurrentDate.get(Calendar.MONTH));
			mMonthSpinner.setMaxValue(mCurrentDate
					.getActualMaximum(Calendar.MONTH));
			mMonthSpinner.setWrapSelectorWheel(false);
		} else if (mCurrentDate.equals(mMaxDate)) {
			mDaySpinner.setMinValue(mCurrentDate
					.getActualMinimum(Calendar.DAY_OF_MONTH));
			mDaySpinner.setMaxValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
			mDaySpinner.setWrapSelectorWheel(false);
			mMonthSpinner.setMinValue(mCurrentDate
					.getActualMinimum(Calendar.MONTH));
			mMonthSpinner.setMaxValue(mCurrentDate.get(Calendar.MONTH));
			mMonthSpinner.setWrapSelectorWheel(false);
		} else {
			mDaySpinner.setMinValue(1);
			mDaySpinner.setMaxValue(mCurrentDate
					.getActualMaximum(Calendar.DAY_OF_MONTH));
			mDaySpinner.setWrapSelectorWheel(true);
			mMonthSpinner.setMinValue(0);
			mMonthSpinner.setMaxValue(11);
			mMonthSpinner.setWrapSelectorWheel(true);
		}
		String[] displayedValues = Arrays.copyOfRange(mShortMonths,
				mMonthSpinner.getMinValue(), mMonthSpinner.getMaxValue() + 1);
		mMonthSpinner.setDisplayedValues(displayedValues);
		mYearSpinner.setMinValue(mMinDate.get(Calendar.YEAR));
		mYearSpinner.setMaxValue(mMaxDate.get(Calendar.YEAR));
		mYearSpinner.setWrapSelectorWheel(false);
		mYearSpinner.setValue(mCurrentDate.get(Calendar.YEAR));
		mMonthSpinner.setValue(mCurrentDate.get(Calendar.MONTH));
		mDaySpinner.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
	}
}
