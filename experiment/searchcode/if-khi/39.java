package thesis.travelspeakbook;

import model.Category;
import model.Item;
import thesis.code.ParseStatistic;
import thesis.travelspeakbook.current_data.Constant;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class FirstUseActivity extends Activity {
	static SharedPreferences.Editor mEditor;
	static SharedPreferences mSharePreference;
	void setupStorage() {
		mSharePreference = getSharedPreferences(Constant.TRAVEL_STORAGE, 0);
		mEditor = mSharePreference.edit();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupStorage();
		
		if (isFirstUse()) {
			saveUsedFirstTime();
//			ParseStatistic.save_database_from_android(Constant.DATABASE);
			Intent i = new Intent(FirstUseActivity.this, ChooseLanguageActivity.class);
			startActivity(i);
			return;
		}
		
		// run xong lan dau
		// if chua chon flag thi cho chon flag rui qua activity
		// if chon flag roi thi qua activity luon
		// truoc khi qua activity thi phai cap nhat cate favorite
		Intent i = new Intent(FirstUseActivity.this, MainActivity.class);
		startActivity(i);
	}
	static boolean isFirstUse() {
		return mSharePreference.getBoolean(Constant.STORE_IS_FIRST_USE, true);
	}
	void saveUsedFirstTime() {
		mEditor.putBoolean(Constant.STORE_IS_FIRST_USE, false);
		mEditor.commit();
	}
	void setUpCategoryAll() {
		Category all = Constant.CURRENT_ROOT_CATE;
		Constant.CURRENT_ROOT_CATE.getmItems().add(1, all);
	}
}

