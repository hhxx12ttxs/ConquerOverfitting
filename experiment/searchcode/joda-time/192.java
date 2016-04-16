package com.apprise.toggl;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.remote.exception.FailedResponseException;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.PlannedTasks;
import com.apprise.toggl.storage.DatabaseAdapter.Projects;
import com.apprise.toggl.storage.DatabaseAdapter.Tags;
import com.apprise.toggl.storage.DatabaseAdapter.Tasks;
import com.apprise.toggl.storage.models.PlannedTask;
import com.apprise.toggl.storage.models.Project;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.tracking.TimeTrackingService;
import com.apprise.toggl.widget.NumberPicker;

import java.util.Calendar;
import java.util.Date;

public class TaskActivity extends ApplicationActivity {

    public static final String TASK_ID = "TASK_ID";
    public static final String NEW_TASK = "NEW_TASK";
    private static final String TAG = "TaskActivity";

    private static final int DATE_DIALOG_ID = 0;
    static final int CREATE_NEW_PROJECT_REQUEST = 1;
    private static final int TIME_START_DIALOG_ID = 2;
    private static final int TIME_STOP_DIALOG_ID = 3;

    private SyncService syncService;
    private DatabaseAdapter dbAdapter;
    private TimeTrackingService trackingService;
    private Task task;
    private Button timeTrackingButton;
    private TextView durationView;
    private AutoCompleteTextView descriptionView;
    private TextView projectView;
    private TextView dateView;
    private TextView plannedTasksView;
    private TextView tagsView;
    private TextView startTimeView;
    private TextView stopTimeView;

    private Toggl app;

