package com.WazaBe.HoloEverywhere.app;

import java.util.Calendar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import org.bitbucket.fredgrott.gwslaf.R;
import com.WazaBe.HoloEverywhere.widget.DatePicker;
import com.WazaBe.HoloEverywhere.widget.DatePicker.OnDateChangedListener;

// TODO: Auto-generated Javadoc
/**
 * The Class DatePickerDialog.
 */
public class DatePickerDialog extends AlertDialog implements OnClickListener,
		OnDateChangedListener {
	
	/**
	 * The listener interface for receiving onDateSet events.
	 * The class that is interested in processing a onDateSet
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnDateSetListener<code> method. When
	 * the onDateSet event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see OnDateSetEvent
	 */
	public interface OnDateSetListener {
		
		/**
		 * On date set.
		 *
		 * @param view the view
		 * @param year the year
		 * @param monthOfYear the month of year
		 * @param dayOfMonth the day of month
		 */
		void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth);
	}

	/** The Constant DAY. */
	private static final String DAY = "day";
	
	/** The Constant MONTH. */
	private static final String MONTH = "month";
	
	/** The Constant YEAR. */
	private static final String YEAR = "year";
	
	/** The m calendar. */
	private final Calendar mCalendar;
	
	/** The m call back. */
	private final OnDateSetListener mCallBack;
	
	/** The m date picker. */
	private final DatePicker mDatePicker;

	/** The m title needs update. */
	private boolean mTitleNeedsUpdate = true;

	/**
	 * Instantiates a new date picker dialog.
	 *
	 * @param context the context
	 * @param theme the theme
	 * @param callBack the call back
	 * @param year the year
	 * @param monthOfYear the month of year
	 * @param dayOfMonth the day of month
	 */
	public DatePickerDialog(Context context, int theme,
			OnDateSetListener callBack, int year, int monthOfYear,
			int dayOfMonth) {
		super(context, theme);
		mCallBack = callBack;
		mCalendar = Calendar.getInstance();
		setButton(BUTTON_POSITIVE, getContext()
				.getText(R.string.date_time_done), this);
		setIcon(0);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.date_picker_dialog, null);
		setView(view);
		mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
		mDatePicker.init(year, monthOfYear, dayOfMonth, this);
		updateTitle(year, monthOfYear, dayOfMonth);
	}

	/**
	 * Instantiates a new date picker dialog.
	 *
	 * @param context the context
	 * @param callBack the call back
	 * @param year the year
	 * @param monthOfYear the month of year
	 * @param dayOfMonth the day of month
	 */
	public DatePickerDialog(Context context, OnDateSetListener callBack,
			int year, int monthOfYear, int dayOfMonth) {
		this(context, 0, callBack, year, monthOfYear, dayOfMonth);
	}

	/**
	 * Gets the date picker.
	 *
	 * @return the date picker
	 */
	public DatePicker getDatePicker() {
		return mDatePicker;
	}

	/**
	 * On click.
	 *
	 * @param dialog the dialog
	 * @param which the which
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		tryNotifyDateSet();
	}

	/**
	 * On date changed.
	 *
	 * @param view the view
	 * @param year the year
	 * @param month the month
	 * @param day the day
	 * @see com.WazaBe.HoloEverywhere.widget.DatePicker.OnDateChangedListener#onDateChanged(com.WazaBe.HoloEverywhere.widget.DatePicker, int, int, int)
	 */
	@Override
	public void onDateChanged(DatePicker view, int year, int month, int day) {
		mDatePicker.init(year, month, day, this);
		updateTitle(year, month, day);
	}

	/**
	 * On restore instance state.
	 *
	 * @param savedInstanceState the saved instance state
	 * @see android.app.Dialog#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int year = savedInstanceState.getInt(YEAR);
		int month = savedInstanceState.getInt(MONTH);
		int day = savedInstanceState.getInt(DAY);
		mDatePicker.init(year, month, day, this);
	}

	/**
	 * On save instance state.
	 *
	 * @return the bundle
	 * @see android.app.Dialog#onSaveInstanceState()
	 */
	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt(YEAR, mDatePicker.getYear());
		state.putInt(MONTH, mDatePicker.getMonth());
		state.putInt(DAY, mDatePicker.getDayOfMonth());
		return state;
	}

	/**
	 * On stop.
	 *
	 * @see android.app.Dialog#onStop()
	 */
	@Override
	protected void onStop() {
		tryNotifyDateSet();
		super.onStop();
	}

	/**
	 * Try notify date set.
	 */
	private void tryNotifyDateSet() {
		if (mCallBack != null) {
			mDatePicker.clearFocus();
			mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
					mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
		}
	}

	/**
	 * Update date.
	 *
	 * @param year the year
	 * @param monthOfYear the month of year
	 * @param dayOfMonth the day of month
	 */
	public void updateDate(int year, int monthOfYear, int dayOfMonth) {
		mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
	}

	/**
	 * Update title.
	 *
	 * @param year the year
	 * @param month the month
	 * @param day the day
	 */
	private void updateTitle(int year, int month, int day) {
		if (!mDatePicker.getCalendarViewShown()) {
			mCalendar.set(Calendar.YEAR, year);
			mCalendar.set(Calendar.MONTH, month);
			mCalendar.set(Calendar.DAY_OF_MONTH, day);
			String title = DateUtils.formatDateTime(getContext(),
					mCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_SHOW_YEAR
							| DateUtils.FORMAT_ABBREV_MONTH
							| DateUtils.FORMAT_ABBREV_WEEKDAY);
			setTitle(title);
			mTitleNeedsUpdate = true;
		} else {
			if (mTitleNeedsUpdate) {
				mTitleNeedsUpdate = false;
				setTitle(R.string.date_picker_dialog_title);
			}
		}
	}
}
