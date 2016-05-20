package train.book.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import train.book.Const;
import train.book.HistoryAdapter;
import train.book.NumberDecimalPicker;
import train.book.R;
import train.book.data.HistoryData;
import train.book.data.WeightData;
import train.book.data.WorkoutData;
import train.book.db.DBWeightData;
import train.book.db.DBWorkoutData;
import train.book.db.DataBaseHandler;
import train.book.db.DataBaseHandler.DBRunnable;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends CustomWindow {
	private ProgressBar progressTitle;
	private HistoryAdapter historyAdapter;
	private AlertDialog editWeightDialog;
	private NumberDecimalPicker dialogWeight;
	private DatePickerDialog datePickerDialog;

	private Calendar c;

	private static ArrayList<HistoryData> historyList = new ArrayList<HistoryData>();
	private static WorkoutData editData;
	private static WeightData editWeight;
	private static HistoryData changeDate;

	private static final int LOAD_SIZE = 10;

	private static final int ID_EDIT = 18;
	private static final int ID_CHANGE_DATE = 19;
	private static final int ID_DELETE = 20;

	private static final int EDIT_WORKOUT = 7;

	private static final int DIALOG_EDIT_WEIGHT = 1;
	private static final int DIALOG_CHANGE_DATE = 2;

	private boolean isFirst = true;
	private static final String TMP_IS_FIRST = "tmp_i_f";
	private static final String TMP_WEIGHT_VALUE = "tmp_w_v";

	private static boolean updating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);

		DataBaseHandler.init(getApplicationContext());

		progressTitle = (ProgressBar) findViewById(R.id.progressBarTitle);
		historyAdapter = new HistoryAdapter(this, historyList);
		ListView lv = (ListView) findViewById(R.id.listViewHistory);
		lv.setAdapter(historyAdapter);

		// Add long click thing for the list view
		registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HistoryData d = (HistoryData) parent
						.getItemAtPosition(position);
				if (d instanceof WorkoutData) {
					editWorkoutData((WorkoutData) d);
				} else if (d instanceof WeightData) {
					editWeightData((WeightData) d);
				}
			}
		});

		View layout = getLayoutInflater().inflate(R.layout.add_weight_data,
				null);
		dialogWeight = (NumberDecimalPicker) layout
				.findViewById(R.id.numberDecimalPickerAddWeightData);
		dialogWeight.setValue(Const.DEFAULT_WEIGHT);
		editWeightDialog = new AlertDialog.Builder(this)
				.setView(layout)
				.setTitle(getString(R.string.title_edit_weight))
				.setPositiveButton(getString(R.string.button_ok),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								updateWeight();
							}
						})
				.setNegativeButton(getString(R.string.button_cancel), null)
				.create();

		c = Calendar.getInstance();
		datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				changeDate(year, monthOfYear, dayOfMonth);
			}
		}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));
	}

	@Override
	protected void onResume() {
		if (isFirst) {
			isFirst = false;

			progressTitle.setVisibility(View.VISIBLE);
			updateHistoryList();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// Clean up static objects...
			historyList.clear();
			isFirst = true;
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.history_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.history_menu_chart:
			showCharts();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_EDIT_WEIGHT:
			return editWeightDialog;
		case DIALOG_CHANGE_DATE:
			return datePickerDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TMP_IS_FIRST, isFirst);
		outState.putDouble(TMP_WEIGHT_VALUE, dialogWeight.getValue());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		isFirst = savedInstanceState.getBoolean(TMP_IS_FIRST);
		dialogWeight.setValue(savedInstanceState.getDouble(TMP_WEIGHT_VALUE));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listViewHistory) {
			menu.setHeaderTitle(getString(R.string.history_manage_title));
			menu.add(Menu.NONE, ID_EDIT, Menu.NONE, R.string.workout_edit);
			menu.add(Menu.NONE, ID_CHANGE_DATE, Menu.NONE,
					R.string.workout_change_date);
			menu.add(Menu.NONE, ID_DELETE, Menu.NONE, R.string.workout_delete);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ID_EDIT: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			HistoryData d = historyAdapter.getItem(info.position);
			if (d instanceof WorkoutData) {
				editWorkoutData((WorkoutData) d);
			} else if (d instanceof WeightData) {
				editWeightData((WeightData) d);
			}
			return true;
		}
		case ID_CHANGE_DATE: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			HistoryData d = historyAdapter.getItem(info.position);
			editDate(d);
			return true;
		}
		case ID_DELETE: {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			final HistoryData d = historyAdapter.getItem(info.position);
			historyAdapter.remove(d);
			if (d instanceof WorkoutData) {
				deleteWorkoutData((WorkoutData) d);
			} else if (d instanceof WeightData) {
				deleteWeightData((WeightData) d);
			}
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_WORKOUT) {
			if (resultCode == RESULT_OK) {
				editData.commit();
				historyAdapter.notifyDataSetChanged();
				updateWorkoutData(editData);
			} else {
				editData.revert();
			}
		}
	}

	private void showCharts() {
		Intent intent = new Intent(HistoryActivity.this,
				HistoryChartActivity.class);
		startActivity(intent);
	}

	private void editWorkoutData(WorkoutData d) {
		editData = d;
		editData.startEditing();
		Intent intent = new Intent(HistoryActivity.this,
				ExerciseHistoryActivity.class);
		startActivityForResult(intent, EDIT_WORKOUT);
	}

	private void deleteWorkoutData(final WorkoutData d) {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				DBWorkoutData.delete(db, d);

				runOnUiThread(new Runnable() {

					public void run() {
						TrainbookActivity.updateWorkoutDate(d.getWorkoutId());
					}
				});
			}
		});
	}

	private void updateWorkoutData(final WorkoutData d) {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				DBWorkoutData.update(db, d);

				runOnUiThread(new Runnable() {

					public void run() {
						TrainbookActivity.updateWorkoutDate(d.getWorkoutId());
					}
				});
			}
		});
	}

	private void editDate(HistoryData d) {
		changeDate = d;
		c.setTimeInMillis(d.getTime());
		datePickerDialog.updateDate(c.get(Calendar.YEAR),
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		showDialog(DIALOG_CHANGE_DATE);
	}

	private void changeDate(int year, int monthOfYear, int dayOfMonth) {
		dismissDialog(DIALOG_CHANGE_DATE);
		c.set(year, monthOfYear, dayOfMonth);
		changeDate.setTime(c.getTimeInMillis());

		if (changeDate instanceof WorkoutData) {
			updateWorkoutData((WorkoutData) changeDate);
		} else if (changeDate instanceof WeightData) {
			updateWeightData((WeightData) changeDate);
		}

		sortData();
		historyAdapter.notifyDataSetChanged();
	}

	private void editWeightData(WeightData d) {
		editWeight = d;
		dialogWeight.setValue(d.getWeight());
		showDialog(DIALOG_EDIT_WEIGHT);
	}

	private void updateWeight() {
		dismissDialog(DIALOG_EDIT_WEIGHT);
		double weight = dialogWeight.getValue();
		editWeight.setWeight(weight);
		updateWeightData(editWeight);

		historyAdapter.notifyDataSetChanged();
	}

	private void deleteWeightData(final WeightData d) {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				DBWeightData.delete(db, d);
			}
		});
	}

	private void updateWeightData(final WeightData d) {
		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				DBWeightData.update(db, d);
			}
		});
	}

	private void updateHistoryList() {
		historyList.clear();
		updating = true;

		DataBaseHandler.post(new DBRunnable() {

			@Override
			public void run(SQLiteDatabase db) {
				// TODO LOW: Try to load all sets first, then all exercise data
				// etc. see if that is faster
				// TODO LOW: Maybe load in proper order?

				// Load 10 at a time and abort if isClosing
				boolean hasMoreWorkoutData = true;
				boolean hasMoreWeightData = true;
				for (int i = 0; i < Integer.MAX_VALUE
						&& (hasMoreWeightData || hasMoreWorkoutData)
						&& !DataBaseHandler.isClosing(); i += LOAD_SIZE) {

					// Load workout data
					if (hasMoreWorkoutData && !DataBaseHandler.isClosing()) {
						final ArrayList<WorkoutData> tmp = DBWorkoutData.loadX(
								db, LOAD_SIZE, i);
						hasMoreWorkoutData = tmp.size() != 0;

						runOnUiThread(new Runnable() {

							public void run() {
								for (WorkoutData d : tmp) {
									historyList.add(d);
								}
								sortData();
								historyAdapter.notifyDataSetChanged();
							}
						});
					}

					// Load weight data
					if (hasMoreWeightData && !DataBaseHandler.isClosing()) {
						final ArrayList<WeightData> tmp = DBWeightData.loadX(
								db, LOAD_SIZE, i);
						hasMoreWeightData = tmp.size() != 0;

						runOnUiThread(new Runnable() {

							public void run() {
								for (WeightData d : tmp) {
									historyList.add(d);
								}
								sortData();
								historyAdapter.notifyDataSetChanged();
							}
						});
					}
				}

				// Hide the progress
				runOnUiThread(new Runnable() {

					public void run() {
						progressTitle.setVisibility(View.GONE);
						updating = false;
						// Hide progress bar in the history chart
						HistoryChartActivity.hideProgressBar();
					}
				});
			}
		});
	}

	private void sortData() {
		Collections.sort(historyList, new Comparator<HistoryData>() {
			public int compare(HistoryData lhs, HistoryData rhs) {
				return Long.valueOf(rhs.getTime()).compareTo(lhs.getTime());
			}
		});
	}

	public static WorkoutData getEditData() {
		return editData;
	}

	public static ArrayList<HistoryData> getHistoryList() {
		return historyList;
	}

	public static boolean isUpdating() {
		return updating;
	}
}

