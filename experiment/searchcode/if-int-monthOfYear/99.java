package com.farissyariati.kuma.projects;

import com.farissyariati.kuma.R;
import com.farissyariati.kuma.utility.CollabtiveManager;
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
public class EditProject extends Activity {
	private RelativeLayout rlStartProject;
	private RelativeLayout rlEndProject;
	private EditText etProjectName, etProjectDesc, etProjectBudget;
	private ProgressDialog pdEditProject;

	private int startDay, startMonth, startYear;
	private int endDay, endMonth, endYear;
	private Thread jsonThread;
	private String resultText;

	private String sessionID;
	private CollabtiveManager collManager;

	static final int DATE_DIALOG_START = 0;
	static final int DATE_DIALOG_END = 1;

	private String passProjectName;
	private long passStartProject;
	private long passEndProject;
	private int passProjectBudget;
	private int passProjectID;
	private String passProjectDesc;

	private long endProject;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_edit_layout);
		getPassedParam();
		initComponents();
		setCallendar();
	}

	private void initComponents() {
		pdEditProject = new ProgressDialog(this);
		setInnerTypeFace();
		initclickAbleComponent();
		initEditAbleComponents();
	}

	private void setCallendar() {
		FTimeUtility ftu = new FTimeUtility(passStartProject);
		this.startDay = ftu.getDayFromMillis();
		this.startMonth = ftu.getMonthFromMillis();
		this.startYear = ftu.getYearFromMillis();

		// end project time. set now
		ftu = new FTimeUtility(passEndProject);
		this.endDay = ftu.getDayFromMillis();
		this.endMonth = ftu.getMonthFromMillis();
		this.endYear = ftu.getYearFromMillis();

		endProject = new FTimeUtility().getUnixTimestamp(endDay, endMonth,
				endYear);
	}

	private DatePickerDialog.OnDateSetListener startProjectDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			startDay = dayOfMonth;
			startMonth = monthOfYear + 1;
			startYear = year;
			Toast.makeText(
					getBaseContext(),
					"Project starts at: " + startDay + "/" + startMonth + "/"
							+ startYear, Toast.LENGTH_LONG).show();
		}
	};

	private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			endDay = dayOfMonth;
			endMonth = monthOfYear + 1;
			endYear = year;
			Toast.makeText(
					getBaseContext(),
					"Project ends at: " + endDay + "/" + endMonth + "/"
							+ endYear, Toast.LENGTH_LONG).show();
			endProject = new FTimeUtility().getUnixTimestamp(endDay, endMonth,
					endYear);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_START:
			return new DatePickerDialog(this, startProjectDateListener,
					startYear, startMonth, startDay);
		case DATE_DIALOG_END:
			return new DatePickerDialog(this, endDateListener, endYear,
					endMonth, endDay);
		default:
			break;
		}
		return null;
	}

	private void setInnerTypeFace() {
		Typeface typeface = Typeface.createFromAsset(this.getAssets(),
				"Sketch_Block.ttf");
		TextView projectName = (TextView) findViewById(R.id.tv_edit_project_name);
		TextView projectDesc = (TextView) findViewById(R.id.tv_edit_project_description);
		TextView projectBudget = (TextView) findViewById(R.id.tv_edit_project_budget);
		TextView startProject = (TextView) findViewById(R.id.tv_edit_project_start);
		TextView endProject = (TextView) findViewById(R.id.tv_edit_project_end);
		projectName.setTypeface(typeface);
		projectDesc.setTypeface(typeface);
		projectBudget.setTypeface(typeface);
		startProject.setTypeface(typeface);
		endProject.setTypeface(typeface);
	}

	private void initclickAbleComponent() {

		rlStartProject = (RelativeLayout) findViewById(R.id.rl_edit_project_start);
		rlStartProject.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_START);
			}
		});
		rlEndProject = (RelativeLayout) findViewById(R.id.rl_edit_project_end);
		rlEndProject.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_END);
			}
		});
	}

	private void initEditAbleComponents() {
		this.etProjectName = (EditText) findViewById(R.id.et_edit_project_name);
		this.etProjectName.setText(passProjectName);
		this.etProjectDesc = (EditText) findViewById(R.id.et_edit_project_description);
		this.etProjectDesc.setText(passProjectDesc);
		this.etProjectBudget = (EditText) findViewById(R.id.et_edit_project_budget);
		this.etProjectBudget.setText(passProjectBudget + "");
	}

	private void getPassedParam() {
		this.passProjectID = getIntent().getIntExtra(
				CollabtiveProfile.COLL_TAG_PROJECT_ID, 0);
		this.passProjectDesc = getIntent().getStringExtra(
				CollabtiveProfile.COLL_TAG_PROJECT_DESC);
		this.passProjectName = getIntent().getStringExtra(
				CollabtiveProfile.COLL_TAG_PROJECT_NAME);
		this.passStartProject = getIntent().getLongExtra(
				CollabtiveProfile.COLL_TAG_PROJECT_START, 0);
		this.passEndProject = getIntent().getLongExtra(
				CollabtiveProfile.COLL_TAG_PROJECT_END, 0);
		this.passProjectBudget = getIntent().getIntExtra(
				CollabtiveProfile.COLL_TAG_PROJECT_BUDGET, 0);
	}

	private void clearAllEditText() {
		etProjectName.setText(null);
		etProjectDesc.setText(null);
		etProjectBudget.setText(null);
	}

	private void onEditProjectLoading() {
		pdEditProject.setMessage("Edit Project");
		pdEditProject.setCancelable(false);
		pdEditProject.show();
		this.jsonThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				collManager = new CollabtiveManager(
//						CollabtiveProfile.TEMPORARY_COLLABTIVE_URL_HOME);
				collManager = new CollabtiveManager(getBaseContext());
				FPreferencesManager fpm = new FPreferencesManager(
						getBaseContext());
				sessionID = fpm.getSessionID();
				String projectName = etProjectName.getText().toString();
				String projectDesc = etProjectDesc.getText().toString();
				String projectBudget = etProjectBudget.getText().toString();
				String end = endProject + "";
				try {
					collManager.getEditProjectJSONObject(passProjectID + "",
							sessionID, projectName, projectDesc, end,
							projectBudget);
					int statusCode = collManager.getEditProjectStatusCode();
					if (statusCode == 1) {
						resultText = "Project is edited successfully";
					} else {
						resultText = "Failed editing a project";
					}
					editProjectHandler.sendMessage(editProjectHandler
							.obtainMessage());
					onUpdateExit();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		jsonThread.start();
	}

	Handler editProjectHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pdEditProject.dismiss();
			Toast.makeText(getBaseContext(), resultText, Toast.LENGTH_LONG)
					.show();
		}
	};

	private void jsonThreadDismiss() {
		try {
			if (jsonThread != null) {
				jsonThread.join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onUpdateProject() {
		FFileManager ffm = new FFileManager();
		collManager.getProjectsJSONObject(sessionID);
		ffm.writeToFile(CollabtiveProfile.KUMA_FILE_JSON_PROJECT, collManager
				.getProjectsJSONArray().toString());
	}

	private void onUpdateExit() {
		onUpdateProject();
		Intent startNewProjectList = new Intent(this, ProjectListActivity.class);
		startActivity(startNewProjectList);
		EditProject.this.finish();
		jsonThreadDismiss();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			jsonThreadDismiss();
			finish();
			Intent returnProjectList = new Intent(this,
					ProjectListActivity.class);
			startActivity(returnProjectList);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.project_edit_menu, menu);
		MenuItem miDone = menu.findItem(R.id.done_edit);
		miDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onEditProjectLoading();
				return false;
			}
		});
		
		MenuItem miClear = menu.findItem(R.id.clear_edit);
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

