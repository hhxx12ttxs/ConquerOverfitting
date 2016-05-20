package info.chrzanowski.inz.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import info.chrzanowski.inz.R;

/**
 * Element UI umożliwiający wybranie dwóch liczb - np. w kontekście godzina:minuta
 */
public class TimeDialogPreference extends DialogPreference {
    private static final String TAG = TimeDialogPreference.class.getCanonicalName();

    private int lastHour;
    private int lastMinute;
    private int step = 10;
    private int maxHour = 23;
    private int minHour = 0;
    private int maxMinute = 50;
    private int minMinute = 0;
    private final String separator = ":";

    private TimePicker timePicker = null;

    public TimeDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(attrs);
    }

    public TimeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        setPersistent(true);

        TypedArray styledAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.TimeDialogPreference);
        step = styledAttrs.getInteger(R.styleable.TimeDialogPreference_step, step);
        maxHour = styledAttrs.getInteger(R.styleable.TimeDialogPreference_maxHour, maxHour);
        minHour = styledAttrs.getInteger(R.styleable.TimeDialogPreference_minHour, minHour);
        maxMinute = styledAttrs.getInteger(R.styleable.TimeDialogPreference_maxMinute, maxMinute);
        minMinute = styledAttrs.getInteger(R.styleable.TimeDialogPreference_minMinute, minMinute);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        int seconds = getSharedPreferences().getInt(getKey(), 0);
        setValue(seconds);
    }

    public void setValue(int seconds) {
        if ( seconds < step ) {
            lastHour = minHour;
            lastMinute = minMinute;
        } else {
            lastHour = seconds / 60;
            lastMinute = seconds % 60;
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int seconds = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
            getSharedPreferences().edit().putInt(getKey(), seconds).commit();
        }

        timePicker = null;
    }

    @Override
    protected View onCreateDialogView() {
        timePicker = new TimePicker(getContext());
        timePicker.setIs24HourView(true);

        timePicker.setCurrentHour( lastHour );
        timePicker.setCurrentMinute( lastMinute );

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener(){
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                /**
                 * Hack to prevent StackOverflowError
                 */
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                    }
                });

                if ( minute == 59 ) {
                    minute = 60 - step;
                    if (hour > lastHour && lastMinute == 0) {
                        --hour;
                    }
                } else if ( minute == 60 - step + 1 ) {
                    minute = 0;
                    ++hour;
                } else if ( minute > lastMinute ) {
                    minute = lastMinute + step;
                } else if ( minute < lastMinute ) {
                    minute = lastMinute - step;
                }

                if ( hour <= minHour || hour == 23 ) {
                    hour = minHour;
                    if ( minute <= minMinute) {
                        minute = minMinute;
                    }
                } else if ( hour >= maxHour) {
                    hour = maxHour;
                    minute = maxMinute;
                }

                lastMinute = minute;
                lastHour = hour;

                timePicker.setCurrentMinute(minute);
                timePicker.setCurrentHour(hour);
                timePicker.setOnTimeChangedListener(this);
            }
        });

        return timePicker;
    }

    public String format(int seconds) {
        int hours, minutes;
        if ( seconds < step ) {
            hours = minHour;
            minutes = minMinute;
        } else {
            hours = seconds / 60;
            minutes = seconds % 60;
        }
        return format(hours, minutes);
    }

    public String format(int hours, int minutes) {
        return hours + separator + (minutes == 0 ? "00" : minutes);
    }

    public int getValue() {
        return lastHour * 60 + lastMinute;
    }

}

