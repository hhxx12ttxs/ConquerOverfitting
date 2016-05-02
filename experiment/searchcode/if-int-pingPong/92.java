package com.pingpong.android.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.pingpong.android.R;
import com.pingpong.android.interfaces.DateTimePickerCallBack;
import com.pingpong.android.interfaces.OnAgeSelectedListener;

import java.util.Calendar;

/**
 * Created by JiangZhenJie on 2015/2/15.
 */
public class UIUtils {

    private static ProgressDialog mProgressDialog;

    public static void showSelectorDialog(Context context, String[] items, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(items, clickListener);
        builder.create().show();
    }

    public static void showProgressDialog(Context context, int message) {
        mProgressDialog = ProgressDialog.show(context, null, context.getString(message));
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static void showDateTimePicker(final Context context, final DateTimePickerCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.date_time_picker_layout, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        datePicker.setSpinnersShown(false);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
        builder.setView(view);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.onClick(dialog, datePicker, timePicker);
            }
        });

        builder.create().show();
    }

    public static void showAlertDialog(Context context, int resId, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(resId);
        builder.setNegativeButton(R.string.ok, listener);
        builder.create().show();
    }

    public static void showAgeSelector(Context context, final OnAgeSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.age_selector_layout, null);
        builder.setView(view);
        final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        String[] data = {"ć", "ĺš´"};
        setSpinnerAdapter(context, spinner, data);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    numberPicker.setMinValue(1);
                    numberPicker.setMaxValue(12);
                }else {
                    numberPicker.setMinValue(1);
                    numberPicker.setMaxValue(100);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int value = numberPicker.getValue();
                int type = spinner.getSelectedItemPosition();
                if (listener != null) {
                    listener.onAgeSelected(type, value);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private static void setSpinnerAdapter(Context context, Spinner spinner, String[] data) {
        if (spinner == null || data == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}

