package edu.berkeley.cs160.clairetuna.prog1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class DisplayTipActivity extends Activity {
Intent intent;
String costOfDinner;
TextView tipPercent;
TextView payAmount;
TextView numSplitters;
TextView tipAmount;
int numSplittersInt=1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_tip);
		intent = getIntent();
		costOfDinner = intent.getStringExtra("COST_OF_DINNER_MESSAGE");
		//get rid of $
		costOfDinner = costOfDinner.substring(1, costOfDinner.length());
		Button buttonPlus = (Button)findViewById(R.id.buttonPlus);
		Button buttonMinus = (Button)findViewById(R.id.buttonMinus);
		buttonPlus.setOnClickListener(buttonPlusListener);
		buttonMinus.setOnClickListener(buttonMinusListener);
		numSplitters = (TextView)findViewById(R.id.num_splitters);
		tipPercent = (TextView)findViewById(R.id.tip_percent);
		payAmount = (TextView)findViewById(R.id.pay_amount);
		tipAmount = (TextView)findViewById(R.id.tip_amount);
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		tipPercent.setText("0%");
		payAmount.setText("$"+costOfDinner);
		numSplitters.setText("1");
		tipAmount.setText("$.00");
		
		
	}

	
	View.OnClickListener buttonPlusListener = new View.OnClickListener(){
		public void onClick(View v){
			numSplittersInt ++;
			numSplitters.setText(new Integer(numSplittersInt).toString());
			updateFinalBill();
		}
	};
	
	View.OnClickListener buttonMinusListener = new View.OnClickListener(){
		public void onClick(View v){
			if (numSplittersInt > 1){
			numSplittersInt --;
			numSplitters.setText(new Integer(numSplittersInt).toString());
			updateFinalBill();
			}
			
		}
	};

	
	SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			tipPercent = (TextView)findViewById(R.id.tip_percent);
			tipPercent.setText(new Integer(progress).toString()+"%");
			updateFinalBill();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			//
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// 
			
		}
	};
	
	
	public void updateFinalBill(){

		int tip = Integer.parseInt(tipPercent.getText().toString().substring(0,tipPercent.getText().toString().length()-1 ));

		double adjusted = (tip*.01)+1;

		double finalCost = Double.parseDouble(costOfDinner)*adjusted;
		finalCost = finalCost/Integer.parseInt(numSplitters.getText().toString());
		double difference = finalCost-Double.parseDouble(costOfDinner)/Integer.parseInt(numSplitters.getText().toString());
		String costString = new Double(finalCost).toString();
		String tipString = new Double(difference).toString();

		CharSequence c = ".";
		if (costString.contains(c)){
			int numDecimals = costString.split("\\.")[1].length();
			if (numDecimals > 2){
				costString=costString.substring(0, costString.length()-numDecimals+2);
			}
			if (numDecimals ==1){
				costString=costString + "0";
			}
		}
		
		if (tipString.contains(c)){
			int numDecimals = tipString.split("\\.")[1].length();
			if (numDecimals > 2){
				tipString=tipString.substring(0, tipString.length()-numDecimals+2);
			}
			if (numDecimals ==1){
				tipString=tipString + "0";
			}
		}
		
		
		//put $ back
		costString="$"+costString;
		payAmount.setText(costString);
		tipAmount.setText("$"+tipString);
	}

}

