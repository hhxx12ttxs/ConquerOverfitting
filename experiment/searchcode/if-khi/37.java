package thesis.travelspeakbook;

import java.util.HashMap;

import model.Category;
import model.Item;
import thesis.code.ParseStatistic;
import thesis.travelspeakbook.current_data.Constant;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class PrepareActivity extends Activity {
	class PrepareDatabaseAsyncTask extends AsyncTask<Context, Void, Integer> {
		private static final String TAG = "PrepareDatase";

		

		
		protected Integer doInBackground(Context... params) {
			// ParseStatistic.prepare_android(params[0], R.raw.database);
			Log.d(TAG, "prepare database in background");
			ParseStatistic.setmContext(getApplicationContext());
			// check whether file on internal memory have exist or not
			ParseStatistic.createDatabaseOnInternalMemory();
			// open from internal memory to install data onto memory 
			Constant.DATABASE = ParseStatistic.getCateFromInternalMem();
			
			if (Constant.DATABASE == null)
				return null;
			
			Constant.CATE_FAVORITE = Constant.DATABASE.getmFavoriteCate();
			Constant.CURRENT_ROOT_CATE = Constant.DATABASE.getmRootCate();
			
			// setUp icon resource
			Constant.RES_ID_BIG_ICON_CATE = new HashMap<Category, Integer>();
			Constant.RES_ID_MIDDLE_ICON_CATE = new HashMap<Category, Integer>();
			Constant.RES_ID_SMALL_ICON_CATE = new HashMap<Category, Integer>();
			Constant.RES_ID_SIDEBAR_CATE = new HashMap<Category, Integer>();
			setUpIconResource(Constant.RES_ID_BIG_ICON_CATE, Constant.resIDBigIconCategory);
			setUpIconResource(Constant.RES_ID_MIDDLE_ICON_CATE, Constant.resIDMiddleIconCategory);
			setUpIconResource(Constant.RES_ID_SMALL_ICON_CATE, Constant.resIDSmallIconCategory);
			setUpIconResource(Constant.RES_ID_SIDEBAR_CATE, Constant.resIDSideBarIconCategory);
			
			return null;
			
			
		}

		private void setUpIconResource(HashMap<Category, Integer> hm, int[] resources) {
			int i = 1;
			for (Item item : Constant.CURRENT_ROOT_CATE.getmItems()) {
				Category c = (Category) item;
				hm.put(c, resources[i]);
				i++;
			}
			hm.put(Constant.CATE_FAVORITE, resources[0]);
		}


		protected void onPostExecute(Integer result) {
			// showDialog("Downloaded " + result + " bytes");
			if (Constant.DATABASE == null) {
				Toast.makeText(getBaseContext(), "Database have updated! Must unistall app and install again", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			
			Log.d(TAG, "post execute prepare database");
			
			new Handler().postDelayed(new Runnable() {
				public void run() {
					Log.d(TAG, "runAfterPrepare");
					onDonePrepare();
				}
			}, SPLASH_DISPLAY_LENGHT);
			
		}
		
		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
		}
		
	}
	final static int SPLASH_DISPLAY_LENGHT = 0;
	final static int TIME_WAIT_SHOW_CHOSEN_LANGUAGE = 1000;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_slash);
		overridePendingTransition(R.anim.transition_right_to_left,
				R.anim.transition_middle_to_left);
		
		PrepareDatabaseAsyncTask pdat = new PrepareDatabaseAsyncTask();
		pdat.execute(this);
	}
	void onDonePrepare() {
		// khi prepare xong roi thi check xem day la lan dau tien su dung phai khong
		// neu phai thi setup mot so cai roi cho chon lang
		// else thi vao main activity
		
		Intent i = new Intent(PrepareActivity.this, FirstUseActivity.class);
		startActivity(i);
	}
}		

