package com.mci.fof.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.mci.fof.dto.UserDTO;
import com.mci.fof.util.Constants;
import com.mci.fof.util.DataUtils;

public class RegisterActivity extends Activity {

	ProgressDialog mDialog;
	boolean isValid = true;
	AlertDialog.Builder ab;
	
	// progress dialog handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	switch (msg.what) {
			case 10:
				mDialog.dismiss();
				break;
			case 1:
				ab.show();
				break;
			default:
				break;
			}
        }
    };
    
	final String items[] = {"Dr.", "Mr.", "Mrs.", "Ms."};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        ImageButton btn_home = (ImageButton) findViewById(R.id.btn_home);
        btn_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
	            RegisterActivity.this.finish();
			}
		});
        
        final EditText salutation = (EditText) findViewById(R.id.txt_salutation);
        final EditText fullname = (EditText) findViewById(R.id.txt_fullname);
        final EditText nric = (EditText) findViewById(R.id.txt_nric);
        final EditText mobile = (EditText) findViewById(R.id.txt_mobile);
        final EditText email = (EditText) findViewById(R.id.txt_email);
        final EditText password = (EditText) findViewById(R.id.txt_password);
        final EditText c_password = (EditText) findViewById(R.id.txt_confirm);
        final Button register = (Button) findViewById(R.id.btn_register);
        final Button cancel = (Button) findViewById(R.id.btn_cancel);
        final DataUtils utils = new DataUtils(getApplicationContext());
        
        salutation.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				ab = new AlertDialog.Builder(RegisterActivity.this);
				ab.setTitle("Salutation");
				ab.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int choice) {
						if(choice == 0) {
							salutation.setText("Dr.");
						}
						else if(choice == 1) {
							salutation.setText("Mr.");
						}
						else if(choice == 2) {
							salutation.setText("Mrs.");
						}
						else if(choice == 3) {
							salutation.setText("Ms.");
						}
					}
				});
				ab.show();
			}
		});
        
        Log.i("Data", salutation.getText().toString() + " - " + fullname.getText().toString() + " - " + nric.getText().toString() + " - " + mobile.getText().toString() + " - " + email.getText().toString() + " - " + password.getText().toString() + " - " + c_password.getText().toString());
        
        register.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String msg = validateFields(salutation.getText().toString(), fullname.getText().toString(), nric.getText().toString(), mobile.getText().toString(), email.getText().toString(), password.getText().toString(), c_password.getText().toString());
				if(!isValid) {
					ab = new AlertDialog.Builder(RegisterActivity.this);
					ab.setTitle(Constants.APP_TITLE);
					msg = "Please fill the following fields:\n\n" + msg;
					ab.setMessage(msg);
					ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							// do nothing
						}
					});
					ab.show();
				}
				else {
					mDialog = new ProgressDialog(RegisterActivity.this);
					mDialog.setMessage("Submitting data...");
		        	mDialog.setCancelable(false);
		        	mDialog.show();
		        	new Thread(new Runnable() {
		        		public void run() {
				        	UserDTO user = new UserDTO();
							user.setSalutation(salutation.getText().toString());
							user.setFullname(fullname.getText().toString());
							user.setNric(nric.getText().toString());
							user.setMobile(mobile.getText().toString());
							user.setEmail(email.getText().toString());
							user.setPassword(password.getText().toString());
							JSONObject message = utils.doRegister(user);
							try {
								showAlert(message.getString("msg"), message.getInt("result"));
							} 
							catch (JSONException e) {
								e.printStackTrace();
							}
		    	            handler.sendEmptyMessage(10);
		        		}
		        	}).start();
				}
			}
		});
        
        cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RegisterActivity.this.finish();
			}
		});
    }
    
    private void showAlert(String m, int r) {
    	final int result = r;
    	final String msg = m;
    	new Thread(new Runnable() {
			public void run() {
		    	ab = new AlertDialog.Builder(RegisterActivity.this);
				ab.setTitle(Constants.APP_TITLE);
				ab.setMessage(msg);
				ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						if(result == 1) {
							RegisterActivity.this.finish();
						}
						else {
							// do nothing
						}
					}
				});
				handler.sendEmptyMessage(1);
			}
		}).start();
    }
    
    public String validateFields(String salutation, String fullname, String nric, String mobile, String email, String password, String c_password) {
    	String message = "";
    	
    	if(salutation.equals(null) || salutation.equals("")) {
    		message = "\t- Salutation\n";
    		isValid = false;
    	}
    	
    	if(fullname.equals(null) || fullname.equals("")) {
    		message = message + "\t- Full name\n";
    		isValid = false;
    	}
    	
    	if(nric.equals(null) || nric.equals("")) {
    		message = message + "\t- NRIC\n";
    		isValid = false;
    	}
    	else {
        	if(!isNRICValid(nric)) {
        		message = message + "\t- Invalid NRIC\n";
        		isValid = false;
        	}
    	}
    	
    	if(mobile.equals(null) || mobile.equals("")) {
    		message = message + "\t- Mobile\n";
    		isValid = false;
    	}
    	
    	if(email.equals(null) || email.equals("")) {
    		message = message + "\t- Email\n";
    		isValid = false;
    	}
    	else {
        	if(!isEmailValid(email)) {
        		message = message + "\t- Invalid Email\n";
        		isValid = false;
        	}
    	}
    	
    	if(password.equals(null) || password.equals("")) {
    		message = message + "\t- Password\n";
    		isValid = false;
    	}
    	
    	if(c_password.equals(null) || c_password.equals("")) {
    		message = message + "\t- Confirm Password\n";
    		isValid = false;
    	}
    	
    	if(password.length() < 6) {
    		message = message + "\t- Password must be at least 6 characters\n";
    		isValid = false;
    	}
    	
    	if(!password.equals(c_password)) {
    		message = message + "\t- Passwords did not match\n";
    		isValid = false;
    	}
    	return message;
    }
    
    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.matches();
    }
    
    private boolean isNRICValid(String nric) {
    	boolean valid = true;
    	if(nric.length() != 9) {
    		Log.i("NRIC", "Not equal to 9 characters");
    		valid = false;
    	}
    	if(isNumeric(nric.substring(0, 1))) {
    		Log.i("NRIC", "First character is numeric: [ " + nric.substring(0, 1) + " ]");
    		valid = false;
    	}
    	if(isNumeric(nric.substring(8, 9))) {
    		Log.i("NRIC", "Last character is numeric: [ " + nric.substring(8, 9) + " ]");
    		valid = false;
    	}
    	return valid;
    }
    
    @SuppressWarnings("unused")
	private boolean isNumeric(String str) {
    	try {
    		double d = Double.parseDouble(str);
    	}  
    	catch(NumberFormatException nfe) {  
    		return false;
    	}  
    	return true;  
    }
    
}

