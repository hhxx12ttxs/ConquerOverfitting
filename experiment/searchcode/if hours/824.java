package net.assemble.timetone.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.util.AttributeSet;

import net.assemble.timetone.R;
import net.assemble.timetone.preferences.TimetonePreferences.Hours;

/**
 * 読み上げ時刻設定
 */
public class TimetoneHoursPreference extends ListPreference
{
    private Hours mHours = new Hours(0);
    private Hours mNewHours = new Hours(0);

    private SharedPreferences mPref;

    public TimetoneHoursPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        String suffix = context.getResources().getString(R.string.pref_hours_suffix);
        String[] values = new String[] {
                 "0" + suffix,  "1" + suffix,  "2" + suffix,
                 "3" + suffix,  "4" + suffix,  "5" + suffix,
                 "6" + suffix,  "7" + suffix,  "8" + suffix,
                 "9" + suffix, "10" + suffix, "11" + suffix,
                "12" + suffix, "13" + suffix, "14" + suffix,
                "15" + suffix, "16" + suffix, "17" + suffix,
                "18" + suffix, "19" + suffix, "20" + suffix,
                "21" + suffix, "22" + suffix, "23" + suffix
        };
        setEntries(values);
        setEntryValues(values);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mHours.set(mNewHours);
            mPref = getSharedPreferences();
            Editor e = mPref.edit();
            e.putInt(TimetonePreferences.PREF_HOURS_KEY, mHours.getCoded());
            e.commit();
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        mPref = getSharedPreferences();
        int hours = mPref.getInt(TimetonePreferences.PREF_HOURS_KEY, TimetonePreferences.PREF_HOURS_DEFAULT);
        Hours eh = new Hours(hours);
        setHours(eh);

        builder.setMultiChoiceItems(
            getEntries(), mHours.getBooleanArray(),
            new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int which,
                        boolean isChecked) {
                    mNewHours.set(which, isChecked);
                }
            });

    }

    public void setHours(Hours eh) {
        mHours.set(eh);
        mNewHours.set(eh);
    }

    public Hours getHours() {
        return mHours;
    }
}

