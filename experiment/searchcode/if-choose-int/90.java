package com.example.shokudochooser;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.example.shokudochooser.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Button mStartButton;
	private View mAlpha;
	private View mBravo;
	private View mCharlie;
	private View mDelta;
	private TextView mAOdds;
	private TextView mBOdds;
	private TextView mCOdds;
	private TextView mDOdds;
	private int mColorNotChoose;
	private Random mRandom = new Random();
	private int MAX;
	private Map<String, Integer> mOdds = new HashMap<String, Integer>();

	private static final int ALPHA = 0;
	private static final int BRAVO = 1;
	private static final int CHARLIE = 2;
	private static final int DELTA = 3;
	private static final String PREF_KEY = "unko";
	private int mLastChoose = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		setOdds();
	}

	private void setOdds() {
		int lastChoose = loadLastChoose();
		switch (lastChoose) {
		case ALPHA:
			setOdds(0, 1, 1, 1);
			break;
		case BRAVO:
			setOdds(1, 0, 1, 1);
			break;
		case CHARLIE:
			setOdds(1, 1, 0, 1);
			break;
		case DELTA:
			setOdds(1, 1, 1, 0);
			break;
		default:
			setOdds(1, 1, 1, 1);
			break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_reset:
			initColor();
			setOdds();
			break;
		case R.id.menu_save_result:
			saveLastChoose(mLastChoose);
			break;

		case R.id.menu_clear_result:
			// TODO
			saveLastChoose(-1);
			break;
		default:
			// nop

		}
		return super.onOptionsItemSelected(item);
	}

	private void setChoose(int choose) {
		mAlpha.setBackgroundColor(mColorNotChoose);
		mBravo.setBackgroundColor(mColorNotChoose);
		mCharlie.setBackgroundColor(mColorNotChoose);
		mDelta.setBackgroundColor(mColorNotChoose);

		int aOdds = mOdds.get("aOdds");
		int bOdds = mOdds.get("bOdds");
		int cOdds = mOdds.get("cOdds");
		int dOdds = mOdds.get("dOdds");

		if (choose <= aOdds) {
			mAlpha.setBackgroundColor(getResources().getColor(
					R.color.choosing_alpha));
			mLastChoose = ALPHA;
		} else if ((aOdds < choose) && (choose <= (bOdds + aOdds))) {
			mBravo.setBackgroundColor(getResources().getColor(
					R.color.choosing_bravo));
			mLastChoose = BRAVO;
		} else if (((bOdds + aOdds) < choose)
				&& (choose <= (cOdds + bOdds + aOdds))) {
			mCharlie.setBackgroundColor(getResources().getColor(
					R.color.choosing_charlie));
			mLastChoose = CHARLIE;
		} else if ((cOdds + bOdds + aOdds) < choose) {
			mDelta.setBackgroundColor(getResources().getColor(
					R.color.choosing_delta));
			mLastChoose = DELTA;
		}

	}

	private void initView() {
		// View
		mAlpha = findViewById(R.id.alpha);
		mBravo = findViewById(R.id.bravo);
		mCharlie = findViewById(R.id.charlie);
		mDelta = findViewById(R.id.delta);
		mColorNotChoose = getResources().getColor(R.color.not_choosing);

		// odds
		mAOdds = (TextView) findViewById(R.id.a_odds);
		mBOdds = (TextView) findViewById(R.id.b_odds);
		mCOdds = (TextView) findViewById(R.id.c_odds);
		mDOdds = (TextView) findViewById(R.id.d_odds);

		// ChooseBotton
		mStartButton = (Button) findViewById(R.id.button1);
		mStartButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setChoose(mRandom.nextInt(MAX));
			}
		});
	}

	private void initColor() {
		mAlpha.setBackgroundColor(getResources().getColor(
				R.color.choosing_alpha));
		mBravo.setBackgroundColor(getResources().getColor(
				R.color.choosing_bravo));
		mCharlie.setBackgroundColor(getResources().getColor(
				R.color.choosing_charlie));
		mDelta.setBackgroundColor(getResources().getColor(
				R.color.choosing_delta));
	}

	private void setOdds(int aRatio, int bRatio, int cRatio, int dRatio) {
		MAX = (aRatio + bRatio + cRatio + dRatio) * 100;
		mOdds.put("aOdds", aRatio * 100);
		mOdds.put("bOdds", bRatio * 100);
		mOdds.put("cOdds", cRatio * 100);
		mOdds.put("dOdds", dRatio * 100);
		int aOdds = aRatio * 100 * 100 / MAX;
		int bOdds = bRatio * 100 * 100 / MAX;
		int cOdds = cRatio * 100 * 100 / MAX;
		int dOdds = dRatio * 100 * 100 / MAX;
		mAOdds.setText(aOdds + "%");
		mBOdds.setText(bOdds + "%");
		mCOdds.setText(cOdds + "%");
		mDOdds.setText(dOdds + "%");
	}

	private void saveLastChoose(int lastChoose) {
		Log.v(TAG, "save:" + mLastChoose);
		// ??
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.edit().putInt(PREF_KEY, lastChoose).commit();
	}

	private int loadLastChoose() {
		// ????
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Log.v(TAG, "load:" + sp.getInt(PREF_KEY, -1));
		return sp.getInt(PREF_KEY, -1);
	}

}