    private CheckBox billableCheckBox;
    private boolean startAutomatically = false;
    private boolean deleted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);
        app = (Toggl) getApplication();

        Intent syncServiceIntent = new Intent(this, SyncService.class);
        bindService(syncServiceIntent, syncConnection, BIND_AUTO_CREATE);

        dbAdapter = new DatabaseAdapter(this, (Toggl) getApplication());
        dbAdapter.open();
        app.retrieveCurrentUser(dbAdapter);

        Intent timeTrackingServiceIntent = new Intent(this, TimeTrackingService.class);
        if (!TimeTrackingService.isAlive()) {
            startService(timeTrackingServiceIntent);
        }
        bindService(timeTrackingServiceIntent, trackingConnection, BIND_AUTO_CREATE);

        long _id = getIntent().getLongExtra(TASK_ID, -1);

        this.task = (Task) getLastNonConfigurationInstance();

        if (this.task == null) {
            instantiateTask(_id);
        }

        initViews();
        attachEvents();
    }

    private void instantiateTask(long _id) {
        boolean newTask = false;
        if (_id > 0) {
            task = dbAdapter.findTask(_id);
            newTask = getIntent().getBooleanExtra(NEW_TASK, false);
        } else {
            task = dbAdapter.createDirtyTask();
            newTask = true;
        }

        if (app.getCurrentUser().new_tasks_start_automatically && newTask) {
            startAutomatically = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        long _id = intent.getLongExtra(TASK_ID, -1);
        instantiateTask(_id);
        initViews();
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(
                TimeTrackingService.BROADCAST_SECOND_ELAPSED);
        registerReceiver(updateReceiver, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(updateReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAllViews();
        unfocusAutocompleteField();
    }

    private void updateAllViews() {
        billableCheckBox.setChecked(task.billable);
        updateDescriptionView();
        updateProjectView();
        updatePlannedTasks();
        updatePlannedTaskView();
        updateDuration();
        updateDateView();
        updateTagsView();
        updateTrackingButton();
        updateStartTimeView();
        updateStopTimeView();
    }

    @Override
    protected void onPause() {
        if (!deleted) {
            if ((task.description == null) || (task.description != null && !task.description.equals(descriptionView.getText().toString()))) {
                task.description = descriptionView.getText().toString();
                saveTask();
            }
            if (task.sync_dirty) {
                new Thread(postTaskInBackground).start();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        dbAdapter.close();
        unbindService(trackingConnection);
        unbindService(syncConnection);
        super.onDestroy();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return task;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_menu_delete_task:
                showDeleteTaskDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initViews() {
        timeTrackingButton = (Button) findViewById(R.id.timer_trigger);
        durationView = (TextView) findViewById(R.id.task_timer_duration);
        initDescriptionAutoComplete();
        dateView = (TextView) findViewById(R.id.task_date);
        projectView = (TextView) findViewById(R.id.task_project);
        plannedTasksView = (TextView) findViewById(R.id.task_planned_tasks);
        tagsView = (TextView) findViewById(R.id.task_tags);
        startTimeView = (TextView) findViewById(R.id.task_start_time);
        stopTimeView = (TextView) findViewById(R.id.task_stop_time);
        billableCheckBox = (CheckBox) findViewById(R.id.task_billable_cb);
    }

    private void initDescriptionAutoComplete() {
        descriptionView = (AutoCompleteTextView) findViewById(R.id.task_description);

        String constraint = "";
        Cursor tasksCursor = dbAdapter.findTasksForAutocomplete(constraint);
        startManagingCursor(tasksCursor);

        DescriptionAutocompleteCursorAdapter adapter = new DescriptionAutocompleteCursorAdapter(this, tasksCursor);
        descriptionView.setAdapter(adapter);

        descriptionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Task selectedTask = dbAdapter.findTask(id);
                task.billable = selectedTask.billable;
                task.description = selectedTask.description;
                task.planned_task = selectedTask.planned_task;
                task.project = selectedTask.project;
                task.workspace = selectedTask.workspace;
                task.tag_names = selectedTask.tag_names;
                updateAllViews();
            }
        });

    }

    private void unfocusAutocompleteField() {
        //set focus to invisible view, hide soft keyboard
        ((EditText) findViewById(R.id.invisible_view)).requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(descriptionView.getWindowToken(), 0);
    }

    private void updateProjectView() {
        if (task.project != null) {
            projectView.setText(task.project.client_project_name);
        } else {
            projectView.setText(R.string.choose_tip);
        }
    }

    private void updateDescriptionView() {
        descriptionView.setText(task.description);
    }

    private void updateDateView() {
        if (task.start != null) dateView.setText(Util.smallDateString(Util.parseStringToDate(task.start)));
    }

    private void updatePlannedTaskView() {
        if (task.planned_task != null) {
            plannedTasksView.setText(task.planned_task.name);
        }
    }

    private void updateTagsView() {
        tagsView.setText(Util.joinStringArray(task.tag_names, ", "));
    }

    private void updateStartTimeView() {
        if (task.start != null) startTimeView.setText(Util.smallTimeString(Util.parseStringToDate(task.start)));
    }

    private void updateStopTimeView() {
        if (task.stop != null) stopTimeView.setText(Util.smallTimeString(Util.parseStringToDate(task.stop)));
    }

    private void updateTrackingButton() {
        if (!todaysTask() && ((trackingService != null && !trackingService.isTracking(task)) || trackingService == null)) {
            timeTrackingButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.timer_trigger_button_continue_today));
        } else if (trackingService != null && trackingService.isTracking(task)) {
            timeTrackingButton.setBackgroundResource(R.drawable.trigger_active);
        } else {
            timeTrackingButton.setBackgroundResource(R.drawable.timer_trigger_button);
        }
    }

    private boolean todaysTask() {
        Date startDate = Util.parseStringToDate(task.start);
        Calendar cal = (Calendar) Calendar.getInstance().clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date beginningOfToday = cal.getTime();

        return startDate.after(beginningOfToday);
    }

    private void updatePlannedTasks() {
        if (task.project != null) {
            long project_remote_id = task.project.id;
            Cursor cursor = dbAdapter.findPlannedTasksByProjectId(project_remote_id);
            if ((cursor == null) || (cursor.getCount() == 0) || !cursor.moveToFirst()) {
                findViewById(R.id.task_planned_tasks_area).setVisibility(LinearLayout.GONE);
            } else {
                findViewById(R.id.task_planned_tasks_area).setVisibility(LinearLayout.VISIBLE);
            }
            if (cursor != null) {
                cursor.close();
            }
        } else {
            findViewById(R.id.task_planned_tasks_area).setVisibility(LinearLayout.GONE);
        }
    }

    protected void attachEvents() {
        timeTrackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (todaysTask() || trackingService.isTracking(task)) {
                    triggerTracking();
                } else {
                    continueToday();
                }
            }
        });

        durationView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChooseDurationDialog();
            }
        });

        findViewById(R.id.task_project_area).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChooseProjectDialog();
            }
        });

        findViewById(R.id.task_date_area).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        findViewById(R.id.task_start_time_area).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_START_DIALOG_ID);
            }
        });

        findViewById(R.id.task_stop_time_area).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_STOP_DIALOG_ID);
            }
        });

        billableCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                task.billable = billableCheckBox.isChecked();
                saveTask();
            }
        });

        findViewById(R.id.task_tags_area).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChooseTagsDialog();
            }
        });

        findViewById(R.id.task_planned_tasks_area).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChoosePlannedTaskDialog();
            }
        });
    }

    private void triggerTracking() {
        boolean startTracking = false;
        if (trackingService.isTracking(task)) {
            task.duration = trackingService.stopTracking();
            task.stop = Util.formatDateToString(Util.currentDate());
            saveTask();
        } else if (trackingService.isTracking()) {
            // is tracking another task, stop it and save
            Task currentlyTracked = trackingService.getTrackedTask();
            currentlyTracked.duration = trackingService.stopTracking();
            currentlyTracked.stop = Util.formatDateToString(Util.currentDate());

            saveTask(currentlyTracked);

            startTracking = true;
        } else {
            startTracking = true;
        }

        if (startTracking) {
            task.duration = trackingService.startTracking(task);
            saveTask();
        }

        updateTrackingButton();
    }

    private void continueToday() {
        Task continueTask = dbAdapter.createDirtyTask();
        continueTask.updateAttributes(task);
        String now = Util.formatDateToString(Util.currentDate());

        continueTask.description = descriptionView.getText().toString();
        continueTask.start = now;
        continueTask.stop = now;
        continueTask.duration = 0;
        continueTask.id = 0;
        dbAdapter.updateTask(continueTask);

        finish();

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra(TASK_ID, continueTask._id);
        intent.putExtra(NEW_TASK, true);
        startActivity(intent);
    }

    private void showChooseProjectDialog() {
        final Cursor projectsCursor = dbAdapter.findAllProjects();
        startManagingCursor(projectsCursor);

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        builder.setTitle(R.string.project);

        builder.setSingleChoiceItems(projectsCursor, getCheckedItem(projectsCursor,
                task.project), Projects.CLIENT_PROJECT_NAME,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        projectsCursor.moveToPosition(which);
                        long clickedId = projectsCursor.getLong(projectsCursor
                                .getColumnIndex(Projects._ID));
                        Project project = dbAdapter.findProject(clickedId);
                        task.project = project;
                        task.billable = project.billable;
                        saveTask();
                        billableCheckBox.setChecked(task.billable);
                        updateProjectView();
                        updatePlannedTasks();
                        dialog.dismiss();
                    }
                });
        builder.setPositiveButton(R.string.create_new,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TaskActivity.this,
                                CreateProjectActivity.class);
                        startActivityForResult(intent, CREATE_NEW_PROJECT_REQUEST);
                    }
                });
        builder.setNeutralButton(R.string.no_project,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        task.project = null;
                        saveTask();
                        updateProjectView();
                        updatePlannedTasks();
                    }
                });

        builder.show();
    }

    private void showChoosePlannedTaskDialog() {
        final Cursor plannedTasksCursor = dbAdapter.findPlannedTasksByProjectId(task.project.id);
        startManagingCursor(plannedTasksCursor);

        String[] from = new String[]{PlannedTasks.NAME};
        int[] to = new int[]{R.id.item_name};
        final SimpleCursorAdapter plannedTasksAdapter = new SimpleCursorAdapter(
                TaskActivity.this, R.layout.dialog_list_item, plannedTasksCursor, from, to);

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        builder.setTitle(R.string.planned_task);

        builder.setAdapter(plannedTasksAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                long clickedId = plannedTasksAdapter.getItemId(pos);
                PlannedTask plannedTask = dbAdapter.findPlannedTask(clickedId);
                task.project = plannedTask.project;
                task.workspace = plannedTask.workspace;
                task.planned_task = plannedTask;
                if (descriptionView.length() == 0) {
                    task.description = plannedTask.name;
                    updateDescriptionView();
                }
                updateProjectView();
                updatePlannedTaskView();
                saveTask();
            }
        });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.show();
    }

    private void showChooseTagsDialog() {
        final Cursor tagsCursor = dbAdapter.findAllTags();
        startManagingCursor(tagsCursor);

        final boolean[] checkedItems = new boolean[tagsCursor.getCount()];
        final String[] items = new String[tagsCursor.getCount()];
        if (tagsCursor.moveToFirst()) {
            // collect all tag names
            for (int i = 0; i < items.length; tagsCursor.moveToNext(), i++) {
                items[i] = tagsCursor.getString(tagsCursor.getColumnIndex(Tags.NAME));

                if (task.tag_names != null) {
                    // find match
                    for (int j = 0; j < task.tag_names.length; j++) {
                        if (items[i].equals(task.tag_names[j])) {
                            checkedItems[i] = true;
                            break;
                        }
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        builder.setTitle(R.string.tags);

        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which,
                                boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        });
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int count = 0;
                // collect amount of checked tags
                for (int i = 0; i < checkedItems.length; i++) if (checkedItems[i]) count += 1;

                String[] newTagNames = new String[count];
                for (int i = 0, j = 0; i < items.length; i++) {
                    if (checkedItems[i]) {
                        newTagNames[j++] = items[i];
                    }
                }

                task.tag_names = newTagNames;
                saveTask();
                updateTagsView();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    private void showChooseDurationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        View durationPicker = getLayoutInflater().inflate(R.layout.duration_picker, null);

        final NumberPicker hoursPicker = (NumberPicker) durationPicker
                .findViewById(R.id.picker_duration_hours);
        final NumberPicker minutesPicker = (NumberPicker) durationPicker
                .findViewById(R.id.picker_duration_minutes);
        hoursPicker.setRange(0, 23);
        minutesPicker.setRange(0, 59);

        hoursPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        minutesPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

        hoursPicker.setCurrent(Util.getHoursFromSeconds(task.duration));
        minutesPicker.setCurrent(Util.getMinutesFromSeconds(task.duration));

        builder.setTitle(Util.hoursMinutesSummary(hoursPicker.getCurrent(),
                minutesPicker.getCurrent(), getResources()));
        builder.setView(durationPicker);
        builder.setPositiveButton(R.string.set,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // make sure last values are validated and stored
                        hoursPicker.onFocusChange(hoursPicker.findViewById(R.id.timepicker_input), false);
                        minutesPicker.onFocusChange(minutesPicker.findViewById(R.id.timepicker_input), false);

                        int hours = hoursPicker.getCurrent();
                        int minutes = minutesPicker.getCurrent();
                        // only hours and minutes are picked, hence get the
                        // seconds from existing task duration
                        int seconds = (int) Util.convertIfRunningTime(task.duration) % 60;

                        long duration = (hours * 60 * 60) + (minutes * 60) + seconds;
                        if (trackingService.isTracking(task)) {
                            trackingService.setCurrentDuration(duration);
                        }

                        task.duration = duration;
                        saveTask();
                        updateDuration();
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        final AlertDialog durationDialog = builder.show();

        NumberPicker.OnChangedListener numbersListener = new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker picker, int oldVal, int newVal) {
                durationDialog.setTitle(Util.hoursMinutesSummary(hoursPicker
                        .getCurrent(), minutesPicker.getCurrent(), getResources()));
            }
        };

        hoursPicker.setOnChangeListener(numbersListener);
        minutesPicker.setOnChangeListener(numbersListener);
    }

    protected void saveTask() {
        saveTask(task);
    }

    protected void saveTask(Task task) {
        Log.d(TAG, "saving task: " + task);
        task.sync_dirty = true;
        if (task._id > 0) {
            dbAdapter.updateTask(task);
        } else {
            this.task = dbAdapter.createTask(task);
        }
    }

    private void showDeleteTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        builder.setTitle(R.string.delete_task);
        builder.setMessage(R.string.are_you_sure);

        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (trackingService.isTracking(task)) {
                            trackingService.stopTracking();
                        }
                        dbAdapter.deleteTask(task);
                        if (task.id > 0) {
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        syncService.deleteRemoteTask(task);
                                    } catch (FailedResponseException e) {
                                        Log.e(TAG, "FailedResponseException", e);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception", e);
                                    }
                                }
                            }).start();
                        }
                        deleted = true;
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.show();
    }

    private void setDate(int year, int month, int date) {
        Date start = Util.parseStringToDate(task.start);
        Date stop = Util.parseStringToDate(task.stop);

        Calendar cal = (Calendar) Calendar.getInstance().clone();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, date);
        cal.set(Calendar.HOUR_OF_DAY, start.getHours());
        cal.set(Calendar.MINUTE, start.getMinutes());
        cal.set(Calendar.SECOND, start.getSeconds());
        task.start = Util.formatDateToString(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, stop.getHours());
        cal.set(Calendar.MINUTE, stop.getMinutes());
        cal.set(Calendar.SECOND, stop.getSeconds());
        task.stop = Util.formatDateToString(cal.getTime());

        saveTask();
        updateDateView();
    }

    private void setTime(int hours, int minutes, boolean isStart) {
        Date date;
        if (isStart) {
            date = Util.parseStringToDate(task.start);
        } else {
            date = Util.parseStringToDate(task.stop);
        }

        Calendar cal = (Calendar) Calendar.getInstance().clone();
        cal.set(Calendar.YEAR, date.getYear() + 1900);
        cal.set(Calendar.MONTH, date.getMonth());
        cal.set(Calendar.DATE, date.getDate());
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, 0);

        // don't check to make sure start is before end, etc..., because we set them one at a time
        // not both at once
        if (isStart) {
            task.start = Util.formatDateToString(cal.getTime());
        } else {
            task.stop = Util.formatDateToString(cal.getTime());
        }

        // getTime returns milliseconds
        task.duration = (Util.parseStringToDate(task.stop).getTime() - Util.parseStringToDate(task.start).getTime()) / 1000;

        saveTask();

        if (isStart) {
            updateStartTimeView();
        } else {
            updateStopTimeView();
        }
        updateDuration();
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int month, int date) {
            setDate(year, month, date);
            updateTrackingButton();
        }
    };

    private TimePickerDialog.OnTimeSetListener mStartTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hours, int minutes) {
            setTime(hours, minutes, true);
            updateTrackingButton();
        }
    };

    private TimePickerDialog.OnTimeSetListener mStopTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hours, int minutes) {
            setTime(hours, minutes, false);
            updateTrackingButton();
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                Calendar cal = Util.parseStringToCalendar(task.start);
                int mYear = cal.get(Calendar.YEAR);
                int mMonth = cal.get(Calendar.MONTH);
                int mDay = cal.get(Calendar.DATE);
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
            case TIME_START_DIALOG_ID:
                Date startDate = Util.parseStringToDate(task.start);
                int mStartHours = startDate.getHours();
                int mStartMinutes = startDate.getMinutes();
                return new TimePickerDialog(this, mStartTimeSetListener, mStartHours, mStartMinutes, true);
            case TIME_STOP_DIALOG_ID:
                Date stopDate = Util.parseStringToDate(task.stop);
                int mStopHours = stopDate.getHours();
                int mStopMinutes = stopDate.getMinutes();
                return new TimePickerDialog(this, mStopTimeSetListener, mStopHours, mStopMinutes, true);
        }
        return null;
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (trackingService.isTracking(task)) {
                task.duration = trackingService.getCurrentDuration();
                updateDuration();
            }
        }

    };

    private ServiceConnection trackingConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            TimeTrackingService.TimeTrackingBinder binding = (TimeTrackingService.TimeTrackingBinder) service;
            trackingService = binding.getService();

            if (trackingService.isTracking(task)) {
                task.duration = trackingService.getCurrentDuration();
                updateDuration();
                timeTrackingButton.setBackgroundResource(R.drawable.trigger_active);
            } else if (startAutomatically) {
                triggerTracking();
            }
        }

        public void onServiceDisconnected(ComponentName name) {

        }

    };

    private void updateDuration() {
        durationView.setText(Util.secondsToHMS(task.duration));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_NEW_PROJECT_REQUEST) {
            if (resultCode == RESULT_OK) {
                long createdId = data.getLongExtra(
                        CreateProjectActivity.CREATED_PROJECT_LOCAL_ID, 0);
                if (createdId > 0) {
                    task.project = dbAdapter.findProject(createdId);
                    saveTask();
                    updateProjectView();
                }
            }
        }
    }

    protected ServiceConnection syncConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
        }

        public void onServiceConnected(ComponentName name, IBinder serviceBinding) {
            SyncService.SyncBinder binding = (SyncService.SyncBinder) serviceBinding;
            syncService = binding.getService();
        }

    };


    protected Runnable postTaskInBackground = new Runnable() {

        public void run() {
            if (app.isConnected()) {
                try {
                    syncService.createOrUpdateRemoteTask(task);
                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                }
            }
        }
    };

    public class DescriptionAutocompleteCursorAdapter extends CursorAdapter implements Filterable {

        public DescriptionAutocompleteCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dropdown_item, parent, false);
            bindView(view, context, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String text = getItemText(cursor);
            TextView itemText = (TextView) view.findViewById(R.id.dropdown_item_text);
            itemText.setText(text);
        }

        private String getItemText(Cursor cursor) {
            String description = cursor.getString(cursor.getColumnIndex(Tasks.DESCRIPTION));
            String clientProjectName = cursor.getString(cursor.getColumnIndex(Projects.CLIENT_PROJECT_NAME));
            String text;
            if (clientProjectName != null && !clientProjectName.equals("")) {
                text = description + " - " + clientProjectName;
            } else {
                text = description;
            }
            return text;
        }

        @Override
        public String convertToString(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(Tasks.DESCRIPTION));
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }

            if (constraint == null) constraint = "";
            Cursor c = dbAdapter.findTasksForAutocomplete(constraint);
            startManagingCursor(c);

            return c;
        }
    }

}

