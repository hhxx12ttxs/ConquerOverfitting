package com.nelson.jarvisclientics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainLand extends Activity implements TextToSpeech.OnInitListener {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private int MY_DATA_CHECK_CODE = 0;
	private TextToSpeech tts;

	TextView commandTV, responseTV;
	ArrayList<String> notes;

	Socket socket = null;
	DataOutputStream dataOutputStream = null;
	DataInputStream dataInputStream = null;
	String responseText = "";

	TextView batteryInfo, batLvl, batTemp, batV;
	//the statistics of the SD card  
	private StatFs stats;  
	//the state of the external storage  
	private String externalStorageState;  
	//the total size of the SD card  
	private double totalSize;  
	//the available free space  
	private double freeSpace;  
	//a String to store the SD card information  
	private String outputInfo;  
	//a TextView to output the SD card state  
	private TextView sd_state;  
	//a TextView to output the SD card information  
	private TextView sd_info;  
	//set the number format output  
	private NumberFormat numberFormat;

	TextView weatherText;
	ImageButton weatherIcon;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Intent intent = new Intent(this, MainPort.class);
			finish();
			startActivity(intent);
		}
		setContentView(R.layout.mainland);

		if(isNetworkAvailable()) { new DownloadFilesTask().execute("weather" , null, null); }
		weatherText = (TextView)findViewById(R.id.weatherText);
		weatherIcon = (ImageButton)findViewById(R.id.weather);

		Intent intent = getIntent();
		notes = new ArrayList<String>();
		notes = intent.getExtras().getStringArrayList("Notes"); //Gets the ArrayList of all notes

		commandTV = (TextView)findViewById(R.id.commandTV);
		responseTV = (TextView)findViewById(R.id.textView2);

		ImageButton notesButton = (ImageButton)findViewById(R.id.notes);
		ImageButton smsButton = (ImageButton)findViewById(R.id.sms);
		ImageButton arc = (ImageButton)findViewById(R.id.arc);

		arc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});

		arc.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				final Dialog commandDialog = new Dialog(MainLand.this); 
				commandDialog.setContentView(R.layout.commanddialog); //sets the layout
				commandDialog.setTitle("Enter a command"); //sets the title
				commandDialog.setCancelable(true); //allows the user to exit the dialog
				final EditText entercommand = (EditText)commandDialog.findViewById(R.id.entercommand);
				final ImageButton go = (ImageButton)commandDialog.findViewById(R.id.go);
				go.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						commandTV.setText("Command: " + entercommand.getText());
						commandDialog.hide();
					}
				});
				commandDialog.show();
				return false;
			}
		});

		TextView availRAM = (TextView)findViewById(R.id.availRAM);
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		availRAM.setTextColor(Color.GREEN);  
		availRAM.setText("PhysMem " + (mi.availMem / 1048576L) + "MB");

		sd_state = (TextView)findViewById(R.id.sd_state); 
		sd_info = (TextView)findViewById(R.id.sd_info); 

		//get external storage (SD card) state  
		externalStorageState = Environment.getExternalStorageState(); 

		//checks if the SD card is attached to the Android device  
		if(externalStorageState.equals(Environment.MEDIA_MOUNTED)  
				|| externalStorageState.equals(Environment.MEDIA_UNMOUNTED)  
				|| externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) { 
			//obtain the stats from the root of the SD card.  
			stats = new StatFs(Environment.getExternalStorageDirectory().getPath());  

			//Add 'Total Size' to the output string:  
			outputInfo = "Total Size:\n";  

			//total usable size  
			totalSize = stats.getBlockCount() * stats.getBlockSize();  

			//initialize the NumberFormat object  
			numberFormat = NumberFormat.getInstance();  
			//disable grouping  
			numberFormat.setGroupingUsed(false);  
			//display numbers with two decimal places  
			numberFormat.setMaximumFractionDigits(2);   

			//Output the SD card's total size in gigabytes, megabytes, kilobytes and bytes  
			outputInfo += numberFormat.format((totalSize / (double)1073741824)) + " GB\n"  
					+ numberFormat.format((totalSize / (double)1048576)) + " MB\n";   

			//Add 'Remaining Space' to the output string:  
			outputInfo += "\nRemaining Space:";  

			//available free space  
			freeSpace = stats.getAvailableBlocks() * stats.getBlockSize();  

			//Output the SD card's available free space in gigabytes, megabytes, kilobytes and bytes  
			outputInfo += numberFormat.format((freeSpace / (double)1073741824)) + " GB\n"  
					+ numberFormat.format((freeSpace / (double)1048576)) + " MB";   

			//output the SD card state  
			sd_state.setTextColor(Color.GREEN);  
			sd_state.setText("SD card is " + externalStorageState +".");  

			//output the SD card info  
			sd_info.setText(outputInfo);  
		}  
		else //external storage not found  
		{  
			//output the SD card state  
			sd_state.setTextColor(Color.RED);  
			sd_state.setText("SD card state is \"" + externalStorageState + "\".");  
		}

		TextView netInfo = (TextView)findViewById(R.id.netInfo);
		String connection = "";
		final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(wifi.isAvailable() )
			connection = "WiFi";
		if(mobile.isAvailable())
			connection += " + 4G";
		if(!wifi.isAvailable() & !mobile.isAvailable()) 
			connection = "No Network Connection";

		netInfo.setTextColor(Color.GREEN);  
		netInfo.setText(connection);


		BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
			int level = -1;
			int voltage = -1;
			int temp = -1;
			public void onReceive(Context context, Intent intent) {
				batLvl = (TextView)findViewById(R.id.batLvl);
				batTemp = (TextView)findViewById(R.id.batTemp);
				batV = (TextView)findViewById(R.id.batV);

				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
				String temperature = Integer.toString(temp);
				String volts = Integer.toString(voltage);
				batLvl.setText("-" + level+"%");
				batTemp.setText("-" + temperature.substring(0, temperature.length()-1)+"C");
				batV.setText("-" + volts.charAt(0) + "." + volts.substring(1, volts.length()) + "V");
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, filter);

		batteryInfo = (TextView)findViewById(R.id.battery);
		batteryInfo.setTextColor(Color.GREEN);
		batteryInfo.setText("BatStats");

		TextView date = (TextView)findViewById(R.id.date);
		TextView day = (TextView)findViewById(R.id.day);
		TextView year = (TextView)findViewById(R.id.year);
		Calendar c = Calendar.getInstance(); 
		int theDay = c.get(Calendar.DAY_OF_WEEK);
		String weekday="";
		if (theDay == 1) { weekday = "Sunday"; }
		else if(theDay == 2) { weekday = "Monday"; }
		else if(theDay == 3) { weekday = "Tuesday"; }
		else if(theDay == 4) { weekday = "Wednesday"; }
		else if(theDay == 5) { weekday = "Thursday"; }
		else if(theDay == 6) { weekday = "Friday"; }
		else if(theDay == 7) { weekday = "Saturday"; }
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		String monthDay = Integer.toString(today.monthDay);
		if(monthDay.length() == 1) {monthDay = "0" + monthDay;}
		date.setText(monthDay);

		day.setText(weekday);
		String yr = Integer.toString(today.year);
		year.setText(yr.charAt(0) + "\n" + yr.charAt(1) + "\n" + yr.charAt(2) + "\n" + yr.charAt(3));

		notesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Notes.class);
				intent.putExtra("Notes", notes);
				finish();
				startActivity(intent);
			}
		});

		smsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), SMS.class);
				finish();
				startActivity(intent);
			}
		});

		ImageButton passwordButton = (ImageButton)findViewById(R.id.password);
		passwordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), PasswordManager.class);
				finish();
				startActivity(intent);
			}
		});

		ImageButton musicButton = (ImageButton)findViewById(R.id.music);
		musicButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Music.class);
				finish();
				startActivity(intent);
			}
		});

	}



	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			commandTV.setText("Command: " + matches.get(0)); //What is recognized by the speech input
		}
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				tts = new TextToSpeech(getApplicationContext(), this);
			}
			else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	/**
	 * Checks to see if an Internet connection is available.
	 * @return true if there is a connection
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	/**
	 * After the command is sent to the server, the server sends back a response.
	 * This method figures out what to do with it. 
	 * Just prints it to the response textview
	 * @param responseText
	 */
	private void responseHandler() {
		runOnUiThread(new Runnable() {
			public void run() {
				weatherText.setText(responseText);
				if(responseText.contains("and Thunderstorms")) {
					Bitmap thunder = BitmapFactory.decodeResource(getResources(), R.drawable.thunder);
					weatherIcon.setImageBitmap(thunder);
				}
				if(responseText.contains("and Cloud") || responseText.contains("and Mostly Cloudy")) {
					Bitmap cloudy = BitmapFactory.decodeResource(getResources(), R.drawable.cloudy);
					weatherIcon.setImageBitmap(cloudy);
				}
				if(responseText.contains("and Fair") || responseText.contains("and Sun")) {
					Bitmap sunny = BitmapFactory.decodeResource(getResources(), R.drawable.sunny);
					weatherIcon.setImageBitmap(sunny);
				}
				if(responseText.contains("and Snow")) {
					Bitmap snow = BitmapFactory.decodeResource(getResources(), R.drawable.snow);
					weatherIcon.setImageBitmap(snow);
				}
				if(responseText.contains("Rain") || responseText.contains("Light Rain")) {
					Bitmap rain = BitmapFactory.decodeResource(getResources(), R.drawable.rain);
					weatherIcon.setImageBitmap(rain);
				}
			}
		});
	}


	/**
	 * SENDS COMMAND TO SERVER
	 */
	protected void sendCommand(String command) {
		try {
			socket = new Socket("192.168.0.105", 8756);
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream.writeUTF(command);
			responseText = dataInputStream.readUTF();
			responseHandler();
		} catch (UnknownHostException e) {
			Toast.makeText(this, "Unknown Host Exception", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		finally{
			if (socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
			if (dataOutputStream != null){
				try {
					dataOutputStream.close();
				} catch (IOException e) {
					Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
			if (dataInputStream != null){
				try {
					dataInputStream.close();
				} catch (IOException e) {
					Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Class for handling NetworkOnMainThread
	 * Sends the command Asynchronously 
	 * @author austinn
	 *
	 */
	private class DownloadFilesTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... command) {
			sendCommand(command[0]); //sends the command to the server!
			return null;
		}

		protected void onProgressUpdate(Void... progress) {}
		protected void onPostExecute(String result) {}
	}


	public void onInit(int status) {       
		if (status == TextToSpeech.SUCCESS) {

		}
		else if (status == TextToSpeech.ERROR) {
			Toast.makeText(MainLand.this,
					"Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
		}
	}

}
