package net.a103.may.mfa.vote;

import net.a103.may.mfa.vote.api.APIListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CellphoneInputActivity extends AbstractActivity {

	private int[] inputButtonIds = new int[] { R.id.button0, R.id.button1,
			R.id.button2, R.id.button3, R.id.button4, R.id.button5,
			R.id.button6, R.id.button7, R.id.button8, R.id.button9};
	
	private TextView input;
	private Button del;
	private Button enter;
	
	private static String PASS = "1590862473";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cellphone);
				
		input = (TextView) findViewById(R.id.cellphone_code);
		
		for(int i = 0; i < inputButtonIds.length; i++) {
			final int num = i;
			Button button = (Button) findViewById(inputButtonIds[i]);
			
			button.setWidth(convertToPixel(100));
			button.setHeight(convertToPixel(100));
			button.setTextSize(convertToPixel(70));
			button.setText(i + "");
			
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addNumber(num);
				}				
			});
		}
		
		del = (Button) findViewById(R.id.button_del);
		del.setWidth(convertToPixel(100));
		
		enter = (Button) findViewById(R.id.button_enter);
		enter.setWidth(convertToPixel(100));

		changeButtonState(" ");
	}
	
	public void delete(View v) {
		String s = input.getText().toString();
		String code = s.length() <= 1 ? " " : s.substring(0, s.length() - 1);
		input.setText(code);
		changeButtonState(code);
	}
	
	public void enter(View v) {
		final String code = input.getText().toString();
		
		if(code.length() != 10) {
			Toast.makeText(this, "コードは10桁です。", Toast.LENGTH_SHORT).show();
			return;
		}
		
		showProgressDialog("コードを確認しています...");
		
		if("0000000000".equals(code) || (!PASS.equals(code) && !validate(code.toCharArray()))) {
			final Handler handler = new Handler();
			final Runnable func = new Runnable(){
				@Override
				public void run() {
					removeDialogIfNeeded(PROGRESS_DIALOG);
					Toast.makeText(CellphoneInputActivity.this, "不正なコードです。", Toast.LENGTH_SHORT).show();
				}	
			};
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						//timeout
						Thread.sleep((long) (300 + Math.random() * 1500));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					handler.post(func);
				}		
			}).start();
			
			return;
		}
		
		if(PASS.equals(code)){
			Intent intent = new Intent(this, DebugActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		MFAApplication app = (MFAApplication) getApplicationContext();
		
		app.sendPhoneCode(code, new APIListener() {

			@Override
			public void onSuccess() {
				removeDialogIfNeeded(PROGRESS_DIALOG);
				send(code);
			}

			@Override
			public void onError(int code) {	
				Toast.makeText(CellphoneInputActivity.this, "コード1つにつき1回しか投票できません。", Toast.LENGTH_SHORT).show();
				removeDialogIfNeeded(PROGRESS_DIALOG);
			}
		});
	}
	
	private void send(String code) {
		if(PASS.equals(code)){
			Intent intent = new Intent(this, DebugActivity.class);
			startActivity(intent);
			finish();
		}else {
			Intent intent = new Intent(this, EnqueteActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	private static final int[] VALUE_INDEX = new int[]{
		0, 1, 2, 4, 5, 6, 8
	};
	
	private static final int[][] PARITY = new int[][]{
		new int[]{3, 13}, new int[]{7, 29}, new int[]{9, 101}
	};
	
	private void setNumberButtonEnabled(boolean enabled) {
		for(int i = 0; i < inputButtonIds.length; i++) {
			Button button = (Button) findViewById(inputButtonIds[i]);		
			button.setEnabled(enabled);
		}
	}
	
	private void changeButtonState(String code) {
		switch(code.length()){
		case 10:
			setNumberButtonEnabled(false);
			del.setEnabled(true);
			enter.setEnabled(true);
			break;
		case 1:
			if(" ".equals(code)) {
				setNumberButtonEnabled(true);
				del.setEnabled(false);
				enter.setEnabled(false);
				break;
			}
		default:
			setNumberButtonEnabled(true);
			del.setEnabled(true);
			enter.setEnabled(false);
		}
	}
	
	private int getValue(char[] code) {
		int value = 0;
		
		for(int i = 0; i < VALUE_INDEX.length; i++) {
			value = value * 10 +  code[VALUE_INDEX[i]] - '0';
		}
		
		return value;
	}
	
	private boolean validate(char[] code) {
		int value = getValue(code);
		
		for(int[] parity : PARITY) {
			int data = code[parity[0]] - '0';
			if((value % parity[1]) % 10 != data) {
				return false;
			}
		}
		
		return true;
	}
	
	private void addNumber(int n) {
		String text = input.getText().toString();
		String target;
		
		if(!text.matches("\\d+")) {
			target = n + "";
		}else if(text.length() == 10) {
			Toast.makeText(this, "コードは10桁です", Toast.LENGTH_SHORT).show();
			return;
		}else {
			target = text + n;
		}
		
		input.setText(target);
		
		changeButtonState(target);
	}
	
	private int convertToPixel(float dp) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		return (int) (metrics.density * dp + 0.5f);
	}
	
	public void back(View v) {
		finish();
	}
}

