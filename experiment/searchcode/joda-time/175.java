package ru.rogachev.slider.app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.android.openfiledialog.OpenFileDialog;
import ru.rogachev.slider.dialogs.TimePickerDialogWithSeconds;
import ru.rogachev.slider.utils.Constants;

public class PrefsFragment extends PreferenceFragment {

    public static final String TIME_ITEM_FORMAT = "%02d";

    private SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
    private SimpleDateFormat timeFormat = new SimpleDateFormat(Constants.TIME_FORMAT);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        final SharedPreferences sharedPref = getPreferenceManager() != null ? getPreferenceManager().getSharedPreferences() : null;
        if (sharedPref == null) {
            return;
        }

        final Preference slideDelayPref = findPreference(Constants.DELAY_PARAM_NAME);
        if (slideDelayPref != null) {
            String delay = sharedPref.getString(Constants.DELAY_PARAM_NAME, null);
            int hours = 0;
            int minutes = 0;
            int seconds = 0;
            if (delay != null) {
                String[] timeItems = delay.split(":");
                hours = Integer.parseInt(timeItems[0]);
                minutes = Integer.parseInt(timeItems[1]);
                seconds = Integer.parseInt(timeItems[2]);
                slideDelayPref.setSummary(getDelayString(hours, minutes, seconds));
            }
            slideDelayPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    int hours = 0;
                    int minutes = 0;
                    int seconds = 0;
                    String delay = sharedPref.getString(Constants.DELAY_PARAM_NAME, null);
                    if (delay != null) {
                        String[] timeItems = delay.split(":");
                        hours = Integer.parseInt(timeItems[0]);
                        minutes = Integer.parseInt(timeItems[1]);
                        seconds = Integer.parseInt(timeItems[2]);
                    }
                    TimePickerDialogWithSeconds dialog = new TimePickerDialogWithSeconds(getActivity(), new TimePickerDialogWithSeconds.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(ru.rogachev.slider.dialogs.TimePicker view, int hourOfDay, int minute, int second) {
                            String hoursValue = String.format(TIME_ITEM_FORMAT, hourOfDay);
                            String minutesValue = String.format(TIME_ITEM_FORMAT, minute);
                            String secondsValue = String.format(TIME_ITEM_FORMAT, second);
                            slideDelayPref.setSummary(getDelayString(hourOfDay, minute, second));
                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                            prefEditor.putString(Constants.DELAY_PARAM_NAME, hoursValue + ":" + minutesValue + ":" + secondsValue);
                            prefEditor.commit();
                        }
                    }, hours, minutes, seconds);
                    dialog.show();
                    return true;
                }
            });
        }

        final Preference dateStartPref = findPreference(Constants.START_DATE_PARAM_NAME);
        if (dateStartPref != null) {
            String sDate = sharedPref.getString(Constants.START_DATE_PARAM_NAME, null);
            Date startDate = new Date();
            if (sDate != null) {
                try {
                    startDate = dateFormat.parse(sDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dateStartPref.setSummary(sDate);
            }
            final Date finalStartDate = startDate;
            dateStartPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(finalStartDate);
                    Dialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            String date = String.format("%04d", year) + "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d", dayOfMonth);
                            dateStartPref.setSummary(date);
                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                            prefEditor.putString(Constants.START_DATE_PARAM_NAME, date);
                            prefEditor.commit();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                    return true;
                }
            });
        }

        final Preference timeStartPref = findPreference(Constants.START_TIME_PARAM_NAME);
        if (timeStartPref != null) {
            String sTime = sharedPref.getString(Constants.START_TIME_PARAM_NAME, null);
            Date startTime = new Date();
            if (sTime != null) {
                try {
                    startTime = timeFormat.parse(sTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                timeStartPref.setSummary(sTime);
            }
            final Date finalStartTime = startTime;
            timeStartPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(finalStartTime);
                    Dialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String hours = String.format(TIME_ITEM_FORMAT, hourOfDay);
                            String minutes = String.format(TIME_ITEM_FORMAT, minute);
                            timeStartPref.setSummary(hours + ":" + minutes);
                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                            prefEditor.putString(Constants.START_TIME_PARAM_NAME, hours + ":" + minutes);
                            prefEditor.commit();
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    dialog.show();
                    return true;
                }
            });
        }

        final Preference dateEndPref = findPreference(Constants.END_DATE_PARAM_NAME);
        if (dateEndPref != null) {
            String eDate = sharedPref.getString(Constants.END_DATE_PARAM_NAME, null);
            Date endDate = new Date();
            if (eDate != null) {
                try {
                    endDate = dateFormat.parse(eDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dateEndPref.setSummary(eDate);
            }
            final Date finalEndDate = endDate;
            dateEndPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(finalEndDate);
                    Dialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            String date = String.format("%04d", year) + "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d", dayOfMonth);
                            dateEndPref.setSummary(date);
                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                            prefEditor.putString(Constants.END_DATE_PARAM_NAME, date);
                            prefEditor.commit();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                    return true;
                }
            });
        }

        final Preference timeEndPref = findPreference(Constants.END_TIME_PARAM_NAME);
        if (timeEndPref != null) {
            String eTime = sharedPref.getString(Constants.END_TIME_PARAM_NAME, null);
            Date endTime = new Date();
            if (eTime != null) {
                try {
                    endTime = timeFormat.parse(eTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                timeEndPref.setSummary(eTime);
            }
            final Date finalEndTime = endTime;
            timeEndPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(finalEndTime);
                    Dialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String hours = String.format(TIME_ITEM_FORMAT, hourOfDay);
                            String minutes = String.format(TIME_ITEM_FORMAT, minute);
                            timeEndPref.setSummary(hours + ":" + minutes);
                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                            prefEditor.putString(Constants.END_TIME_PARAM_NAME, hours + ":" + minutes);
                            prefEditor.commit();
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    dialog.show();
                    return true;
                }
            });
        }

        final Preference folderNamePref = findPreference(Constants.FOLDER_PARAM_NAME);
        if (folderNamePref != null) {
            final String folderName = sharedPref.getString(Constants.FOLDER_PARAM_NAME, null);
            if (folderName != null) {
                folderNamePref.setSummary(folderName);
            }
            folderNamePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    final String folderName = sharedPref.getString(Constants.FOLDER_PARAM_NAME, null);
                    if (folderName != null) {
                        folderNamePref.setSummary(folderName);
                    }
                    OpenFileDialog dialog = new OpenFileDialog(getActivity(), folderName)
                            .setFilter(".*\\.*")
                            .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                                @Override
                                public void OnSelectedFile(String fileName) {
                                    folderNamePref.setSummary(fileName);
                                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                                    prefEditor.putString(Constants.FOLDER_PARAM_NAME, fileName);
                                    prefEditor.commit();
                                }
                            });
                    dialog.show();
                    return true;
                }
            });
        }
    }

    private String getDelayString(int hours, int minutes, int seconds) {
        String delayString = "" + (hours > 0 ? hours + " h" : "");
        delayString = delayString + (delayString.length() > 0 ? " " : "") + (minutes > 0 ? minutes + " min" : "");
        return delayString + (delayString.length() > 0 ? " " : "") + (seconds > 0 ? seconds + " sec" : "");
    }
}

