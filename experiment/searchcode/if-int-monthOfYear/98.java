package com.farissyariati.kuma.milestones;

import com.farissyariati.kuma.R;
import com.farissyariati.kuma.select.users.SelectUsersListActivity;
import com.farissyariati.kuma.utility.CollabtiveManager;
import com.farissyariati.kuma.utility.CollabtiveNotificator;
import com.farissyariati.kuma.utility.CollabtiveProfile;
import com.farissyariati.kuma.utility.FFileManager;
import com.farissyariati.kuma.utility.FPreferencesManager;
import com.farissyariati.kuma.utility.FTimeUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "HandlerLeak" })
public class AddMilestone extends Activity {
	private RelativeLayout rlEndMilestone;
	private EditText etMilestoneName, etMilestoneDesc;
	private ProgressDialog pdAddMilestone;
	private TextView tvProjectEndControl;

	private int startDay, startMonth, startYear;
	private int endDay, endMonth, endYear;
	private Thread jsonThread;
	private Thread jsonUsersThread;
	private String resultText;
	private int projectLatestID;

	private String sessionID;
	private String passProjectID;
	private CollabtiveManager collManager;
	private CollabtiveNotificator notificator;
	private FTimeUtility timeUtility;
	static final int DATE_DIALOG_START = 0;
	static final int DATE_DIALOG_END = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.milestone_add_layout);
		initVar();
		initComponents();
		setCallendar();
	}

	private void initComponents() {
		pdAddMilestone = new ProgressDialog(this);
		this.tvProjectEndControl = (TextView) findViewById(R.id.tv_project_control);
		tvProjectEndControl
				.setText("Milestone limit date: "
						+ timeUtility.collabtiveDateFormat(new FPreferencesManager(this).getProjectControlEndTime(),
								"d/M/yyyy"));
		setInnerTypeFace();
		initclickAbleComponent();
		initEditAbleComponents();
	}

	private void initVar() {
		this.timeUtility = new FTimeUtility();
		this.notificator = new CollabtiveNotificator(this);
		passProjectID = getIntent().getIntExtra(CollabtiveProfile.COLL_TAG_MILESTONE_POSTED_ID, 0) + "";
	}

	private void setCallendar() {
		FTimeUtility ftu = new FTimeUtility();
		this.startDay = ftu.getDay();
		this.startMonth = ftu.getMonth();
		this.startYear = ftu.getYear();
		// end project time. set now
		this.endDay = ftu.getDay();
		this.endMonth = ftu.getMonth();
		this.endYear = ftu.getYear();
	}

	private DatePickerDialog.OnDateSetListener startProjectDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			startDay = dayOfMonth;
			startMonth = monthOfYear + 1;
			startYear = year;
			Toast.makeText(getBaseContext(), "Project starts at: " + startDay + "/" + startMonth + "/" + startYear,
					Toast.LENGTH_LONG).show();
		}
	};

	private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			endDay = dayOfMonth;
			endMonth = monthOfYear + 1;
			endYear = year;
			Toast.makeText(getBaseContext(), "Milestone ends at: " + endDay + "/" + endMonth + "/" + endYear,
					Toast.LENGTH_LONG).show();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_START:
			return new DatePickerDialog(this, startProjectDateListener, startYear, startMonth, startDay);
		case DATE_DIALOG_END:
			return new DatePickerDialog(this, endDateListener, endYear, endMonth, endDay);
		default:
			break;
		}
		return null;
	}

	private void setInnerTypeFace() {
		Typeface typeface = Typeface.createFromAsset(this.getAssets(), "Sketch_Block.ttf");
		TextView milestoneName = (TextView) findViewById(R.id.tv_add_milestone_name);
		TextView milestoneDesc = (TextView) findViewById(R.id.tv_add_milestone_description);
		TextView endMilestone = (TextView) findViewById(R.id.tv_add_project_end);

		milestoneName.setTypeface(typeface);
		milestoneDesc.setTypeface(typeface);
		endMilestone.setTypeface(typeface);
	}

	private void initclickAbleComponent() {

		rlEndMilestone = (RelativeLayout) findViewById(R.id.rl_add_project_end);
		rlEndMilestone.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_END);
			}
		});
	}

	private void initEditAbleComponents() {
		this.etMilestoneName = (EditText) findViewById(R.id.et_add_project_name);
		this.etMilestoneDesc = (EditText) findViewById(R.id.et_add_project_description);
	}

	private void clearAllEditText() {
		etMilestoneName.setText(null);
		etMilestoneDesc.setText(null);
	}

	private void onAddMilestoneLoading() {
		pdAddMilestone.setMessage("Adding project to server..");
		pdAddMilestone.setCancelable(false);
		pdAddMilestone.show();
		this.jsonThread = new Thread(new Runnable() {
			@Override
			public void run() {
				collManager = new CollabtiveManager(getBaseContext());
				FPreferencesManager fpm = new FPreferencesManager(getBaseContext());
				sessionID = fpm.getSessionID();
				String milestoneName = etMilestoneName.getText().toString();
				String milestoneDesc = etMilestoneDesc.getText().toString();
				long endMilestone = new FTimeUtility().getUnixTimestamp(endDay, endMonth - 1, endYear);
				String end = endMilestone + "";
				final int status = 1;
				try {
					collManager.getAddMilestoneJSONObjects(passProjectID, sessionID, milestoneName, milestoneDesc, end,
							status + "");
					System.out
							.println("MilestoneList AddMilestone JSON Object: " + collManager.addMilestoneJSONObjects);
					int statusCode = collManager.getAddMilestonesStatusCode();
					if (statusCode == 1) {
						resultText = "Milestone is added successfully";
					} else {
						resultText = "Failed adding a milestone";
					}
					addMilestoneHandler.sendMessage(addMilestoneHandler.obtainMessage());
					jsonThreadDismiss();
				} catch (Exception e) {
					System.out.println("Error dalam Thread Penambahan Milestone");
					e.printStackTrace();
				}
			}
		});
		jsonThread.start();
	}

	void assignAllSelectedUsers() {
		FPreferencesManager fpm = new FPreferencesManager(this);
		String selectedIDChain = fpm.getSelectedIDChain();
		String id[] = selectedIDChain.split(";");
		for (int i = 0; i < id.length; i++) {
			if (!id[i].equals("0") && !id[i].equals("")) {
				collManager.getAssignProjectJSONObjects(id[i], sessionID, projectLatestID + "");
				System.out.println("Get Assigned on ID: " + id[i] + " Latest Project ID: " + projectLatestID);
			}

		}
	}

	void onLoadUsersToSelect() {
		pdAddMilestone.setMessage("Waiting Users List");
		pdAddMilestone.setCancelable(false);
		pdAddMilestone.show();
		// collManager = new CollabtiveManager(
		// CollabtiveProfile.TEMPORARY_COLLABTIVE_URL_HOME);
		collManager = new CollabtiveManager(getBaseContext());
		this.jsonUsersThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					FFileManager ffm = new FFileManager();
					collManager.getUsersJSONObject();
					ffm.writeToFile(CollabtiveProfile.KUMA_FILE_JSON_USERS, collManager.getUsersJSONArray().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				selectUsersHandler.sendMessage(selectUsersHandler.obtainMessage());
				goSelectUsers();
			}
		});
		jsonUsersThread.start();
	}

	Handler addMilestoneHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pdAddMilestone.dismiss();
			Toast.makeText(getBaseContext(), resultText, Toast.LENGTH_LONG).show();
		}
	};

	Handler selectUsersHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pdAddMilestone.dismiss();
			Toast.makeText(getBaseContext(), "Select User/Users", Toast.LENGTH_LONG).show();
		}
	};

	private void jsonThreadDismiss() {
		try {
			// jsonThread.join();
			onUpdateExit();
		} catch (Exception e) {
			System.out.println("error start activity");
			e.printStackTrace();
		}
	}

	private void onUpdateProject() {
		FFileManager ffm = new FFileManager();
		collManager.getMilestonesJSONObjects(passProjectID, sessionID);
		ffm.writeToFile(CollabtiveProfile.KUMA_FILE_JSON_MILESTONES, collManager.getMilestonesJSONArray().toString());
	}

	private void onUpdateExit() {
		onUpdateProject();
		Intent startNewMilestoneList = new Intent(this, MilestonesListActivity.class);
		startActivity(startNewMilestoneList);
		this.finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
			Intent returnProjectList = new Intent(this, MilestonesListActivity.class);
			startActivity(returnProjectList);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void goSelectUsers() {
		Intent selectUsers = new Intent(this, SelectUsersListActivity.class);
		startActivity(selectUsers);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.milestone_add_menu, menu);
		MenuItem miDone = menu.findItem(R.id.done_add_milestone_menu);
		miDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				FTimeUtility ftu = new FTimeUtility();
				FPreferencesManager fpm = new FPreferencesManager(getBaseContext());
				long endMilestone = new FTimeUtility().getUnixTimestamp(endDay, endMonth - 1, endYear);
				if (ftu.onTimeRange(fpm.getProjectControlEndTime(), endMilestone))
					onAddMilestoneLoading();
				else
					notificator.showAlert("Failed Adding Milestone", "Calendar Out of Project Range", "OK");
				return false;
			}
		});

		MenuItem miClear = menu.findItem(R.id.clear_add_milestone_menu);
		miClear.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				clearAllEditText();
				return false;
			}
		});
		return true;
	}

}

