package thesis.travelspeakbook;

import model.Item.Language;
import thesis.travelspeakbook.current_data.Constant;
import thesis.travelspeakbook.fragment.ChooseFlagFragment;
import thesis.travelspeakbook.fragment.ChooseFlagFragment.OnFlagSelectedListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class ChooseLanguageActivity extends FragmentActivity implements OnFlagSelectedListener {
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
		setContentView(R.layout.page_first_use);
	}
	static int [] resIDFragment = {R.id.fragment_choose_lang_input,
		R.id.fragment_choose_lang_translate};
	
	Language mLangInput = null, mLangOutput = null;
	
	public void onFlagSelected(int idFlagSelected, int idFragment) {
		int resIDFragment = 0;
		// update another fragment
		if (idFragment == R.id.fragment_choose_lang_input) {
			mLangInput = Language.values()[idFlagSelected];
			
			
			// update fragment kia
			// chi show fragment khi check co 2 flag roi
			resIDFragment = R.id.fragment_choose_lang_translate;
			
		}
		else {
			mLangOutput = Language.values()[idFlagSelected];
			resIDFragment = R.id.fragment_choose_lang_input;
		}
		// check if have already 2 flags, then set and move to MainActivity
		if (mLangInput!=null && mLangOutput!= null) {
			Constant.setLangMode(mLangInput, mLangOutput);
			Intent i = new Intent(ChooseLanguageActivity.this, MainActivity.class);
			startActivity(i);
			return;
		}
		Fragment f = new ChooseFlagFragment(idFlagSelected);
		Constant.showFragment(getSupportFragmentManager(), f, resIDFragment, false);
	}
}


