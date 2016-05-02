package com.isitajeansday;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JeansDayActivity extends Activity
{
    private static CalendarDates calendarDates;
    private static TextView todayTextView;
    private static TextView tomorrowTextView;
    private static TextView todayDetailsView;
    private static TextView tomorrowDetailsView;
    private static ProgressDialog dialog;
    private static Handler handler;
    private Thread downloadThread;
    private ViewGroup todayContainer;
    private ViewGroup tomorrowContainer;

    @Override
    public void onResume() {
        super.onResume();

        // Create a handler to update the UI
        handler = new Handler();

        // Did we already download the calendar dates?
        if (calendarDates != null) {
            SetUIText();
        }
        // Check if the thread is already running
        downloadThread = (Thread) getLastNonConfigurationInstance();
        if (downloadThread != null && downloadThread.isAlive()) {
            dialog = ProgressDialog.show(this, "Retrieving", "Retrieving Calendar Dates");
        }
        else {
            getCalendar();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jeansday_layout);

        todayTextView = (TextView) this.findViewById(R.id.today_text);
        tomorrowTextView = (TextView) this.findViewById(R.id.tomorrow_text);
        todayDetailsView = (TextView) this.findViewById(R.id.today_details);
        tomorrowDetailsView = (TextView) this.findViewById(R.id.tomorrow_details);
        todayContainer = (ViewGroup) findViewById(R.id.today_container);
        tomorrowContainer = (ViewGroup) findViewById(R.id.tomorrow_container);



        todayTextView.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                applyRotation(0, 0, 90);
            }
        });
        todayTextView.setOnTouchListener(new AdapterView.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    applyRotation(0, 0, 90);
                }
                return true;
            }
        });
        tomorrowTextView.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                applyRotation(1, 0, 90);
            }
        });
        tomorrowTextView.setOnTouchListener(new AdapterView.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    applyRotation(1, 0, 90);
                }
                return true;
            }
        });

        todayDetailsView.setClickable(true);
        todayDetailsView.setFocusable(true);
        todayDetailsView.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                applyRotation(2, 0, 90);
            }
        });
        todayDetailsView.setOnTouchListener(new AdapterView.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    applyRotation(2, 0, 90);
                }
                return true;
            }
        });
        tomorrowDetailsView.setClickable(true);
        tomorrowDetailsView.setFocusable(true);
        tomorrowDetailsView.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                applyRotation(3, 0, 90);
            }
        });
        tomorrowDetailsView.setOnTouchListener(new AdapterView.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    applyRotation(3, 0, 90);
                }
                return true;
            }
        });

        // Since we are caching large views, we want to keep their cache between each animation
        todayContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        tomorrowContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_menu:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.refresh_menu:
                getCalendar(true);
                return true;

        }
        return false;
    }

    public void getCalendar() {
        getCalendar(false);
    }

    public void getCalendar(boolean ignoreCache) {
        long lastRetrieveTimeStamp = getSharedPreferences("One", MODE_PRIVATE).getLong("LastRetrieveTimeStamp", 0);
        File file = new File(getCacheDir(), "CalendarDates.txt");

        // reload after one day
        if (ignoreCache || !file.exists() || lastRetrieveTimeStamp + 86400000 < System.currentTimeMillis()) {
            dialog = ProgressDialog.show(this, "Retrieving", "Retrieving Calendar Dates");
            downloadThread = new MyThread();
            downloadThread.start();

            SharedPreferences.Editor editor = getSharedPreferences("One", MODE_PRIVATE).edit();
            editor.putLong("LastRetrieveTimeStamp", System.currentTimeMillis());
            editor.commit();
        } else {
            Gson gson = new Gson();
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                calendarDates = gson.fromJson(br.readLine(), CalendarDates.class);
                br.close();

            } catch (FileNotFoundException e) {
                Log.e("JeansDay", "Cache file could not be found");
            } catch (IOException e) {
                Log.e("JeansDay", "Cache file could not be read");
            }


            SetUIText();
        }
    }


    // Save the thread
    @Override
    public Object onRetainNonConfigurationInstance() {
        return downloadThread;
    }

    // dismiss dialog if activity is destroyed
    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            calendarDates = new JeansDayService().retrieveJeansDays();
            handler.post(new MyRunnable());
        }
    }

    public void SetUIText()
    {
        if (!calendarDates.ErrorMessage.equals(""))
        {
            todayContainer.setBackgroundResource(R.drawable.jeansday_widget_background_warning);
            tomorrowContainer.setBackgroundResource(R.drawable.jeansday_widget_background_warning);
            Toast.makeText(getApplicationContext(), calendarDates.ErrorMessage, Toast.LENGTH_SHORT).show();
        } else {
            todayContainer.setBackgroundResource(R.drawable.jeansday_widget_background);
            tomorrowContainer.setBackgroundResource(R.drawable.jeansday_widget_background);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = JeansDayService.removeTime(new Date());
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            Date tomorrow = JeansDayService.removeTime(c.getTime());
            if (calendarDates.Days.containsKey(sdf.format(today))) {
                todayTextView.setText("Yes");
                todayDetailsView.setText(calendarDates.Days.get(sdf.format(today)));
            } else {
                todayTextView.setText("No");
                todayDetailsView.setText("This day is not a jeans day");
            }

            if (calendarDates.Days.containsKey(sdf.format(tomorrow))) {
                tomorrowTextView.setText("Yes");
                tomorrowDetailsView.setText(calendarDates.Days.get(sdf.format(tomorrow)));
            } else {
                tomorrowTextView.setText("No");
                tomorrowDetailsView.setText("This day is not a jeans day");
            }
        }

        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.jeansday_widget_layout);
        if (!calendarDates.ErrorMessage.equals(""))
        {
            views.setInt(R.id.widget, "setBackgroundResource", R.drawable.jeansday_widget_background_warning);
            Toast.makeText(context, calendarDates.ErrorMessage, Toast.LENGTH_LONG).show();
        } else {
            views.setInt(R.id.widget, "setBackgroundResource", R.drawable.jeansday_widget_background);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = JeansDayService.removeTime(new Date());
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            Date tomorrow = JeansDayService.removeTime(c.getTime());
            if (calendarDates.Days.containsKey(sdf.format(today))) {
                views.setTextViewText(R.id.widget_today, "Yes");
            } else {
                views.setTextViewText(R.id.widget_today, "No");
            }

            if (calendarDates.Days.containsKey(sdf.format(tomorrow))) {
                views.setTextViewText(R.id.widget_tomorrow, "Yes");
            } else {
                views.setTextViewText(R.id.widget_tomorrow, "No");
            }
        }

        Intent intent = new Intent(context, JeansDayActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_today, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_tomorrow, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_today_label, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_tomorrow_label, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_today_container, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_tomorrow_container, pendingIntent);
        ComponentName thisWidget = new ComponentName(context, JeansDayWidgetProvider.class);
        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    public class MyRunnable implements Runnable {
        public void run() {
            SetUIText();
            File file = new File(getCacheDir(), "CalendarDates.txt");
            try {
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                Gson gson = new Gson();
                bw.write(gson.toJson(calendarDates));
                bw.close();
            } catch (IOException e) {
                Log.e("JeansDay", "Cache file does not exist");
            }
            dialog.dismiss();
        }
    }

    /**
     * Setup a new 3D rotation on the container view.
     *
     * @param position the item that was clicked to show a picture, or -1 to show the list
     * @param start the start angle at which the rotation must begin
     * @param end the end angle of the rotation
     */
    private void applyRotation(int position, float start, float end) {
        // Find the center of the container
        float centerX, centerY;
        if (position % 2 == 0) {
            centerX = todayContainer.getWidth() / 2.0f;
            centerY = todayContainer.getHeight() / 2.0f;
        } else {
            centerX = tomorrowContainer.getWidth() / 2.0f;
            centerY = tomorrowContainer.getHeight() / 2.0f;
        }

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(position));

        if (position % 2  == 0) {
            todayContainer.startAnimation(rotation);
        } else {
            tomorrowContainer.startAnimation(rotation);
        }
    }

    /**
     * This class listens for the end of the first half of the animation.
     * It then posts a new action that effectively swaps the views when the container
     * is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {
        private final int mPosition;

        private DisplayNextView(int position) {
            mPosition = position;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (mPosition % 2 == 0) {
                todayContainer.post(new SwapViews(mPosition));
            } else {
                tomorrowContainer.post(new SwapViews(mPosition));
            }

        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * This class is responsible for swapping the views and start the second
     * half of the animation.
     */
    private final class SwapViews implements Runnable {
        private final int mPosition;

        public SwapViews(int position) {
            mPosition = position;
        }

        public void run() {
            float centerX, centerY;
            if (mPosition % 2 == 0) {
                centerX = todayContainer.getWidth() / 2.0f;
                centerY = todayContainer.getHeight() / 2.0f;
            } else {
                centerX = tomorrowContainer.getWidth() / 2.0f;
                centerY = tomorrowContainer.getHeight() / 2.0f;
            }
            Rotate3dAnimation rotation = null;

            if (mPosition == 0) {
                todayTextView.setVisibility(View.GONE);
                todayDetailsView.setVisibility(View.VISIBLE);
                todayDetailsView.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            } else if (mPosition == 1) {
                tomorrowTextView.setVisibility(View.GONE);
                tomorrowDetailsView.setVisibility(View.VISIBLE);
                tomorrowDetailsView.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            } else if (mPosition == 2) {
                todayDetailsView.setVisibility(View.GONE);
                todayTextView.setVisibility(View.VISIBLE);
                todayTextView.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            } else if (mPosition == 3) {
                tomorrowDetailsView.setVisibility(View.GONE);
                tomorrowTextView.setVisibility(View.VISIBLE);
                tomorrowTextView.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            }

            if (rotation != null) {
                rotation.setDuration(500);
                rotation.setFillAfter(true);
                rotation.setInterpolator(new DecelerateInterpolator());
            }

            if (mPosition % 2 == 0) {
                todayContainer.startAnimation(rotation);
            } else {
                tomorrowContainer.startAnimation(rotation);
            }
        }
    }


}

