package com.fsahoy.android10.activities;

import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.fsahoy.android10.model.IModelProfile;
import com.fsahoy.android10.model.ModelManager;
import com.fsahoy.android10.model.profile.Countries;
import com.fsahoy.android10.model.profile.Gender;
import com.fsahoy.android10.model.profile.Image;
import com.fsahoy.android10.model.profile.PositionRank;
import com.fsahoy.android10.model.profile.Profile;
import com.fsahoy.android10.model.profile.ProfileUpdate;
import com.fsahoy.android10.model.profile.RelationshipStatus;
import com.fsahoy.android10.R;

/**
 * This class contains the Activity for showing the profile to the user.
 * 
 * @author Team E
 * @version 1.0
 * @since 2010-05-13
 */
public class ProfileActivity extends FSAhoy_Activity {
	
	/*
	 * It is necessary for the date picker window - maybe later on we can transfer it
	 * to an own class.
	 */
    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID = 0;
    
    private ProgressDialog dialog;
    
    /*
     * Initialization of the relevant variables
     */
	private TextView profileName;
	private TextView profileDateOfBirth;
	private Spinner profileGender;
	private Spinner profileNationality;
	private Spinner profileRelationshipStatus;
	private TextView profileLanguage;
	private TextView profileAboutMe;
	private Spinner profileSailingSince;
	private TextView profileSeaMiles;
	private TextView profileInterestedIn;
	private Spinner profilePosition;
	private TextView profileQualifications;
	private ImageView profileImage;
	private Profile profile;
	
	private final int toYear = 2012;
	private final int fromYear = 1920;

	/**
	 * Called when activity is first created,
	 * 
	 * This method is called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            State of the instance given to the the activities by android
	 *            API.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		/*
		 * Fetching profile data from the model.
		 */
		
		profile = ModelManager.getInstance_ModelProfile()
				.fetchCurrentProfile();
		
		
		/*
		 * Displaying the profil image
		 */
		profileImage = (ImageView) findViewById(R.id.profileImageAvatar);
		if (profile.getImageResource() != null) {
			Image image = ModelManager.getInstance_ModelProfile().retrieveImage();
			byte[] imageBytes = image.getImage();
			if (image != null && imageBytes != null) {
				profileImage.setImageBitmap(BitmapFactory.decodeByteArray(
						ModelManager.getInstance_ModelProfile().retrieveImage()
								.getImage(), 0, ModelManager
								.getInstance_ModelProfile().retrieveImage()
								.getImage().length));
			}
		}
		
		/*
		 * Dumping the profile data into the regarding data fields.
		 */
		
		profileName = (TextView) findViewById(R.id.profileName);
		profileName.setText(profile.getFirstName() + " "
				+ profile.getLastName());
		
		profileDateOfBirth = (TextView) findViewById(R.id.profileDateOfBirth);
		profileDateOfBirth.setText(Integer.toString(profile.getDateOfBirth().getMonth()+1) + "/"
				+ Integer.toString(profile.getDateOfBirth().getDate()+1) + "/"
				+ Integer.toString(profile.getDateOfBirth().getYear()+1900));
		
		/*
		 * Setting up the date picker window
		 */
		
        profileDateOfBirth.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		profileGender = (Spinner) findViewById(R.id.profileGender);
		String[] genders = new String[Gender.values().length];
		for (int i = 0; i < Gender.values().length; i++) {
			genders[i] = Gender.values()[i].getGenderValue();
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, genders);
		profileGender.setAdapter(adapter);
		for (int i = 0; i < Gender.values().length; i++) {
			if (profile.getGender().getGenderValue().equals(Gender.values()[i].getGenderValue())) {
				profileGender.setSelection(i);
			}
		}

