package train.book.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import train.book.ActiveWorkoutAdapter;
import train.book.Const;
import train.book.NumberDecimalPicker;
import train.book.R;
import train.book.activities.step.WorkoutEndActivity;
import train.book.activities.step.WorkoutStepBaseActivity;
import train.book.data.ActiveExerciseData;
import train.book.data.ActiveWorkoutData;
import train.book.data.Exercise;
import train.book.data.ExerciseType;
import train.book.data.WeightData;
import train.book.data.Workout;
import train.book.db.DBExercise;
import train.book.db.DBWeightData;
import train.book.db.DBWorkout;
import train.book.db.DBWorkoutData;
import train.book.db.DataBaseHandler;
import train.book.db.DataBaseHandler.DBRunnable;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TrainbookActivity extends CustomWindow {
	private static final int START_WORKOUT = 1;
	private static final int EDIT_WORKOUT = 42;
	private static final int NEW_WORKOUT = 43;
	private static final int SETTINGS = 45;

	private static final int ID_EDIT = 5;
	private static final int ID_DELETE = 6;
	private static final int ID_START = 7;
	private static final int ID_PREV = 8;

	private ProgressBar progressTitle;
	private TextView textDate;
	private static ActiveWorkoutData activeData;
	private ActiveWorkoutAdapter workoutAdapter;
	private static ArrayList<Long> workoutUpdates = new ArrayList<Long>();
	private static ArrayList<Exercise> exerciseList = new ArrayList<Exercise>();
	private static ArrayList<Workout> workoutList = new ArrayList<Workout>();
	private static ArrayList<Pair<Workout, Long>> workoutHistoryList = new ArrayList<Pair<Workout, Long>>();
	private AlertDialog addWeightDialog;
	private NumberDecimalPicker dialogWeight;
	private DatePickerDialog datePickerDialog;
	private Button buttonChangeDate, buttonRestoreDate;
	private static final String TMP_WEIGHT_VALUE = "tmp_w_v";
	private static final String TMP_DATE_VALUE = "tmp_d_v";
	private static final String TMP_DATE_CUSTOM = "tmp_d_c";
	private static final String TMP_IS_FIRST = "tmp_i_f";
	public static final String WORKOUT_STEP_INDEX = "wo_step_index";

	private static Workout editData;
	private static Workout previousWorkout;
	private static boolean workoutShowStateChanged = false;

	private Calendar c;
	private boolean isCustomDate = false;

	private boolean isFirst = true;

	private static final int DIALOG_ADD_WEIGHT = 1;
	private static final int DIALOG_PICK_DATE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// TODO LOW: Disable orientation in all activities?

		// TODO LOW: Fix all strings!
		// TODO MED: Delete all button

		// TODO LOW: Deleting workoutdata breaks view previous! (probably fixed)

		DataBaseHandler.init(getApplicationContext());

		c = Calendar.getInstance();
		textDate = (TextView) findViewById(R.id.textViewMainTitleDate);

		progressTitle = (ProgressBar) findViewById(R.id.progressBarTitle);
		buttonChangeDate = (Button) findViewById(R.id.buttonMainChange);
		buttonRestoreDate = (Button) findViewById(R.id.buttonMainRestore);

		ListView lv = (ListView) findViewById(R.id.listViewSelectPass);
		workoutAdapter = new ActiveWorkoutAdapter(this, workoutHistoryList);
		lv.setAdapter(workoutAdapter);

		// Add long click thing for the list view
		registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				@SuppressWarnings("unchecked")
				Pair<Workout, Long> p = (Pair<Workout, Long>) parent
						.getItemAtPosition(position);
				startWorkout(p.first);
			}
		});

		datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				changeDate(year, monthOfYear, dayOfMonth);
			}
		}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));

		View layout = getLayoutInflater().inflate(R.layout.add_weight_data,
				null);
		dialogWeight = (NumberDecimalPicker) layout
				.findViewById(R.id.numberDecimalPickerAddWeightData);
		dialogWeight.setIncreaseValue(Const.WEIGHT_INCREASE);

		addWeightDialog = new AlertDialog.Builder(this)
				.setView(layout)
				.setTitle(getString(R.string.title_add_weight))
				.setPositiveButton(getString(R.string.button_ok),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								addWeight();
							}
						})
				.setNegativeButton(getString(R.string.button_cancel), null)
				.create();

	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			DataBaseHandler.quit();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (isFirst) {
			isFirst = false;

			progressTitle.setVisibility(View.VISIBLE);
			updateData();
		} else if (workoutShowStateChanged) {
			workoutShowStateChanged = false;
			updateWorkoutHistoryList();
			workoutUpdates.clear();
		} else if (workoutUpdates.size() > 0) {
			// Update list if needed
			if (workoutUpdates.size() > workoutList.size() / 2) {
				updateWorkoutHistoryList();
			} else {
				for (long workoutId : workoutUpdates) {
					updateWorkoutHistoryItem(workoutId);
				}
			}
			workoutUpdates.clear();
		}
		updateTitle();
		updateDateButton();
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TMP_IS_FIRST, isFirst);
		outState.putDouble(TMP_WEIGHT_VALUE, dialogWeight.getValue());
		outState.putLong(TMP_DATE_VALUE, c.getTime().getTime());
		outState.putBoolean(TMP_DATE_CUSTOM, isCustomDate);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		isFirst = savedInstanceState.getBoolean(TMP_IS_FIRST);
		dialogWeight.setValue(savedInstanceState.getDouble(TMP_WEIGHT_VALUE));
		c.setTime(new Date(savedInstanceState.getLong(TMP_DATE_VALUE)));
		isCustomDate = savedInstanceState.getBoolean(TMP_DATE_CUSTOM);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ADD_WEIGHT:
			return addWeightDialog;
		case DIALOG_PICK_DATE:
			return datePickerDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create_workout:
			createWorkout();
			return true;
		case R.id.menu_history:
			showHistory();
			return true;
		case R.id.menu_settings:
			showSettings();
			return true;
		case R.id.menu_add_weight:
			refreshDate();
			showDialog(DIALOG_ADD_WEIGHT);
			return true;
		case R.id.menu_login:
			// TODO LOW: Login
			return true;
		case R.id.menu_sync:
			// TODO LOW: Sync
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listViewSelectPass) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			Pair<Workout, Long> p = workoutAdapter.getItem(info.position);
			menu.setHeaderTitle(p.first.getName());
			menu.add(Menu.NONE, ID_START, Menu.NONE, R.string.workout_start);
			menu.add(Menu.NONE, ID_PREV, Menu.NONE,
					R.string.workout_view_previous);
			menu.add(Menu.NONE, ID_EDIT, Menu.NONE, R.string.workout_edit);
			menu.add(Menu.NONE, ID_DELETE, Menu.NONE, R.string.workout_delete);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ID_START: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			startWorkout(workoutAdapter.getItem(info.position).first);
			return true;
		}
		case ID_PREV: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			viewPrevious(workoutAdapter.getItem(info.position));
			return true;
		}
		case ID_EDIT: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			editWorkout(workoutAdapter.getItem(info.position).first);
			return true;
		}
		// TODO MED: Duplicate workout?
		case ID_DELETE: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			final Pair<Workout, Long> p = workoutAdapter.getItem(info.position);
			workoutList.remove(p.first);
			workoutAdapter.remove(p);
			DataBaseHandler.post(new DBRunnable() {

				@Override
				public void run(SQLiteDatabase db) {
					DBWorkout.delete(db, p.first);
				}
			});

			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == START_WORKOUT) {
			if (resultCode == RESULT_OK) {
				final ActiveWorkoutData tmp = activeData;
				activeData = null;

				// Update the date in the list
				updateWorkoutHistoryItem(tmp.getWorkoutId(), tmp.getTime(),
						false);
				DataBaseHandler.post(new DBRunnable() {

					@Override
					public void run(SQLiteDatabase db) {
						DBWorkoutData.store(db, tmp);
					}
				});
			}
		} else if (requestCode == EDIT_WORKOUT) {
			if (resultCode == RESULT_OK) {
				editData.commit();
				sortWorkouts();
				workoutAdapter.notifyDataSetChanged();
				final Workout tmp = editData;
				DataBaseHandler.post(new DBRunnable() {

					@Override
					public void run(SQLiteDatabase db) {
						DBWorkout.update(db, tmp);
					}
				});
			} else {
				editData.revert();
			}
		} else if (requestCode == NEW_WORKOUT) {
			if (resultCode == RESULT_OK) {
				workoutList.add(editData);
				Collections.sort(workoutList); // Sort
				workoutAdapter.add(new Pair<Workout, Long>(editData, Long
						.valueOf(0)));
				sortWorkouts();
				workoutAdapter.notifyDataSetChanged();
				final Workout tmp = editData;
				DataBaseHandler.post(new DBRunnable() {

					@Override
					public void run(SQLiteDatabase db) {
						DBWorkout.store(db, tmp);
					}
				});
			}
		} else if (requestCode == SETTINGS) {
			// TODO LOW: Maybe do something? :D
		}
	}

	public void onClickPickDate(View v) {
		showDialog(DIALOG_PICK_DATE);
	}

	private void updateDateButton() {
		buttonChangeDate.setVisibility(isCustomDate == true ? View.GONE
				: View.VISIBLE);
		buttonRestoreDate.setVisibility(isCustomDate == false ? View.GONE
				: View.VISIBLE);
	}

	public void onClickRestoreDate(View v) {
		isCustomDate = false;
		updateDateButton();
		c = Calendar.getInstance();
		datePickerDialog.updateDate(c.get(Calendar.YEAR),
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		updateTitle();
	}

	private void changeDate(int year, int monthOfYear, int dayOfMonth) {
		dismissDialog(DIALOG_PICK_DATE);
		isCustomDate = true;
		updateDateButton();
		c.set(year, monthOfYear, dayOfMonth);
		updateTitle();
	}

	private void updateTitle() {
		textDate.setText(getDateString());
	}

	private void addWeight() {
		dismissDialog(DIALOG_ADD_WEIGHT);
		final WeightData tmp = new WeightData(c.getTime(),
				dialogWeight.getValue());
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				DBWeightData.store(db, tmp);
			}
		});
	}

	private void refreshDate() {
		if (!isCustomDate) {
			c = Calendar.getInstance();
		}
	}

	private void startWorkout(Workout w) {
		refreshDate();
		activeData = new ActiveWorkoutData(w, c.getTime());
		Intent intent;
		if (activeData.hasNext(0)) {
			// Step
			ExerciseType t = activeData.get(0).getType();
			intent = new Intent(TrainbookActivity.this,
					WorkoutStepBaseActivity.getActivity(t));
		} else {
			// End
			intent = new Intent(TrainbookActivity.this,
					WorkoutEndActivity.class);
		}
		intent.putExtra(WORKOUT_STEP_INDEX, 0);
		startActivityForResult(intent, START_WORKOUT);
	}

	private void viewPrevious(Pair<Workout, Long> p) {
		previousWorkout = p.first;
		// TODO LOW: Maybe don't show at 0?
		// if (p.second != 0) {
		Intent intent = new Intent(TrainbookActivity.this,
				PreviousResultActivity.class);
		startActivity(intent);
		// }
	}

	private void createWorkout() {
		editData = new Workout("");
		Intent intent = new Intent(TrainbookActivity.this,
				WorkoutSettingActivity.class);
		startActivityForResult(intent, NEW_WORKOUT);
	}

	private void showHistory() {
		Intent intent = new Intent(TrainbookActivity.this,
				HistoryActivity.class);
		startActivity(intent);
	}

	private void showSettings() {
		Intent intent = new Intent(TrainbookActivity.this,
				SettingsActivity.class);
		startActivityForResult(intent, SETTINGS);
	}

	private void editWorkout(Workout w) {
		editData = w;
		editData.startEditing();
		Intent intent = new Intent(TrainbookActivity.this,
				WorkoutSettingActivity.class);
		startActivityForResult(intent, EDIT_WORKOUT);
	}

	private void updateData() {
		// Update exercises
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				final ArrayList<Exercise> res = DBExercise.load(db);

				runOnUiThread(new Runnable() {

					public void run() {
						exerciseList.clear();
						for (Exercise e : res) {
							exerciseList.add(e);
						}
						onExerciseLoaded();
					}
				});
			}
		});
		// Update weight
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				WeightData w = DBWeightData.getLastWeight(db);
				final double weight = w.getWeight();

				runOnUiThread(new Runnable() {

					public void run() {
						dialogWeight.setValue(weight);
					}
				});
			}
		});
	}

	private void onExerciseLoaded() {
		updateWorkoutListFromShell();
	}

	private void updateWorkoutListFromShell() {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				final ArrayList<Workout> res = DBWorkout.loadShell(db,
						exerciseList);

				runOnUiThread(new Runnable() {

					public void run() {
						workoutList.clear();
						for (Workout w : res) {
							workoutList.add(w);
						}
						onWorkoutLoaded();
					}
				});
			}
		});
	}

	private void onWorkoutLoaded() {
		updateWorkoutHistoryList();
	}

	private void updateWorkoutHistoryList() {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				final ArrayList<Pair<Workout, Long>> res = DBWorkoutData
						.getLatestTimes(db, workoutList);

				runOnUiThread(new Runnable() {

					public void run() {
						workoutHistoryList.clear();
						for (Pair<Workout, Long> p : res) {
							if (p.first.doShow()) {// Hide
								workoutHistoryList.add(p);
							}
						}
						onWorkoutHistoryLoaded();
					}
				});
			}
		});
	}

	private void onWorkoutHistoryLoaded() {
		sortWorkouts();
		progressTitle.setVisibility(View.GONE);
		workoutAdapter.notifyDataSetChanged();
	}

	private void sortWorkouts() {
		Collections.sort(workoutHistoryList,
				new Comparator<Pair<Workout, Long>>() {

					public int compare(Pair<Workout, Long> lhs,
							Pair<Workout, Long> rhs) {
						// First by last date, then by default
						int longCmp = lhs.second.compareTo(rhs.second);
						if (longCmp == 0) {
							return lhs.first.compareTo(rhs.first);
						}
						return longCmp;
					}
				});
	}

	private void updateWorkoutHistoryItem(final long workoutId) {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				final long time = DBWorkoutData.getLatestTime(db, workoutId);

				runOnUiThread(new Runnable() {

					public void run() {
						updateWorkoutHistoryItem(workoutId, time, true);
					}
				});
			}
		});
	}

	private void updateWorkoutHistoryItem(long workoutId, long time,
			boolean force) {
		for (int i = 0; i < workoutHistoryList.size(); i++) {
			Pair<Workout, Long> p = workoutHistoryList.get(i);
			if (p.first.getId() == workoutId) {
				if (p.second < time || force) {
					// Only update if time is newer
					workoutHistoryList.remove(i);
					workoutHistoryList.add(new Pair<Workout, Long>(p.first,
							time));
				}
				break;
			}
		}
		sortWorkouts();
		workoutAdapter.notifyDataSetChanged();
	}

	private String getDateString() {
		return Const.date_medium_format.format(c.getTime());
	}

	public static void setWorkoutComment(String comment) {
		activeData.setComment(comment);
	}

	public static ActiveExerciseData getActiveExerciseData(int index) {
		return activeData.get(index);
	}

	public static boolean hasNext(int stepIndex) {
		return activeData.hasNext(stepIndex);
	}

	public static boolean isLast(int stepIndex) {
		return activeData.isLast(stepIndex);
	}

	public static ActiveWorkoutData getActiveData() {
		return activeData;
	}

	public static Workout getEditData() {
		return editData;
	}

	public static Workout getPreviousWorkout() {
		return previousWorkout;
	}

	public static ArrayList<Exercise> getExerciseList() {
		return exerciseList;
	}

	public static ArrayList<Workout> getWorkoutList() {
		return workoutList;
	}

	public static void updateWorkoutDate(long workoutId) {
		if (!workoutUpdates.contains(workoutId)) {
			workoutUpdates.add(workoutId);
		}
	}

	public static void setWorkoutShowStateChanged() {
		workoutShowStateChanged = true;
	}
}
