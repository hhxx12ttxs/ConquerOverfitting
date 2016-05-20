package pt.up.fe.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import pt.up.fe.Logic.SensorItem;
import pt.up.fe.Logic.SetecApp;
import pt.up.fe.Messages.LoginMessage;
import pt.up.fe.Messages.SensorListMessage;
import pt.up.fe.Presentation.LoginFragment;
import pt.up.fe.Presentation.MainActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service {

	public static BackgroundService bService;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private boolean mRunServerSocket = false;
	private boolean DEBUG = true;
	private PhoneServerSocket mPhoneConnection;
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	private int connectionRetry = 500;	// wait Milliseconds for connection
	private int serviceRetry = 5;		// Restart service in seconds
	private boolean sticky = true;
	private boolean mRunning = false;
	private SetecApp mAppState;
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_DATA_UPDATED = 3;	//Messenger identifier

	//BLE
	private BluetoothAdapter BLEAdapter;
    private Handler BLEHandler;
    private BLE mBLE;
	
    
    /** Keeps track of all current registered clients. */
    static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /** Holds last value set by a client. */
    static int mValue = 0;

	@Override
	public void onCreate() {
		
		if ( mRunning ) {
			return;
        }
		
		bService = this;
		mAppState = (SetecApp) getApplicationContext();
		
        // Check login state
        if(!mAppState.isUserLoggedIn()) {
        	if(DEBUG) Log.w("Service","Trying manual login");
        	if(mAppState.isUserCaretaker()) {
        		// Try to Login Caretaker
        		SharedPreferences settings = getApplicationContext().getSharedPreferences("Login", 0);
        	    String user = settings.getString("Username", null);
        	    String pass = settings.getString("Password", null);
        	    if(user!=null && pass!=null)
        	    {
        	    	
        	    	LoginMessage lm = new LoginMessage(user, LoginFragment.md5(pass));
        	    	String ans = lm.sendMessage( mAppState );
        	    	Log.e("RES","resposnta "+ans);
        	    	if(ans == "ok") {
        	    		mAppState.setUserLoggedIn(true);
        	    	}
        	    	else {
        	    		sticky = false;
        	    		//stopSelf();
        	    		return;
        	    	}
        	    } else {
        	    	sticky = false;
        	    	//stopSelf();
        	    	return;
        	    }
        	} else {
        		// Try to Login Senior
        		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        			final BluetoothManager bluetoothManager =
        			        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        			BLEAdapter = bluetoothManager.getAdapter();
        			BLEHandler = new Handler();
        			
        			if (BLEAdapter == null){
        	            sticky = false;
        	            //stopSelf();
        	            return;
        	        }
        			
        			if(!mAppState.isUserLoggedIn())
        			{
	        			LoginMessage lm = new LoginMessage(BLEAdapter.getAddress());
	    	        	String ans = lm.sendMessage( mAppState );
	    	        	
	    	        	if( ans == "ok" ) {
	    	        		mAppState.setUserLoggedIn(true);
	    	        	} else {
	            	    	sticky = false;
	            	    	//stopSelf();
	            	    	return;
	            	    }
        			}
    	        	
        		} else {
        			if(DEBUG) Log.w("Service","No BLE in device");
        	    	sticky = false;
        	    	//stopSelf();
        	    	return;
        	    }
        	}
        }
		
		HandlerThread thread = new HandlerThread("SetecBackgroundService", Process.THREAD_PRIORITY_DEFAULT);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper, this);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if ( mRunning ) {
			return START_STICKY;
        }
		if( !sticky )
			return 0;
    	
        mRunning = true;
		
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		if(DEBUG) Log.i(ACTIVITY_SERVICE, "Service Starting");
		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	/**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
    	Log.e(STORAGE_SERVICE, "Service bounded");
        return mMessenger.getBinder();
    }

	@Override
	public void onDestroy() {
		mRunning = false;

		Toast.makeText(this, "Service gone \n Restarting in "+(serviceRetry+10)+" seconds", Toast.LENGTH_SHORT).show();
		// Close Server Socket
		mPhoneConnection.closeSocket();
		mPhoneConnection.closeServerSocket();
		
		if( sticky ) {
			// Schedule next Service wake up
			alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			Intent intentAlarm = new Intent(getApplicationContext(), BackgroundService.class);
			alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);
			
			// Try again after x+10 seconds
			alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
			        SystemClock.elapsedRealtime() +
			        (serviceRetry+10) * 1000, alarmIntent);
			if(DEBUG) Log.w(ACTIVITY_SERVICE, "Service reboot scheduled in "+(serviceRetry+10)+" seconds");
		}
		// Unregister broadcast receiver
		unregisterReceiver(mReceiver);
		stopForeground(true);
		if(DEBUG) Log.i(ACTIVITY_SERVICE, "Service Ending");
	}
	
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	/**
     * Handler of incoming messages from clients.
     */
    @SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_DATA_UPDATED:
                	mValue = msg.arg1;
                    for (int i=mClients.size()-1; i>=0; i--) {
                        try {
                            mClients.get(i).send(Message.obtain(null,
                            		MSG_DATA_UPDATED, mValue, 0));
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
	
	//Broadcast Receiver for connectivity_changed
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
				
			// If connection changed while service is running
			if(intent.getAction().equals(SetecApp.CONNECTIVITY_CHANGED) && mRunning) {
				
				// Schedule next Service wake up
				alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
				Intent intentAlarm = new Intent(getApplicationContext(), BackgroundService.class);
				alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);
				
				// Try again after x seconds
				alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				        SystemClock.elapsedRealtime() +
				        serviceRetry * 1000, alarmIntent);
				if(DEBUG) Log.e(ACTIVITY_SERVICE, "Retry after "+serviceRetry+" seconds");
				
				// Set socket state
				setServerSocketState(false);	// TODO: nope
				
				// Close Server Socket
				mPhoneConnection.closeSocket();
				mPhoneConnection.closeServerSocket();
				
				// Unregister broadcast receiver
				unregisterReceiver(mReceiver);
				if(mServiceHandler.obtainMessage() != null)
					stopSelf(mServiceHandler.obtainMessage().arg1);
				else 
					stopSelf();
			}
		}
	};

	// Handler that receives messages from the thread
	private static class ServiceHandler extends Handler {
		//Message mMsg = null;
		private final WeakReference<BackgroundService> mService; 
		
		public ServiceHandler(Looper looper, BackgroundService service) {
			super(looper);
			final IntentFilter myFilter = new IntentFilter(SetecApp.CONNECTIVITY_CHANGED);
			mService = new WeakReference<BackgroundService>(service);
			mService.get().registerReceiver(mService.get().mReceiver, myFilter);
		}
		
		@Override
		public void handleMessage(Message msg) {
			//mMsg = msg;
			
			BackgroundService service = mService.get();
			if( service != null) {
			
				// Check for Wifi Connectivity
				ConnectivityManager cm =
				        (ConnectivityManager)service.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
				 
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
				boolean isConnected = activeNetwork != null &&
				                      activeNetwork.isConnectedOrConnecting();
				
				boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
							
				// If phone currently on WIFI
				if( isConnected && isWiFi ){
					//Cancel alarm
					if (service.alarmMgr!= null) {
						service.alarmMgr.cancel(service.alarmIntent);
					}
					
					// Get Current IP Addrs		//String phoneIP = getIPAddress(true);
					String phoneIP = service.wifiIpAddress();
					
					if( phoneIP.isEmpty() )
						return;

					//onPreExecute
					service.mPhoneConnection = service.new PhoneServerSocket(phoneIP, 5001);
					service.mPhoneConnection.startServer();
					if(service.DEBUG) Log.e(ACTIVITY_SERVICE, "Server Started");
					
					//doInBackground
					while (service.runServerSocket()) {
						if(service.DEBUG) Log.e(ACTIVITY_SERVICE, "Server Listening");
						service.mPhoneConnection.waitConnection();
						if(service.DEBUG) Log.e(ACTIVITY_SERVICE, "server: "+
								service.mPhoneConnection.isServerConnected()+" socket: "+
								service.mPhoneConnection.isSocketConnected());
						service.getData();
						service.mPhoneConnection.closeSocket();
					}
		
					//onPostExecute
					service.mPhoneConnection.closeServerSocket();
					if(service.DEBUG) Log.e(ACTIVITY_SERVICE, "Server Ended");
				}
				else {
					service.getApplicationContext();
					// Schedule next wake
					service.alarmMgr = (AlarmManager)service.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
					Intent intent = new Intent(service.getApplicationContext(), BackgroundService.class);
					service.alarmIntent = PendingIntent.getService(service.getApplicationContext(), 0, intent, 0);
					
					// Try again after x seconds
					service.alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					        SystemClock.elapsedRealtime() +
					        service.serviceRetry * 1000, service.alarmIntent);
					if(service.DEBUG) Log.e(ACTIVITY_SERVICE, "Retry after 10 seconds");
				}
				if( service.isRestricted() ) 
					service.unregisterReceiver(service.mReceiver);
				service.stopSelf(msg.arg1);
			}
		}
		
	}
	
	public boolean runServerSocket() {
		return mRunServerSocket;
	}

	public void setServerSocketState(boolean runServerSocket) {
		mRunServerSocket = runServerSocket;
	}
	
	public void sendBLEToServer(ArrayList<BLEDevice> devices) {
		Log.i("Bluetooth","Sending device list to Base Station...");
		SensorListMessage connect = new SensorListMessage(mAppState);
		connect.sendSensors(devices);
		Log.i("Bluetooth","Devices sent");
	}
	
	private void getData(){
		long endTime = System.currentTimeMillis() + connectionRetry;
		int iter = 0;
		while ( System.currentTimeMillis() < endTime ) {
			synchronized (this) {
				try {
					// Send some data
					// mPhoneConnection.sendData("Hello\n");
					
					// Get data
					String data = mPhoneConnection.getData();
					
					if(data != null) {
						Log.e("RECEIVED", data);
						iter = 0;
						String inParts[]=data.split("#");
						String ans=null;
						
						if(inParts[0].equals("12"))
						{
							if(!mAppState.isUserCaretaker() && inParts.length > 2)
							{
								int delta = Integer.parseInt(inParts[1]);
								if(DEBUG) Log.w("Service","BLE request with delta: "+delta+" seconds");
								//BLE
						    	if(BLEAdapter.isEnabled()){
						    		mBLE = new BLE(BLEAdapter, BLEHandler, delta, this);
							    	mBLE.getStartBLE().run();
							    	//(new BLE(BLEAdapter, BLEHandler, delta, this)).startScan();
							    	//BLEHandler.postDelayed(mBLE.getStopBLE(), delta);
							    	//mBLE.getStartBLE().run();
							        //END OF BLE
							    	mPhoneConnection.sendData("12#1#.\n");
						    	}else
						    		mPhoneConnection.sendData("12#2#.\n");
							}
						}
						else if(inParts[0].equals("5"))	// Atualizacao de estado
						{
							if(mAppState.isUserCaretaker() && mAppState.isUserLoggedIn() && inParts.length > 4)
							{
								mAppState.getSeniorById( Integer.parseInt(inParts[2]) )
										 .getSensorById( Integer.parseInt(inParts[1]) )
										 .setSensorState( Integer.parseInt(inParts[3]) == 1 );
								
								ans = "5#" + mAppState.getCurrentuserId() + "#" + inParts[2] + "#" + inParts[1] + "#.\n";
								mPhoneConnection.sendData(ans);
							}
							else
							{
								mAppState.getUserSeniorData()
								 .getSensorById( Integer.parseInt(inParts[1]) )
								 .setSensorState( Integer.parseInt(inParts[2]) == 1 );
						
								ans = "5#" + mAppState.getCurrentuserId() + "#" + inParts[1] + "#.\n";
								mPhoneConnection.sendData(ans);
							}
							// Notify UI
							for (int i=mClients.size()-1; i>=0; i--) {
				                try {
				                    mClients.get(i).send(Message.obtain(null,
				                            MSG_DATA_UPDATED, MSG_DATA_UPDATED, 0));
				                } catch (RemoteException e) {
				                    // The client is dead.  Remove it from the list;
				                    // we are going through the list from back to front
				                    // so this is safe to do inside the loop.
				                    mClients.remove(i);
				                }
				            }
						}
						else if(inParts[0].equals("6"))	// Alarme Timeout
						{
							if(!mAppState.isUserCaretaker() && mAppState.isUserLoggedIn()  && inParts.length > 2)
							{
								mAppState.getUserSeniorData()
									.getSensorById( Integer.parseInt(inParts[1]) )
									.setSensorAlarmActivated(true);
								
								ans = "6#" + mAppState.getCurrentuserId()+"#"+inParts[1]+"#1#.\n";
								mPhoneConnection.sendData(ans);
								
								// Broadcast alarm
								// Build Intent
								ArrayList<String> sensorsId = new ArrayList<String>();
								sensorsId.add(inParts[1]);
						        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class); 
						        newIntent.putExtra("alarm_notification", true);			//Set Intent as Notification
						        newIntent.putExtra("alarm_type", 6);					//Set Notification type
						        newIntent.putExtra("alarm_sensors_list", sensorsId );	//Set Sensors Alarm
						        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);  
						        getApplicationContext().startActivity(newIntent);
							}
						}
						else if(inParts[0].equals("7"))	// Alarme Proximidade
						{
							if(!mAppState.isUserCaretaker() && inParts.length > 2)
							{
								mAppState.getUserSeniorData()
									.getSensorById( Integer.parseInt(inParts[1]) )
									.setSensorAlarmActivated(true);
								
								ans = "7#" + mAppState.getCurrentuserId()+"#"+inParts[1]+"#1#.\n";
								mPhoneConnection.sendData(ans);
								
								// Broadcast alarm
								// Build Intent
								ArrayList<String> sensorsId = new ArrayList<String>();
								sensorsId.add(inParts[1]);
						        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class); 
						        newIntent.putExtra("alarm_notification", true);			//Set Intent as Notification
						        newIntent.putExtra("alarm_type", 7);					//Set Notification type
						        newIntent.putExtra("alarm_sensors_list", sensorsId );	//Set Sensors Alarm
						        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );  
						        getApplicationContext().startActivity(newIntent);
							}
						}
						else if(inParts[0].equals("8"))	// Alarme Caretaker
						{
							if(mAppState.isUserCaretaker() && mAppState.isUserLoggedIn() && inParts.length > 3)
							{
								
								//8#IDd#IDs#.
								SensorItem sensor = 
										mAppState.getSeniorById( Integer.parseInt(inParts[2]) ).getSensorById(Integer.parseInt(inParts[1]));
								
								if(sensor == null) {
									SensorListMessage sm = new SensorListMessage(Integer.parseInt(inParts[2]), mAppState);
									ans=sm.sendMessage();
									if(ans!="ok"){
										if(DEBUG) Log.e("Service","Get data reply: Not ok");
										return;
									}
										
								}		
								mAppState.getSeniorById( Integer.parseInt(inParts[2]) )
									 .getSensorById( Integer.parseInt(inParts[1]) )
									 .setSensorAlarmActivated(true);

								//8#IDc#IDd#IDs#1#.
								ans = "8#" + mAppState.getCurrentuserId()+"#"+inParts[1]+"#"+inParts[2]+"#1#.\n";

								mPhoneConnection.sendData(ans);

								// Broadcast alarm
								// Build Intent
								ArrayList<String> sensorsId = new ArrayList<String>();
								sensorsId.add(inParts[1]);
						        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class); 
						        newIntent.putExtra("alarm_notification", true);			//Set Intent as Notification
						        newIntent.putExtra("alarm_type", 8);					//Set Notification type
						        newIntent.putExtra("alarm_caretaker_id", 
						        		Integer.parseInt(mAppState.getCurrentuserId()));//Set Current User Id
						        newIntent.putExtra("alarm_senior_id",					//Set Senior User Id
						        		Integer.parseInt(inParts[2]));
						        newIntent.putExtra("alarm_sensors_list", sensorsId );	//Set Sensors Alarm
						        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );  
						        getApplicationContext().startActivity(newIntent);
								
							}
						}	
						else if(inParts[0].equals("9"))	// Alarme Caretaker Mal Funcionamento
						{
							if(mAppState.isUserCaretaker() && mAppState.isUserLoggedIn() && inParts.length > 3)
							{
								//9#IDd#IDs#.
								mAppState.getSeniorById( Integer.parseInt(inParts[2]) )
									 .getSensorById( Integer.parseInt(inParts[1]) )
									 .setSensorAlarmActivated(true);
								
								//9#IDc#IDd#IDs#1#.
								ans = "9#" + mAppState.getCurrentuserId() + "#" + inParts[1] + "#" + inParts[2] + "#1#.\n";
								mPhoneConnection.sendData(ans);
								
								// Broadcast alarm
								// Build Intent
								ArrayList<String> sensorsId = new ArrayList<String>();
								sensorsId.add(inParts[1]);
						        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class); 
						        newIntent.putExtra("alarm_notification", true);			//Set Intent as Notification
						        newIntent.putExtra("alarm_type", 9);					//Set Notification type
						        newIntent.putExtra("alarm_caretaker_id", 
						        		Integer.parseInt(mAppState.getCurrentuserId()));//Set Current User Id
						        newIntent.putExtra("alarm_senior_id",					//Set Senior User Id
						        		Integer.parseInt(inParts[2]));
						        newIntent.putExtra("alarm_sensors_list", sensorsId );	//Set Sensors Alarm
						        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );  
						        getApplicationContext().startActivity(newIntent);
							}
						}
					}
					else {
						//if(DEBUG) Log.e(ACTIVITY_SERVICE, "No data: waiting : "+iter+" iter state");
						// if no data receiver for 5 iterations
						// close socket
						if ( !(iter < 5) ){
							break;
						}
						
						// Wait for x seconds 
						wait(endTime - System.currentTimeMillis());
						endTime = System.currentTimeMillis() + connectionRetry;
						iter++;
					}
				} catch (Exception e) {
				}
			}
		}
	}
		
	private class PhoneServerSocket {
		
		private InetSocketAddress mPhoneAddrs;
		private Socket mSocket = new Socket();
		private ServerSocket mServerSocket = null;
		private DataOutputStream outToServer;
		private BufferedReader inFromServer;
		

		private PhoneServerSocket(String phoneIP, int phonePort) {
			super();
			mPhoneAddrs = new InetSocketAddress(phoneIP, phonePort);
			setServerSocketState(true);
		}

		private void startServer(){
			//Construct ServerSocket
			try {
				mServerSocket = new ServerSocket(mPhoneAddrs.getPort(), 1, mPhoneAddrs.getAddress());
				setServerSocketState(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void waitConnection(){
			try {
				if(mServerSocket.isClosed())
					startServer();
				
				if(DEBUG) Log.e(ACTIVITY_SERVICE, "Waiting for connection :: Phone IP "+mServerSocket.getInetAddress().toString());
				mSocket = mServerSocket.accept();
				if(DEBUG) Log.e(ACTIVITY_SERVICE, "Connection established with "+mSocket.getInetAddress().toString());
				mSocket.setSoTimeout(10*1000);
				
				outToServer = new DataOutputStream(mSocket.getOutputStream());
				inFromServer = new BufferedReader( new InputStreamReader(mSocket.getInputStream()));
				if(DEBUG) Log.e(ACTIVITY_SERVICE, "Socket Opened");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void sendData(String data){
			try {
				if(mSocket.isClosed())
					waitConnection();
				Log.e("Send", data);
				outToServer.writeBytes(data);
			} catch (Exception e) {
				//Log.e(ACTIVITY_SERVICE, "Writer Exception");
			}
		}
		
		private String getData(){
			try {
				if(mSocket.isClosed())
					waitConnection();
				
				if (inFromServer.ready())
					return inFromServer.readLine();
			} catch (Exception e) {
				// TODO: handle exception
				if(DEBUG) Log.e(ACTIVITY_SERVICE, "Reader Exception");
			}
			return null;
		}
		
		private void closeSocket(){
			if(mSocket != null){
				try {
					if(!mSocket.isClosed())
						mSocket.close();
					if(outToServer != null)
						outToServer.close();
					if(inFromServer != null)
						inFromServer.close();
					if(DEBUG) Log.e(ACTIVITY_SERVICE, "Socket Closed");
				} catch (IOException e) {
					if(DEBUG) Log.e(ACTIVITY_SERVICE, "Socket Not Opened - Not Closing");
					//e.printStackTrace();
				}
			}
		}
		
		private void closeServerSocket(){
			if(mServerSocket != null){
				try {
					mServerSocket.close();
				} catch (IOException e) {
					if(DEBUG) Log.e(ACTIVITY_SERVICE, "Server Socket Not Opened - Not Closing");
					//e.printStackTrace();
				}
			}
		}
		
		private boolean isServerConnected(){
			return !mServerSocket.isClosed();
		}
		
		private boolean isSocketConnected(){
			return mSocket.isConnected();
		}

	}
		
	protected String wifiIpAddress() {
		
	    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
	    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

	    // Convert little-endian to big-endianif needed
	    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
	        ipAddress = Integer.reverseBytes(ipAddress);
	    }

	    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

	    String ipAddressString;
	    try {
	        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
	    } catch (UnknownHostException ex) {
	    	if(DEBUG) Log.e("WIFIIP", "Unable to get host address.");
	        ipAddressString = null;
	    }

	    return ipAddressString;
	}
	
	/**
     * Get IP address from first non-localhost interface
     * without using Android specific methods
     * source: http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
     * 
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
	/*private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }*/

}