		profileNationality = (Spinner) findViewById(R.id.profileNationality);
		String[] nationalities = new String[Countries.values().length];
		for (int i = 0; i < Countries.values().length; i++) {
			nationalities[i] = Countries.values()[i].getCountryValue();
		}
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nationalities);
		profileNationality.setAdapter(adapter2);
		for (int i = 0; i < Countries.values().length; i++) {
			if (profile.getNationality().getCountryValue().equals(Countries.values()[i].getCountryValue())) {
				profileNationality.setSelection(i);
			}
		}

		profileRelationshipStatus = (Spinner) findViewById(R.id.profileRelationshipStatus);
		String[] relationships = new String[RelationshipStatus.values().length];
		for (int i = 0; i < RelationshipStatus.values().length; i++) {
			relationships[i] = RelationshipStatus.values()[i].getRelationshipStatusValue();
		}
		ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, relationships);
		profileRelationshipStatus.setAdapter(adapter3);
		for (int i = 0; i < RelationshipStatus.values().length; i++) {
			if (profile.getRelationshipStatus().getRelationshipStatusValue().equals(RelationshipStatus.values()[i].getRelationshipStatusValue())) {
				profileRelationshipStatus.setSelection(i);
			}
		}

		profileLanguage = (TextView) findViewById(R.id.profileLanguage);
		profileLanguage.setText(profile.getLanguage().toString());

		profileAboutMe = (TextView) findViewById(R.id.profileAboutMe);
		profileAboutMe.setText(profile.getAboutMe().toString());
		
		profileSailingSince = (Spinner) findViewById(R.id.profileSailingSince);
		int j = 0;
		Integer[] sailingSince = new Integer[toYear - fromYear + 1];
		for (int i = fromYear; i <= toYear; i++) {
			sailingSince[j++] = i;
		}
		ArrayAdapter<Integer> adapter4 = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, sailingSince);
		profileSailingSince.setAdapter(adapter4);
		profileSailingSince.setSelection(profile.getSailingSince()-fromYear);
		
		profileSeaMiles = (TextView) findViewById(R.id.profileSeaMiles);
		profileSeaMiles.setText(Integer.toString(profile.getSailingExperience()));
		
		profileInterestedIn = (TextView) findViewById(R.id.profileInterestedIn);
		profileInterestedIn.setText(profile.getInterestedIn());
		
		profilePosition = (Spinner) findViewById(R.id.profilePosition);
		String[] position = new String[PositionRank.values().length];
		for (int i = 0; i < PositionRank.values().length; i++) {
			position[i] =  PositionRank.values()[i].getPositionValue();
		}
		ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, position);
		profilePosition.setAdapter(adapter5);
		for (int i = 0; i < PositionRank.values().length; i++) {
			if (profile.getPosition().getPositionValue().equals(PositionRank.values()[i].getPositionValue())) {
				profilePosition.setSelection(i);
			}
		}
		
		profileQualifications = (TextView) findViewById(R.id.profileQualifications);
		profileQualifications.setText(profile.getQualifications());

        // get the current date (as default for the datePicker Dialog)
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

	}
	
	/**
	 * Updates the profileDateOfBirth controller according to the datePicker
	 * dialog
	 */
    private void updateDisplay() {
        profileDateOfBirth.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mMonth+1).append("/")
                    .append(mDay).append("/")
                    .append(mYear));
    }
    
	/**
	 * Creates a dateSetListener on the datePicker dialog, that handles date
	 * change.
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};
	
	/**
	 * Called, when the datePicker dialog is created.
	 * @param id
	 * @return
	 */
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mDateSetListener,
	                    mYear, mMonth, mDay);
	    }
	    return null;
	}
	
	/**
	 * Updates the user profile according to the changes.
	 * @param view
	 */
	public void updateProfile(View view) {
		//Showing a dialog about the upload progress 
		dialog = ProgressDialog.show(ProfileActivity.this, "Uploading",
				"Please wait...", true);
		
		ProfileUpdate profileUpdate = new ProfileUpdate();
		
		//Updating the attributes from this profile activity
		profileUpdate.setFirstName(profileName.getText().toString().substring(0, profileName.getText().toString().indexOf(" ")));
		profileUpdate.setLastName(profileName.getText().toString().substring(profileName.getText().toString().indexOf(" "), profileName.getText().length()));
		profileUpdate.setDateOfBirth((String) profileDateOfBirth.getText());
		profileUpdate.setGender((String) profileGender.getSelectedItem());
		profileUpdate.setNationality((String) profileNationality.getSelectedItem());
		profileUpdate.setRelationshipStatus((String) profileRelationshipStatus.getSelectedItem());
		profileUpdate.setLanguage(profileLanguage.getText().toString());
		profileUpdate.setAboutMe(profileAboutMe.getText().toString());
		profileUpdate.setSailingSince((Integer) profileSailingSince.getSelectedItem());
		profileUpdate.setSailingExperience(Integer.parseInt(profileSeaMiles.getText().toString()));
		profileUpdate.setInterestedIn(profileInterestedIn.getText().toString());
		profileUpdate.setPosition((String) profilePosition.getSelectedItem());
		profileUpdate.setQualifications(profileQualifications.getText().toString());

		//Populating the update objects with the old attributes.
		profileUpdate.setLocationTravelledCity(profile.getLocationTravelledCity());
		profileUpdate.setLocationTravelledContinent(profile.getLocationTravelledContinent());
		profileUpdate.setLocationTravelledCountry(profile.getLocationTravelledCountry());
		
		//Updating the profile in the local db and on the webservice
		IModelProfile modelProfile = ModelManager.getInstance_ModelProfile();
		modelProfile.updateProfile(profileUpdate);
		modelProfile.refreshProfile();
		
		dialog.dismiss();
	}
	
	/**
	 * If clicked on the profile picture, the ProfileAvatarUpdateActivity is called
	 * @param view
	 */
	public void updateAvatar(View view) {
		startActivity(new Intent(getApplicationContext(), ProfileAvatarUpdateActivity.class));
	}
}
