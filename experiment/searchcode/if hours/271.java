package com.example.student_employment;

import android.support.v7.app.ActionBarActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Monday extends ActionBarActivity {
private CheckBox eighta, 
eightthirtya,ninea, ninethirtya, tena, tenthirtya, elevena, eleventhirtya, twelvea, 
onep, onethirtyp, twop, twothirtyp, threep, threethirtyp, fourp, fourthirtyp, fivep, fivethirtyp,
sixp, sixthirtyp, sevenp, seventhirtyp, eightp, eightthirtyp, 
ninep, ninethirtyp, tenp, tenthirtyp, elevenp, eleventhirtyp, twelvep, twelvethirtyp;
private Button btnDisplay;
public final static String BusyHours ="busyHours";
//public final static String hours = "0000000000000000";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monday);
		//btnDisplay = (Button)findViewById(R.id.btnDisplay);
		addListenerOnButton();
	}
	
public void addListenerOnButton() {
	eighta = (CheckBox) findViewById(R.id.eighta);
	eightthirtya = (CheckBox) findViewById(R.id.eightthirtya);
	ninea = (CheckBox) findViewById(R.id.ninea);
	ninethirtya = (CheckBox) findViewById(R.id.ninethirtya);
	tena = (CheckBox) findViewById(R.id.tena);
	tenthirtya = (CheckBox) findViewById(R.id.tenthirtya);
	elevena = (CheckBox) findViewById(R.id.elevena);
	eleventhirtya = (CheckBox) findViewById(R.id.eleventhirtya);
	twelvea = (CheckBox) findViewById(R.id.twelvea);
	
	onep = (CheckBox) findViewById(R.id.onep);
	onethirtyp = (CheckBox) findViewById(R.id.onethirtyp);
	twop = (CheckBox) findViewById(R.id.twop);
	twothirtyp = (CheckBox) findViewById(R.id.twothirtyp);
	threep = (CheckBox) findViewById(R.id.threep);
	threethirtyp = (CheckBox) findViewById(R.id.threethirtyp);
	fourp = (CheckBox) findViewById(R.id.fourp);
	fourthirtyp = (CheckBox) findViewById(R.id.fourthirtyp);
	fivep = (CheckBox) findViewById(R.id.fivep);
	fivethirtyp = (CheckBox) findViewById(R.id.fivethirtyp);
	sixp = (CheckBox) findViewById(R.id.sixp);
	sixthirtyp = (CheckBox) findViewById(R.id.sixthirtyp);
	sevenp = (CheckBox) findViewById(R.id.sevenp);
	seventhirtyp = (CheckBox) findViewById(R.id.seventhirtyp);
	eightp = (CheckBox) findViewById(R.id.eightp);
	eightthirtyp = (CheckBox) findViewById(R.id.eightthirtyp);
	ninep = (CheckBox) findViewById(R.id.ninep);
	ninethirtyp = (CheckBox) findViewById(R.id.ninethirtyp);
	tenp = (CheckBox) findViewById(R.id.tenp);
	tenthirtyp = (CheckBox) findViewById(R.id.tenthirtyp);
	elevenp = (CheckBox) findViewById(R.id.elevenp);
	eleventhirtyp = (CheckBox) findViewById(R.id.eleventhirtyp);
	twelvep = (CheckBox) findViewById(R.id.twelvep);
	twelvethirtyp = (CheckBox) findViewById(R.id.twelvethirtyp);
	
	btnDisplay = (Button) findViewById(R.id.btnDisplay);
 
	btnDisplay.setOnClickListener(new OnClickListener() {
 
          //Run when button is clicked
	  @Override
	  public void onClick(View v) {
 
		StringBuffer result = new StringBuffer();
		result.append("\n8am : ").append(eighta.isChecked());
		result.append("\n8:30am : ").append(eightthirtya.isChecked());
		result.append("\n9am : ").append(ninea.isChecked());
		result.append("\n9:30am : ").append(ninethirtya.isChecked());
		result.append("\n10am : ").append(tena.isChecked());
		result.append("\n10:30am : ").append(tenthirtya.isChecked());
		result.append("\n11am : ").append(elevena.isChecked());
		result.append("\n11:30am : ").append(eleventhirtya.isChecked());
		result.append("\n12pm : ").append(twelvep.isChecked());
		result.append("\n12:30pm : ").append(twelvethirtyp.isChecked());
		result.append("\n1pm : ").append(onep.isChecked());
		result.append("\n1:30pm : ").append(onethirtyp.isChecked());
		result.append("\n2pm : ").append(twop.isChecked());
		result.append("\n2:30pm : ").append(twothirtyp.isChecked());
		result.append("\n3pm : ").append(threep.isChecked());
		result.append("\n3:30pm : ").append(threethirtyp.isChecked());
		result.append("\n4pm : ").append(fourp.isChecked());
		result.append("\n4:30pm : ").append(fourthirtyp.isChecked());
		result.append("\n5pm : ").append(fivep.isChecked());
		result.append("\n5:30pm : ").append(fivethirtyp.isChecked());
		result.append("\n6pm : ").append(sixp.isChecked());
		result.append("\n6:30pm : ").append(sixthirtyp.isChecked());
		result.append("\n7pm : ").append(sevenp.isChecked());
		result.append("\n7:30pm : ").append(seventhirtyp.isChecked());
		result.append("\n8pm : ").append(eightp.isChecked());
		result.append("\n8:30pm : ").append(eightthirtyp.isChecked());
		result.append("\n9pm : ").append(ninep.isChecked());
		result.append("\n9:30pm : ").append(ninethirtyp.isChecked());
		result.append("\n10pm : ").append(tenp.isChecked());
		result.append("\n1:30pm : ").append(tenthirtyp.isChecked());
		result.append("\n11pm : ").append(elevenp.isChecked());
		result.append("\n11:30pm : ").append(eleventhirtyp.isChecked());
		result.append("\n12am : ").append(twelvea.isChecked());
 
		Toast.makeText(Monday.this, result.toString(),
				Toast.LENGTH_LONG).show();
		busyHours();
		Intent intent = new Intent(Monday.this, Tuesday.class);
		String message = busyHours();
		intent.putExtra(BusyHours, message);
		startActivity(intent);
		//Intent i=new Intent(Monday.this, Tuesday.class);
        //startActivity(i);
	  }
	  
	  
	});
 
  }
//checks status of each checkbox
public String busyHours(){
	String hours = "0000000000000000";
	if (eighta.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (eightthirtya.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (ninea.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (ninethirtya.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (tena.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (tenthirtya.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (elevena.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (eleventhirtya.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (twelvea.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (onep.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (onethirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (twop.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (twothirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (threep.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (threethirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (fourp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (fourthirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (fivep.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (fivethirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (sixp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (sixthirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (sevenp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (seventhirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (eightp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (eightthirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (ninep.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (ninethirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (tenp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (tenthirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (elevenp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (eleventhirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (twelvep.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	if (twelvethirtyp.isChecked()){
		hours = hours + "1";
	}
	else{
		hours = hours +"0";
	}
	
	return hours;
}
//goes to next page and keeps BusyHours 

@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
        return true;
    }
    return super.onOptionsItemSelected(item);
}
	
}

