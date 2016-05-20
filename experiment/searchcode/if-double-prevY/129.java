package com.example.mackinpad;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class TouchActivity extends Activity implements
OnGesturePerformedListener, OnDrawerOpenListener, OnDrawerCloseListener, SensorEventListener {

	private GestureLibrary mLibrary;
	GestureOverlayView gestureOverlayview;

	View numPad;

	PointF prevP1 = null, prevP2 = null, prevP3 = null, prevP4 = null,
			prevP5 = null;
	PointF p1 = null, p2 = null, p3 = null, p4 = null, p5 = null;
	PointF defp1 = null, defp2 = null, defp3 = null, defp4 = null, defp5 = null;

	//TextView modetext = null;

	GestureDetector mGestureDetector;

	final static String TAG = "MacKinPad";

	final static int SENSOR_ON = 1;
	final static int SENSOR_OFF = 2;
	int SENSOR_MODE = SENSOR_OFF;

	final static int MIN_MOUSEMOVE_VALUE = 10;
	final static int ULMIN_TOUCHMOVE_VALUE = 50;
	final static int DRMIN_TOUCHMOVE_VALUE = -50;

/////
	final static int NONE = 20;
	final static int READY_SINGLE = 21;
	final static int READY_MULTI = 22;
	final static int MOUSE = 23;
	final static int SWIPE_LEFT = 24;
	final static int SWIPE_RIGHT = 25;
	final static int SWIPE_UP = 26;
	final static int SWIPE_DOWN = 27;
	final static int HSCROLL = 28;
	final static int VSCROLL = 29;
	final static int PINCH_REDUCT = 30;
	final static int PINCH_EXPAND = 31;
	final static int TRI_SWIPE_LEFT = 32;
	final static int TRI_SWIPE_RIGHT = 33;
	final static int TRI_SWIPE_UP = 34;
	final static int TRI_SWIPE_DOWN = 35;
	final static int TRI_HSCROLL = 36;
	final static int TRI_VSCROLL = 37;
	final static int TRI_FIND_WORD = 38;
	final static int DRAG = 39;
	final static int RCLICK = 40;
	final static int SCREENSHOT = 41;

	final static int TOP = 42;
	final static int MID = 43;
	final static int BOTTOM = 44;

	final static int QUAD = 45;
	final static int QUAD_EXPAND = 46;
	final static int QUAD_REDUCT = 47;

	final static int QUAD_BACKGROUND = 48;
	final static int QUAD_NORMAL = 49;
	final static int QUAD_APPLIST = 50;

	// final static int ALL_POINTER_UP = 51;//prevent all move state. while
	// other pointer release
	static boolean TIME_CHECK = false;
	static boolean DB_CHECK = false;


	int MODE = NONE;
	int SCROLLMODE = NONE;
	int PREMODE = NONE;
	int STATE = MID;
	int QUADMODE = QUAD_NORMAL;

	final static int TWO = 2;
	final static int THREE = 3;
	final static int FOUR = 4;
	final static int FIVE = 5;
	
	final static double CURSOR_SPEED_DEFAULT = 1;
	final static double CURSOR_SPEED_1 = 1.1;
	final static double CURSOR_SPEED_2 = 1.2;
	final static double CURSOR_SPEED_3 = 1.3;
	final static double CURSOR_SPEED_4 = 1.4;
	final static double CURSOR_SPEED_5 = 1.5;
	final static double CURSOR_SPEED_6 = 1.6;
	final static double CURSOR_SPEED_7 = 1.7;
	final static double CURSOR_SPEED_8 = 1.8;
	final static double CURSOR_SPEED_9 = 1.9;
	final static double CURSOR_SPEED_10 = 2;
	
	final static int SPEED_DEFAULT =1;
	final static int SPEED_1 =2;
	final static int SPEED_2 =3;
	final static int SPEED_3 =4;
	final static int SPEED_4 =5;
	final static int SPEED_5 =6;
	final static int SPEED_6 =7;
	final static int SPEED_7 =8;
	final static int SPEED_8 =9;
	final static int SPEED_9 =10;
	final static int SPEED_10 =11;
	int CURSOR_SPEED_MODE =1;
	
	
	double moving_gap = CURSOR_SPEED_DEFAULT;

	float LENGTH = 0.0f;
	float prevLENGTH = 0.0f;

	boolean isXYInit = true;
	boolean isFirstMove = true;
	float p1X, p1Y, p2X, p2Y;
	int movedX, movedY;
	int pressedNum = 0;

	/****************** Sliding Drawer *********************/

	ImageButton k1, k2, k3, k4, k5, k6, k7, k8, k9, k0, kenter, kclear, kplus,
	kminus, kstar, kslash, kdot, keqaul;

	SlidingDrawer numpad;

	PadClickListener pcl = new PadClickListener();

	/********************* Switch Btn **********************/

	ImageButton switch_to_gesture_btn;
	ImageButton switch_to_touch_btn;

	SwitchBtnListener sbl = new SwitchBtnListener();

	/********************* ACCELEROMETER Sensor*************/

	SensorManager sm;
	SensorEventListener accL;
	Sensor accSensor;
	float preVar0=0 ;
	float preVar1=0;

	/******************** BlueTooth ************************/

	// private static final UUID MY_UUID = UUID
	// .fromString("00001101-0000-1000-8000-00805F9B34FB");
	private String address;
	private BluetoothSocket btSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private OutputStream outStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!StartActivity.isTablet(this))
			requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (StartActivity.isTablet(this))
			setContentView(R.layout.activity_touch_tab);
		else
			setContentView(R.layout.activity_touch);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		//modetext = (TextView) findViewById(R.id.modetext);

		k0 = (ImageButton) findViewById(R.id.key_0);
		k1 = (ImageButton) findViewById(R.id.key_1);
		k2 = (ImageButton) findViewById(R.id.key_2);
		k3 = (ImageButton) findViewById(R.id.key_3);
		k4 = (ImageButton) findViewById(R.id.key_4);
		k5 = (ImageButton) findViewById(R.id.key_5);
		k6 = (ImageButton) findViewById(R.id.key_6);
		k7 = (ImageButton) findViewById(R.id.key_7);
		k8 = (ImageButton) findViewById(R.id.key_8);
		k9 = (ImageButton) findViewById(R.id.key_9);
		kenter = (ImageButton) findViewById(R.id.key_enter);
		kclear = (ImageButton) findViewById(R.id.key_clear);
		kplus = (ImageButton) findViewById(R.id.key_plus);
		kminus = (ImageButton) findViewById(R.id.key_minus);
		kstar = (ImageButton) findViewById(R.id.key_star);
		kslash = (ImageButton) findViewById(R.id.key_slash);
		kdot = (ImageButton) findViewById(R.id.key_dot);
		keqaul = (ImageButton) findViewById(R.id.key_equal);

		numpad = (SlidingDrawer) findViewById(R.id.drawer1);
		numpad.setOnDrawerOpenListener(this);
		numpad.setOnDrawerCloseListener(this);

		prevP1 = new PointF();
		prevP2 = new PointF();
		prevP3 = new PointF();
		prevP4 = new PointF();

		p1 = new PointF();
		p2 = new PointF();
		p3 = new PointF();
		p4 = new PointF();
		p5 = new PointF();

		defp1 = new PointF();
		defp2 = new PointF();
		defp3 = new PointF();
		defp4 = new PointF();
		defp5 = new PointF();


		mGestureDetector = new GestureDetector(this, new ClickListner(
				getBaseContext()));

		gestureOverlayview = new GestureOverlayView(this);
		View inflate;
		if (StartActivity.isTablet(this)) {
			Log.i("inflate", "tablet inflate");
			inflate = getLayoutInflater().inflate(
					R.layout.activity_gesturedetect_tab, null);
		} else {
			Log.i("inflate", "mobile inflate");
			inflate = getLayoutInflater().inflate(
					R.layout.activity_gesturedetect, null);
		}
		gestureOverlayview.addView(inflate);
		gestureOverlayview.addOnGesturePerformedListener(this);
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);

		if (!mLibrary.load()) {
			finish();
			Log.i(TAG, "lib load fail");
		}

		this.address = getIntent().getStringExtra("address");
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.btSocket = null;

		try {
			setSocket(this.mBluetoothAdapter.getRemoteDevice(this.address));
		} catch (Exception e) {
			e.printStackTrace();
		}

		switch_to_gesture_btn = (ImageButton) findViewById(R.id.change_to_gesture_btn);
		switch_to_gesture_btn.setOnClickListener(sbl);

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*
		modetext = (TextView) findViewById(R.id.modetext);

		k0 = (ImageButton) findViewById(R.id.key_0);
		k1 = (ImageButton) findViewById(R.id.key_1);
		k2 = (ImageButton) findViewById(R.id.key_2);
		k3 = (ImageButton) findViewById(R.id.key_3);
		k4 = (ImageButton) findViewById(R.id.key_4);
		k5 = (ImageButton) findViewById(R.id.key_5);
		k6 = (ImageButton) findViewById(R.id.key_6);
		k7 = (ImageButton) findViewById(R.id.key_7);
		k8 = (ImageButton) findViewById(R.id.key_8);
		k9 = (ImageButton) findViewById(R.id.key_9);
		kenter = (ImageButton) findViewById(R.id.key_enter);
		kclear = (ImageButton) findViewById(R.id.key_clear);
		kplus = (ImageButton) findViewById(R.id.key_plus);
		kminus = (ImageButton) findViewById(R.id.key_minus);
		kstar = (ImageButton) findViewById(R.id.key_star);
		kslash = (ImageButton) findViewById(R.id.key_slash);
		kdot = (ImageButton) findViewById(R.id.key_dot);
		keqaul = (ImageButton) findViewById(R.id.key_equal);

		numpad = (SlidingDrawer) findViewById(R.id.drawer1);
		numpad.setOnDrawerOpenListener(TouchActivity.this);
		numpad.setOnDrawerCloseListener(TouchActivity.this);
		 */
		sm.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);

	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		if (predictions.size() > 0) {
			if (predictions.get(0).score > 4.0) {
				String command = "/g";
				String action = predictions.get(0).name;
				command = command.concat(action);
				command = command.replace("\\", "");
				action = action.replace("\\g", "");
				Toast.makeText(this, action, Toast.LENGTH_SHORT).show();
				send(command);
			}
		}
	}

	public void setSocket(BluetoothDevice paramBluetoothDevice) {
		try {

			Method m = paramBluetoothDevice.getClass().getMethod(
					"createRfcommSocket", new Class[] { int.class });
			btSocket = (BluetoothSocket) m.invoke(paramBluetoothDevice, 1);
			// this.btSocket =
			// paramBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
			this.btSocket.connect();
			// this.inStream = this.btSocket.getInputStream();
			this.outStream = this.btSocket.getOutputStream();
			Log.i("TAG", "connected");
		} catch (IOException localIOException) {
			Toast.makeText(this, "Failed to connect.", Toast.LENGTH_SHORT)
			.show();
			Toast.makeText(this, "Please turn on Server.", Toast.LENGTH_SHORT)
			.show();
			localIOException.printStackTrace();
			finish();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public void send(String paramString) {
		try {
			byte[] arrayOfByte = paramString.getBytes();
			this.outStream.write(arrayOfByte);
			return;
		} catch (IOException localIOException) {
			Log.d("TAG", "send Error");
			Toast.makeText(this, "Failed to connect.", Toast.LENGTH_SHORT)
			.show();
			Toast.makeText(this, "Please try again.", Toast.LENGTH_SHORT)
			.show();
			localIOException.printStackTrace();
			finish();
		}
	}

	
	/*
	 * 
	 * 
	 * 
	 * MENU
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.touch, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.on:
			SENSOR_MODE = SENSOR_ON;
			break;
		case R.id.off:
			SENSOR_MODE = SENSOR_OFF;
			break;
		case R.id.speedDefault:
			CURSOR_SPEED_MODE=SPEED_DEFAULT;
			moving_gap=1;
			break;
		case R.id.speed1:
			CURSOR_SPEED_MODE=SPEED_1;
			moving_gap=1.1;
			break;
		case R.id.speed2:
			CURSOR_SPEED_MODE=SPEED_2;
			moving_gap=1.2;
			break;
		case R.id.speed3:
			CURSOR_SPEED_MODE=SPEED_3;
			moving_gap=1.3;
			break;
		case R.id.speed4:
			CURSOR_SPEED_MODE=SPEED_4;
			moving_gap=1.4;
			break;
		case R.id.speed5:
			CURSOR_SPEED_MODE=SPEED_5;
			moving_gap=1.5;
			break;
		case R.id.speed6:
			CURSOR_SPEED_MODE=SPEED_6;
			moving_gap=1.6;
			break;
		case R.id.speed7:
			CURSOR_SPEED_MODE=SPEED_7;
			moving_gap=1.7;
			break;
		case R.id.speed8:
			CURSOR_SPEED_MODE=SPEED_8;
			moving_gap=1.8;
			break;
		case R.id.speed9:
			CURSOR_SPEED_MODE=SPEED_9;
			moving_gap=1.9;
			break;
		case R.id.speed10:
			CURSOR_SPEED_MODE=SPEED_10;
			moving_gap=2;
			break;
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		switch(SENSOR_MODE){
		case SENSOR_ON:
			menu.findItem(R.id.on).setChecked(true);
			break;
		case SENSOR_OFF:
			menu.findItem(R.id.off).setChecked(true);
			break;
		}
		
		switch(CURSOR_SPEED_MODE){
		case SPEED_DEFAULT:
			menu.findItem(R.id.speedDefault).setChecked(true);
			break;
		case SPEED_1:
			menu.findItem(R.id.speed1).setChecked(true);
			break;
		case SPEED_2:
			menu.findItem(R.id.speed2).setChecked(true);
			break;
		case SPEED_3:
			menu.findItem(R.id.speed3).setChecked(true);
			break;
		case SPEED_4:
			menu.findItem(R.id.speed4).setChecked(true);
			break;
		case SPEED_5:
			menu.findItem(R.id.speed5).setChecked(true);
			break;
		case SPEED_6:
			menu.findItem(R.id.speed6).setChecked(true);
			break;
		case SPEED_7:
			menu.findItem(R.id.speed7).setChecked(true);
			break;
		case SPEED_8:
			menu.findItem(R.id.speed8).setChecked(true);
			break;
		case SPEED_9:
			menu.findItem(R.id.speed9).setChecked(true);
			break;
		case SPEED_10:
			menu.findItem(R.id.speed10).setChecked(true);
			break;
		}
		

		return true;
	}
	
	/*end of MENU
	 * 
	 * 
	 * 
	 * 
	 */

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			try {
				this.btSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			onBackPressed();
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * onTouchEvent!!!!!!!!!!!!
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int count = event.getPointerCount();
		//Log.i("touch_num", String.valueOf(count));// when onTouchEvent called ,,
		// what's count?
		// in in!o
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: // single touch
			/*
			Log.w("motionevent", "action down" + String.valueOf(count));
			modetext.setText("Single touch action down");
			*/
			prevP1.set(event.getX(0), event.getY(0));
			MODE = READY_SINGLE;
			break;
			//end of ACTION_DOWN


		case MotionEvent.ACTION_POINTER_DOWN:
			//Log.w("motionevent", "action pointer down" + String.valueOf(count));
			MODE = READY_MULTI;
			//modetext.setText("multi touch action down");
			switch (count) {
			case TWO:
				prevP1.set(event.getX(0), event.getY(0));
				prevP2.set(event.getX(1), event.getY(1));

				prevLENGTH = (float) Math
						.sqrt(((prevP1.x - prevP2.x) * (prevP1.x - prevP2.x))
								+ ((prevP1.y - prevP2.y) * (prevP1.y - prevP2.y)));
				pressedNum=2;
				break;
			case THREE:
				prevP1.set(event.getX(0), event.getY(0));
				prevP2.set(event.getX(1), event.getY(1));
				prevP3.set(event.getX(2), event.getY(2));
				pressedNum=3;
				break;
			case FOUR:
				prevP1.set(event.getX(0), event.getY(0));
				prevP2.set(event.getX(1), event.getY(1));
				prevP3.set(event.getX(2), event.getY(2));
				prevP4.set(event.getX(3), event.getY(3));
				/*
				Log.w("motionevent", "action pointer down (" + prevP1.x + ","
						+ prevP1.y + ")  (" + prevP2.x + "," + prevP2.y
						+ ")  (" + prevP3.x + "," + prevP3.y + ")  ("
						+ prevP4.x + "," + prevP4.y + ")");
						*/
				pressedNum = 4;
				break;
			case FIVE:
				//Log.i("fivein", "fivein!");
				prevP1.set(event.getX(0), event.getY(0));
				prevP2.set(event.getX(1), event.getY(1));
				prevP3.set(event.getX(2), event.getY(2));
				prevP4.set(event.getX(3), event.getY(3));
				// prevP5.set(event.getX(4), event.getY(4));
				//Log.i("fivein", "fiveEnd!");
				pressedNum = 5;
				break;
			}
			break;
			//end of ACTION_POINTER_DOWN


			/////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////
			//START OF ACTION_MOVE
		case MotionEvent.ACTION_MOVE:
			//Log.w("motionevent", "action move in mode" + String.valueOf(MODE));

			switch (MODE) {
			case READY_SINGLE:
				p1.set(event.getX(0), event.getY(0));
				defp1.set(Math.abs(prevP1.x - p1.x), Math.abs(prevP1.y - p1.y));
				if (defp1.x > MIN_MOUSEMOVE_VALUE
						|| defp1.y > MIN_MOUSEMOVE_VALUE) {
					//modetext.setText("ready single");
					MODE = MOUSE;
				}
				break;
			case MOUSE:
				p1.set(event.getX(0), event.getY(0));
				if (isFirstMove) {
					p1X = p1.x;
					p1Y = p1.y;
					isFirstMove = false;
				}
				movedX = (int) (p1.x - p1X);
				movedY = (int) (p1.y - p1Y);
				if (StartActivity.isTablet(this)) {
					movedX *= 1.3;
					movedY *= 1.3;
				}

				//if tablet, default is *1.3
				
				movedX *=moving_gap;
				movedY *=moving_gap;

				/*
				Log.w("gamgam", "p1.x=" + String.valueOf(p1.x) + " p1X="
						+ String.valueOf(p1X));
				Log.w("gamgam", "p1.y=" + String.valueOf(p1.y) + " p1Y="
						+ String.valueOf(p1Y));
*/
				send("/mmove" + String.valueOf(movedX) + ";"
						+ String.valueOf(movedY));

				p1X = p1.x;
				p1Y = p1.y;
/*
				Log.w("gamgam",
						String.valueOf(movedX) + " " + String.valueOf(movedY));
						*/
				break;
			case READY_MULTI:
				//Log.w("motionevent","readymulty!");
				if (pressedNum == count) {// ignore action move what isn't equal
					// first pressedNum;
					switch (count) {
					case TWO:/*
						Log.w("motionevent",
								"action move " + String.valueOf(count)
								+ "touch");
								*/
						if (isXYInit) {
							p1X = p1.x;
							p1Y = p1.y;
							p2X = p2.x;
							p2Y = p2.y;
							isXYInit = false;
						}
						p1.set(event.getX(0), event.getY(0));
						p2.set(event.getX(1), event.getY(1));

						defp1.set(prevP1.x - p1.x, prevP1.y - p1.y);
						defp2.set(prevP2.x - p2.x, prevP2.y - p2.y);

						LENGTH = (float) Math.sqrt(((p1.x - p2.x) * (p1.x - p2.x))
								+ ((p1.y - p2.y) * (p1.y - p2.y)));

						float diflen = prevLENGTH - LENGTH;
/*
						Log.i(TAG, "prev : " + String.valueOf(prevLENGTH)
								+ " / LENGTH : " + String.valueOf(LENGTH)
								+ " / diflen : " + Float.toString(diflen));
*/
						if (Math.abs(defp1.x) < 20 && Math.abs(defp1.y) < 20
								&& Math.abs(defp2.x) < 20 && Math.abs(defp2.y) < 20) {
							if (PREMODE == VSCROLL)// skip
								break;
							PREMODE = RCLICK;

						} else if (Math.abs(defp1.x) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(defp2.x) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(diflen) > -20 && Math.abs(diflen) < 20) {
							PREMODE = HSCROLL;
						} else if (Math.abs(defp1.y) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(defp2.y) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(diflen) > -20 && Math.abs(diflen) < 20) {
							if (defp1.y > 10 && defp2.y > 10) {// swipe down to
								// up///
								// in case mac,
								// scroll down
								// for (int i = 0; i < 3; i++)
								send("/tdsu");
							} else if (defp1.y < -10 && defp2.y < -10) {
								// for (int i = 0; i < 3; i++)
								send("/tdsd");
							}
							p1X = p1.x;
							p1Y = p1.y;
							p2X = p2.x;
							p2Y = p2.y;

							PREMODE = NONE;
						} else {

							if (diflen < -150) {
								PREMODE = PINCH_EXPAND;
							} else if (diflen > 150) {
								PREMODE = PINCH_REDUCT;
							}
						}

						break;
					case THREE:/*
						Log.w("motionevent",
								"action move" + String.valueOf(count) + "touch");
								*/
						p1.set(event.getX(0), event.getY(0));
						p2.set(event.getX(1), event.getY(1));
						p3.set(event.getX(2), event.getY(2));

						defp1.set(prevP1.x - p1.x, prevP1.y - p1.y);
						defp2.set(prevP2.x - p2.x, prevP2.y - p2.y);
						defp3.set(prevP3.x - p3.x, prevP3.y - p3.y);

						if (Math.abs(defp1.x) < 20 && Math.abs(defp1.y) < 20
								&& Math.abs(defp2.x) < 20 && Math.abs(defp2.y) < 20
								&& Math.abs(defp3.x) < 20 && Math.abs(defp3.y) < 20) {
							//define touch 3
						}
						if (Math.abs(defp1.x) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(defp2.x) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(defp3.x) > ULMIN_TOUCHMOVE_VALUE) {
							PREMODE = TRI_HSCROLL;
							MODE = NONE;
						} else if (Math.abs(defp1.y) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(defp2.y) > ULMIN_TOUCHMOVE_VALUE
								&& Math.abs(defp3.y) > ULMIN_TOUCHMOVE_VALUE) {
							PREMODE = TRI_VSCROLL;
							MODE = NONE;
						}

						break;
					case FOUR:
						p1.set(event.getX(0), event.getY(0));
						p2.set(event.getX(1), event.getY(1));
						p3.set(event.getX(2), event.getY(2));
						p4.set(event.getX(3), event.getY(3));
						/*
						Log.w("motionevent", "action move in mode (" + p1.x + ","
								+ p1.y + ")  (" + p2.x + "," + p2.y + ")  (" + p3.x
								+ "," + p3.y + ")  (" + p4.x + "," + p4.y + ")");
								*/
						break;

					case FIVE:
/*
						Log.w("motionevent",
								"action move" + String.valueOf(count) + "touch");
								*/
						if (Math.abs(defp1.x) < 20 && Math.abs(defp1.y) < 20
								&& Math.abs(defp2.x) < 20
								&& Math.abs(defp2.y) < 20
								&& Math.abs(defp3.x) < 20
								&& Math.abs(defp3.y) < 20
								&& Math.abs(defp4.x) < 20
								&& Math.abs(defp4.y) < 20
								&& Math.abs(defp5.x) < 20
								&& Math.abs(defp5.y) < 20) {
							PREMODE = SCREENSHOT;
						}

						break;
						//end of case FIVE;
					}
					//end of if (pressedNum == count)
				}
				break;
				//end of case READY_MULTI:
			}
			break;
			//end of ACTION_MOVE



			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ////////////////ACTION_UP	
		case MotionEvent.ACTION_UP:
			//Log.w("motionevent", "action up" + String.valueOf(count));
			if (MODE == MOUSE) {
				//modetext.setText("single action up");
				send("/menddrag");
				MODE = NONE;
				isFirstMove = true;
			}
			/*
			 * else if(MODE == DRAG){ send("/menddrag"); MODE = NONE; }
			 */

			pressedNum=0;
			break;
			//end of ACTION_UP

			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ///////////////////////////////////////////////////////////////////
			// ////////////////ACTION_POINTER_UP
		case MotionEvent.ACTION_POINTER_UP:
			//Log.w("motionevent", "action pointer up" + String.valueOf(count));
			//Log.w("motionevent", "pressedNum = " + String.valueOf(pressedNum));
			if(pressedNum==count){
				//ignore other count ,,excepting first number of touch
				switch (count) {
				case TWO:

					if (PREMODE == HSCROLL
					&& Math.abs(defp1.y) < ULMIN_TOUCHMOVE_VALUE
					&& Math.abs(defp2.y) < ULMIN_TOUCHMOVE_VALUE) {
						if ((defp1.x > ULMIN_TOUCHMOVE_VALUE && defp2.x > ULMIN_TOUCHMOVE_VALUE)
								&& Math.abs(defp1.x) > Math.abs(defp1.y)
								&& Math.abs(defp2.x) > Math.abs(defp2.y)) {
							MODE = SWIPE_LEFT;
						} else if ((defp1.x < DRMIN_TOUCHMOVE_VALUE && defp2.x < DRMIN_TOUCHMOVE_VALUE)
								&& Math.abs(defp1.x) > Math.abs(defp1.y)
								&& Math.abs(defp2.x) > Math.abs(defp2.y)) {
							MODE = SWIPE_RIGHT;
						}
					}

					else if (PREMODE == PINCH_EXPAND) {
						MODE = PINCH_EXPAND;
					} else if (PREMODE == PINCH_REDUCT) {
						MODE = PINCH_REDUCT;
					} else if (PREMODE == RCLICK) {
						MODE = RCLICK;
					}

					switch (MODE) {
					case SWIPE_LEFT:
						//modetext.setText("Swipe Left");
						send("/tdsl");
						MODE = NONE;
						break;
					case SWIPE_RIGHT:
						//modetext.setText("Swipe Right");
						send("/tdsr");
						MODE = NONE;
						break;
						/*
						 * case SWIPE_UP: modetext.setText("Swipe Up"); for (int i = 0;
						 * i < 10; i++) send("/tdsu"); MODE = NONE; break; case
						 * SWIPE_DOWN: modetext.setText("Swipe Down"); for (int i = 0; i
						 * < 10; i++) send("/tdsd"); MODE = NONE; break;
						 */
					case PINCH_EXPAND:
						//modetext.setText("Pinch(Expand)");
						send("/tpe");
						MODE = NONE;
						break;
					case PINCH_REDUCT:
						//modetext.setText("Pinch(Reduct)");
						send("/tpr");
						MODE = NONE;
						break;
					case RCLICK:
						send("/mrclick");
						MODE = NONE;
						break;
					}

					isXYInit = true;
					//Log.i("gam", String.valueOf(isXYInit));
					break;
				case THREE:

					if (PREMODE == TRI_HSCROLL
					&& Math.abs(defp1.y) < ULMIN_TOUCHMOVE_VALUE
					&& Math.abs(defp2.y) < ULMIN_TOUCHMOVE_VALUE
					&& Math.abs(defp3.y) < ULMIN_TOUCHMOVE_VALUE) {
						if ((defp1.x > ULMIN_TOUCHMOVE_VALUE
								&& defp2.x > ULMIN_TOUCHMOVE_VALUE && defp3.x > ULMIN_TOUCHMOVE_VALUE)
								&& Math.abs(defp1.x) > Math.abs(defp1.y)
								&& Math.abs(defp2.x) > Math.abs(defp2.y)
								&& Math.abs(defp3.x) > Math.abs(defp3.y)) {
							MODE = TRI_SWIPE_LEFT;
						} else if ((defp1.x < DRMIN_TOUCHMOVE_VALUE
								&& defp2.x < DRMIN_TOUCHMOVE_VALUE && defp3.x < DRMIN_TOUCHMOVE_VALUE)
								&& Math.abs(defp1.x) > Math.abs(defp1.y)
								&& Math.abs(defp2.x) > Math.abs(defp2.y)
								&& Math.abs(defp3.x) > Math.abs(defp3.y)) {
							MODE = TRI_SWIPE_RIGHT;
						}
					} else if (PREMODE == TRI_VSCROLL
							&& Math.abs(defp1.x) < ULMIN_TOUCHMOVE_VALUE
							&& Math.abs(defp2.x) < ULMIN_TOUCHMOVE_VALUE
							&& Math.abs(defp3.x) < ULMIN_TOUCHMOVE_VALUE) {
						if ((defp1.y > ULMIN_TOUCHMOVE_VALUE
								&& defp2.y > ULMIN_TOUCHMOVE_VALUE && defp3.y > ULMIN_TOUCHMOVE_VALUE)
								&& Math.abs(defp1.y) > Math.abs(defp1.x)
								&& Math.abs(defp2.y) > Math.abs(defp2.x)
								&& Math.abs(defp3.y) > Math.abs(defp3.x)) {
							MODE = TRI_SWIPE_UP;
						} else if ((defp1.y < DRMIN_TOUCHMOVE_VALUE
								&& defp2.y < DRMIN_TOUCHMOVE_VALUE && defp3.y < DRMIN_TOUCHMOVE_VALUE)
								&& Math.abs(defp1.y) > Math.abs(defp1.x)
								&& Math.abs(defp2.y) > Math.abs(defp2.x)
								&& Math.abs(defp3.y) > Math.abs(defp3.x)) {
							MODE = TRI_SWIPE_DOWN;
						}
					} else if (PREMODE == TRI_FIND_WORD) {
						MODE = TRI_FIND_WORD;
					}

					switch (MODE) {
					case TRI_SWIPE_LEFT:
						//modetext.setText("Swipe Left(TRI)");
						send("/ttsl");
						MODE = NONE;
						break;
					case TRI_SWIPE_RIGHT:
						//modetext.setText("Swipe Right(TRI)");
						send("/ttsr");
						MODE = NONE;
						break;
					case TRI_SWIPE_UP:
						//modetext.setText("Swipe Up(TRI)");
						send("/ttsu");
						MODE = NONE;
						break;
					case TRI_SWIPE_DOWN:
						//modetext.setText("Swipe Down(TRI)");
						send("/ttsd");
						MODE = NONE;
						break;
					case TRI_FIND_WORD:
						break;
					}

					break;
				case FOUR:
					/*
					Log.w("gamtang", "action pointer up 44");
					Log.w("quad", "prev Y (" + prevP1.y + ")  (" + prevP2.y
							+ ")  (" + prevP3.x + ")  (" + prevP4.y + ")");
							*/
					int maxY1 = 0,
							maxY2 = 0,
							maxY = 0;
					float bottomPY = 0,
							temp_maxY1 = 0,
							temp_maxY2 = 0;
					PointF bottomPrevP = null;

					// tonument, what's most bottom
					if (prevP1.y >= prevP2.y) {
						maxY1 = 0;
						temp_maxY1 = prevP1.y;
					} else {
						maxY1 = 1;
						temp_maxY1 = prevP2.y;
					}
					if (prevP3.y >= prevP4.y) {
						maxY2 = 2;
						temp_maxY2 = prevP3.y;
					} else {
						maxY2 = 3;
						temp_maxY2 = prevP4.y;
					}

					// find most bottomY num of point
					maxY = (temp_maxY1 >= temp_maxY2) ? maxY1 : maxY2;

					//Log.w("quad", "maxY=" + maxY);
					switch (maxY) {
					case 0:
						bottomPrevP = prevP1;
						bottomPY = p1.y;
						break;
					case 1:
						bottomPrevP = prevP2;
						bottomPY = p2.y;
						break;
					case 2:
						bottomPrevP = prevP3;
						bottomPY = p3.y;
						break;
					case 3:
						bottomPrevP = prevP4;
						bottomPY = p4.y;
						break;
					}
/*
					Log.w("quad", "starting prev Y (" + prevP1.y + ")  ("
							+ prevP2.y + ")  (" + prevP3.x + ")  (" + prevP4.y
							+ ")");
					Log.w("quad", "last Y (" + p1.y + ")  (" + p2.y + ")  (" + p3.x
							+ ")  (" + p4.y + ")");
*/
					// compare prevy with py
					if (bottomPrevP.y < bottomPY) {
						/*modetext.setText("Quad Expand");
						Log.w("quad", "Quad Expand prevY=" + bottomPrevP.y
								+ ", pY=" + bottomPY);
								*/
						if (QUADMODE == QUAD_NORMAL) {
							send("/tqe");
							QUADMODE = QUAD_BACKGROUND;
						} else if (QUADMODE == QUAD_APPLIST) {
							send("/tqr");
							QUADMODE = QUAD_NORMAL;
						}

					} else {
						//modetext.setText("Quad Reduct");
						/*Log.w("quad", "Quad reduct prevY=" + bottomPrevP.y
								+ ", pY=" + bottomPY);
								*/
						if (QUADMODE == QUAD_BACKGROUND) {
							send("/tqe");
							QUADMODE = QUAD_NORMAL;
						} else if (QUADMODE == QUAD_NORMAL) {
							send("/tqr");
							QUADMODE = QUAD_APPLIST;
						}

					}
					//Log.i(TAG, String.valueOf(QUADMODE));
					MODE = NONE;
					break;
				case FIVE:
					//Log.w("screenShot", "screenShot");
					send("/ts");
					break;
				}
				//end of switch(count)
			}
			//end of if(pressNum==count)
			break;
			//end of ACTION_POINTER_UP
		}
		// end of on TouchEvent switch
		// ///////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////////////
		// ////////////////


		if (mGestureDetector != null)
			return mGestureDetector.onTouchEvent(event);
		return false;
	}
	//end of onTouchEvent



	class SwitchBtnListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.change_to_gesture_btn) {
				setContentView(gestureOverlayview);

				switch_to_touch_btn = (ImageButton) findViewById(R.id.change_to_touch_btn);
				switch_to_touch_btn.setOnClickListener(sbl);

				// detect_mode = GESTURE;
			} else if (v.getId() == R.id.change_to_touch_btn) {
				if (StartActivity.isTablet(TouchActivity.this))
					setContentView(R.layout.activity_touch_tab);
				else
					setContentView(R.layout.activity_touch);

				//modetext = (TextView) findViewById(R.id.modetext);

				k0 = (ImageButton) findViewById(R.id.key_0);
				k1 = (ImageButton) findViewById(R.id.key_1);
				k2 = (ImageButton) findViewById(R.id.key_2);
				k3 = (ImageButton) findViewById(R.id.key_3);
				k4 = (ImageButton) findViewById(R.id.key_4);
				k5 = (ImageButton) findViewById(R.id.key_5);
				k6 = (ImageButton) findViewById(R.id.key_6);
				k7 = (ImageButton) findViewById(R.id.key_7);
				k8 = (ImageButton) findViewById(R.id.key_8);
				k9 = (ImageButton) findViewById(R.id.key_9);
				kenter = (ImageButton) findViewById(R.id.key_enter);
				kclear = (ImageButton) findViewById(R.id.key_clear);
				kplus = (ImageButton) findViewById(R.id.key_plus);
				kminus = (ImageButton) findViewById(R.id.key_minus);
				kstar = (ImageButton) findViewById(R.id.key_star);
				kslash = (ImageButton) findViewById(R.id.key_slash);
				kdot = (ImageButton) findViewById(R.id.key_dot);
				keqaul = (ImageButton) findViewById(R.id.key_equal);

				numpad = (SlidingDrawer) findViewById(R.id.drawer1);
				numpad.setOnDrawerOpenListener(TouchActivity.this);
				numpad.setOnDrawerCloseListener(TouchActivity.this);

				switch_to_gesture_btn = (ImageButton) findViewById(R.id.change_to_gesture_btn);
				switch_to_gesture_btn.setOnClickListener(sbl);
				// detect_mode = MULTITOUCH;

			}
		}

	}

	class PadClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.key_clear:
				send("/kclear");
				break;
			case R.id.key_dot:
				send("/kdot");
				break;
			case R.id.key_enter:
				send("/kenter");
				break;
			case R.id.key_equal:
				send("/kequal");
				break;
			case R.id.key_plus:
				send("/kplus");
				break;
			case R.id.key_minus:
				send("/kminus");
				break;
			case R.id.key_slash:
				send("/kslash");
				break;
			case R.id.key_star:
				send("/kstar");
				break;
			case R.id.key_0:
				send("/k0");
				break;
			case R.id.key_1:
				send("/k1");
				break;
			case R.id.key_2:
				send("/k2");
				break;
			case R.id.key_3:
				send("/k3");
				break;
			case R.id.key_4:
				send("/k4");
				break;
			case R.id.key_5:
				send("/k5");
				break;
			case R.id.key_6:
				send("/k6");
				break;
			case R.id.key_7:
				send("/k7");
				break;
			case R.id.key_8:
				send("/k8");
				break;
			case R.id.key_9:
				send("/k9");
				break;
			}

		}

	}

	public class ClickListner extends GestureDetector.SimpleOnGestureListener {

		public ClickListner(Context baseContext) {
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			//modetext.setText("Click");
			send("/mclick");
			MODE = NONE;
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			//modetext.setText("Double Click");
			send("/mdclick");
			MODE = NONE;
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			send("/mdrag");// ������
			MODE = MOUSE;
		}
	}

	@Override
	public void onDrawerOpened() {
		k0.setOnClickListener(pcl);
		k1.setOnClickListener(pcl);
		k2.setOnClickListener(pcl);
		k3.setOnClickListener(pcl);
		k4.setOnClickListener(pcl);
		k5.setOnClickListener(pcl);
		k6.setOnClickListener(pcl);
		k7.setOnClickListener(pcl);
		k8.setOnClickListener(pcl);
		k9.setOnClickListener(pcl);
		kenter.setOnClickListener(pcl);
		kclear.setOnClickListener(pcl);
		kplus.setOnClickListener(pcl);
		kminus.setOnClickListener(pcl);
		kstar.setOnClickListener(pcl);
		kslash.setOnClickListener(pcl);
		kdot.setOnClickListener(pcl);
		keqaul.setOnClickListener(pcl);
	}

	@Override
	public void onDrawerClosed() {
		k0.setOnClickListener(null);
		k1.setOnClickListener(null);
		k2.setOnClickListener(null);
		k3.setOnClickListener(null);
		k4.setOnClickListener(null);
		k5.setOnClickListener(null);
		k6.setOnClickListener(null);
		k7.setOnClickListener(null);
		k8.setOnClickListener(null);
		k9.setOnClickListener(null);
		kenter.setOnClickListener(null);
		kclear.setOnClickListener(null);
		kplus.setOnClickListener(null);
		kminus.setOnClickListener(null);
		kstar.setOnClickListener(null);
		kslash.setOnClickListener(null);
		kdot.setOnClickListener(null);
		keqaul.setOnClickListener(null);
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		synchronized (this) {
			float var0 = event.values[0];
			float var1 = event.values[1];



			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:

				if(var1>2.0 && preVar1<=2.0 && SENSOR_MODE == SENSOR_ON){//swipe left three finger
					if(StartActivity.isTablet(this))
						send("/ttsd");
					else
						send("/ttsl");
				}else if(var1<-2.0 && preVar1>=-2.0 && SENSOR_MODE == SENSOR_ON ){//swipe right three finger
					if(StartActivity.isTablet(this))
						send("/ttsu");
					else
						send("/ttsr");
				}else if(var0>2.0 && preVar0<=2.0 && SENSOR_MODE == SENSOR_ON ){//swipe down three finger
					if(StartActivity.isTablet(this))
						send("/ttsr");
					else	
						send("/ttsd");
				}else if(var0<-2.0 && preVar0>=-2.0 && SENSOR_MODE == SENSOR_ON){//swipe up three finger
					if(StartActivity.isTablet(this))
						send("/ttsl");
					else	
						send("/ttsu");
				}


				break;
			}
			preVar0=var0;preVar1=var1;
		}
	}
}


