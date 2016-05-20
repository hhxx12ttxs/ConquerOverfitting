package org.rapidpm.android.component;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import org.rapidpm.android.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: Alexander Vos
 * Date: 20.01.13
 * Time: 11:26
 */
public class DateTimeView extends LinearLayout implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private final TextView mDateTextView;
    private final TextView mTimeTextView;

    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;

    private final Calendar mCalendar;
    private Date mDate;

    public DateTimeView(final Context context, final AttributeSet attr) {
        super(context, attr);

        setOrientation(HORIZONTAL);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.date_time_view, this);

        mDateTextView = (TextView) findViewById(R.id.dateTextView);
        mTimeTextView = (TextView) findViewById(R.id.timeTextView);

//        mDateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        mDateFormat = DateFormat.getDateInstance(DateFormat.FULL);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);

        mCalendar = Calendar.getInstance();

        mDateTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                final DatePickerDialog datePickerDialog = new DatePickerDialog(context, DateTimeView.this, year, month, day);
                datePickerDialog.show();
            }
        });

        mTimeTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = mCalendar.get(Calendar.MINUTE);
                final TimePickerDialog timePickerDialog = new TimePickerDialog(context, DateTimeView.this, hour, minute,
                        android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
    }


    public void setDateTime(final Date dateTime) {
        mDate = dateTime;
        if (dateTime != null) {
            mCalendar.setTime(dateTime);
            mDateTextView.setText(mDateFormat.format(dateTime));
            mTimeTextView.setText(mTimeFormat.format(dateTime));
        } else {
            mCalendar.setTime(new Date());
            mDateTextView.setText("");
            mTimeTextView.setText("");
        }
    }

    public Date getDateTime() {
        return mDate;
    }

    public void setDate(final int year, final int monthOfYear, final int dayOfMonth) {
        mCalendar.set(year, monthOfYear, dayOfMonth);
        mDate = mCalendar.getTime();
        mDateTextView.setText(mDateFormat.format(mDate));
    }

    public void setDate(final Date date) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        setDate(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
    }

    public void setTime(final int hourOfDay, final int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mDate = mCalendar.getTime();
        mTimeTextView.setText(mTimeFormat.format(mDate));
    }

    public void setTime(final Date time) {
        final Calendar c = Calendar.getInstance();
        c.setTime(time);
        setTime(c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE));
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
        setDate(year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
        setTime(hourOfDay, minute);
    }
}

