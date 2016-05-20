package mobi.trk.app.services;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.telephony.TelephonyManager;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import mobi.trk.app.ITrkService;
import mobi.trk.app.R;
import mobi.trk.app.receivers.DeviceAdminEnabled;
import mobi.trk.app.receivers.ScreenOn;
import mobi.trk.app.utils.ServerConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class TrkService extends Service implements LocationListener {

    private static final String SENDER_ID = "980246011053";
    // Device unique key on remote server
    String key;
    // Device IMEI to retrieve key from server
    String imei;
    String mac_address;
    // SimCard identifier
    String sim;
    // Custom message from remote server
    String message;
    // Owner name from remote server
    String owner;
    // Device name
    String name;
    // Default server url
    String base_url = "https://trk-mobi.appspot.com";
    //String base_url = "http://10.0.0.128";

    String regId;

    Location last_location;

    boolean started;
    boolean registeredOnServer;
    boolean waitingForServer;

    // Local Flags - Real feature status on device
    boolean localAlarm;
    boolean localTrack;
    boolean localLock;
    boolean smsLog;
    boolean new_device;
    boolean notification_sent;

    // Current PIN (from server)
    String requestPin = "";

    // Receiver for screen unlock events
    final BroadcastReceiver lockScreenReceiver = new ScreenOn();

    // Sound related vars
    SoundPool soundpool;
    int soundId;
    int soundStreamId;

    // Flag - Sound available/unavailable
    protected String alarm_event;
    //protected String backup_event;
    protected String lock_event;
    protected String track_event;
    protected String wipe_event;
    protected String email;
    protected long last_notification;

    protected ServerConnection serverConnection;

    Messenger activityMessenger;

    NotificationManager mNotificationManager;
    DevicePolicyManager mDevicePolicyManager;
    TelephonyManager mTelephonyManager;
    LocationManager mLocationManager;
    ConnectivityManager mConnectivityManager;
    WifiManager mWifiManager;

    BroadcastReceiver b_receiver_sent_sms, b_receiver_delivered_sms;

    SharedPreferences settings;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    // SERVICE RELATED METHODS
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(b_receiver_delivered_sms);
        unregisterReceiver(b_receiver_sent_sms);
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Receive the messenger (if any)
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.get("MESSENGER").toString().equals("none")) {
                    activityMessenger = null;
                } else if (extras.get("MESSENGER") != null)
                    activityMessenger = (Messenger) extras.get("MESSENGER");
                if (extras.get("SMS_number") != null && smsLog) {
                    serverConnection.sendInstruction(base_url + "/api/device/sms/create?device_key=" + key + "&regId=" + regId + "&message=" + URLEncoder.encode(extras.getString("SMS_message")) + "&number=" + URLEncoder.encode(extras.getString("SMS_number")));
                }
            }
        }

        if (started && !waitingForServer) {                         /** If the service is started and already notified the server **/
            notifyMessenger("service_started");
            return super.onStartCommand(intent, flags, startId);
        } else if (started) {                                       /** If the service is started but haven't notified the server yet **/
            return super.onStartCommand(intent, flags, startId);
        } else {                                                    /** If it's the first run **/
            started = false;
            waitingForServer = true;

            settings = getSharedPreferences("MobiTrk", MODE_PRIVATE);

            String curRegId = settings.getString("regId", "");
            // Initialize Managers
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            mac_address = mWifiManager.getConnectionInfo().getMacAddress();
            imei = mTelephonyManager.getDeviceId();
            if (imei == null && mac_address == null) { // If the device don't have connectivity, it can't be protected
                waitingForServer = false;
                int res = super.onStartCommand(intent, flags, startId);
                this.stopSelf();
                return res;
            }
            sim = mTelephonyManager.getSimSerialNumber();
            serverConnection = new ServerConnection(this.getBaseContext());
            // Check if the device is already registered

            requestPin = settings.getString("pin", "");
            key = settings.getString("key", "");
            if(!key.equals(""))
                started = true;

            registerReceiver(b_receiver_sent_sms = new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    if (arg1.getExtras() == null) {
                        return;
                    }
                    if (arg1.getExtras().getString("sms_key") == null) {
                        return;
                    }
                    String sms_key = arg1.getExtras().getString("sms_key");
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            serverConnection.sendInstruction(base_url + "/api/device/sms/update?device_key=" + key + "&sms_key=" + sms_key + "&sent=ok");
                            break;
                        default:
                            serverConnection.sendInstruction(base_url + "/api/device/sms/update?device_key=" + key + "&sms_key=" + sms_key + "&sent=fail");
                            break;
                    }
                }
            }, new IntentFilter(SENT));
            //---when the SMS has been delivered---
            registerReceiver(b_receiver_delivered_sms = new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    if (arg1.getExtras() == null) {
                        return;
                    }
                    if (arg1.getExtras().getString("sms_key") == null) {
                        return;
                    }
                    String sms_key = arg1.getExtras().getString("sms_key");
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            serverConnection.sendInstruction(base_url + "/api/device/sms/update?device_key=" + key + "&sms_key=" + sms_key + "&delivered=ok");
                            break;
                        case Activity.RESULT_CANCELED:
                            serverConnection.sendInstruction(base_url + "/api/device/sms/update?device_key=" + key + "&sms_key=" + sms_key + "&delivered=fail");
                            break;
                    }
                }
            }, new IntentFilter(DELIVERED));


            // Stop if there's no connectivity & try again later
            if (mConnectivityManager.getActiveNetworkInfo() == null) {
                waitingForServer = false;
                started = false;
                notifyMessenger("no_connectivity");
                unregisterReceiver(b_receiver_sent_sms);
                unregisterReceiver(b_receiver_delivered_sms);
                return super.onStartCommand(intent, flags, startId);
            }
            if (!mConnectivityManager.getActiveNetworkInfo().isConnected()) {
                waitingForServer = false;
                started = false;
                notifyMessenger("no_connectivity");
                unregisterReceiver(b_receiver_sent_sms);
                unregisterReceiver(b_receiver_delivered_sms);
                return super.onStartCommand(intent, flags, startId);
            }
            if (!waitingForServer && started) {
                notifyMessenger("service_started");
            }
            // Register device on Google's cloud Messaging

            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            do {
                Log.d(this.getClass().getName(), "DO...");
                regId = GCMRegistrar.getRegistrationId(this);
                if (regId.equals("")) {
                    GCMRegistrar.register(this, SENDER_ID);
                }

                if (!regId.equals("") && key.equals("")) { // Valid reg_id but no key
                    ServerDeviceCheck();
                    waitingForServer = true;
                } else if (!regId.equals("") && curRegId.equals(regId)) { // The device has a key store locally & the registration didn't change
                    ServerDeviceRegister();
                    waitingForServer = true;
                } else if (!regId.equals("") && !curRegId.equals(regId)) { // The registration id changed
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("regId", regId);
                    editor.commit();
                    ServerDeviceRegister();
                    waitingForServer = true;
                }

                if (regId.equals("")) {    // There is no known key
                    waitingForServer = false;
                    Log.d(this.getClass().getName(), "No REG ID!! " + regId);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        ;
                    }
                    //return super.onStartCommand(intent, flags, startId);
                }
            } while (regId.equals(""));


            // Reset the lock flag in 5 seconds
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        if (waitingForServer == true) {
                            waitingForServer = false;
                            notifyMessenger("service_timeout");
                            TrkService.this.stopSelf();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // SERVER INSTRUCTION METHODS
    private void ServerDeviceCheck() {
        String url = base_url
                + "/api/device/check?imei="
                + imei
                + "&wadd="
                + mac_address
                + "&registration_id="
                + regId;
        serverConnection.sendInstruction(url);
    }

    private void ServerDeviceRegister() {
        String url = base_url
                + "/api/device/register?imei="
                + imei
                + "&wadd="
                + mac_address
                + "&device_key="
                + key
                + "&registration_id="
                + regId;
        serverConnection.sendInstruction(url);
    }

    private void ServerDeviceCreate() {
        String url = base_url
                + "/api/device/create?imei="
                + imei
                + "&wadd="
                + mac_address
                + "&registration_id="
                + regId
                + "&email="
                + email
                + "&email="
                + Build.MANUFACTURER
                + "&email="
                + Build.MODEL
                + "&os="
                + "android"
                + "&os_version="
                + Build.VERSION.INCREMENTAL;
        serverConnection.sendInstruction(url);
    }

    private void ServerLocationCreate(Location location) {
        if (location != null) {
            float accuracy = 0f;
            // Don't update the location if the accuracy value in meters is higher than the locations distance
            if (location.hasAccuracy()) {
                accuracy = location.getAccuracy();
                if (last_location != null)
                    if (location.distanceTo(last_location) < accuracy)
                        return;
            }
            // Don't update the location if the distance between points is less than 10 meters
            if (last_location != null)
                if (location.distanceTo(last_location) < 10)
                    return;
            last_location = location;
            Double lat = location.getLatitude();
            Double lng = location.getLongitude();
            String provider = location.getProvider();
            serverConnection.sendInstruction(base_url + "/api/device/event/location/create?imei="
                    + imei
                    + "&wadd=" + mac_address
                    + "&device_key="
                    + key + "&event_key=" + track_event + "&lat=" + lat + "&lng=" + lng + "&accuracy=" + accuracy + "&provider=" + provider);
        }
    }

    private void ServerEventCreate(String action) {
        serverConnection.sendInstruction(base_url + "/api/device/event/create?device_key="
                + key + "&imei=" + imei + "&wadd=" + mac_address + "&" + action + "=1");
    }

    private void ServerEventUpdate(String event_key, String action, boolean value) {
        String v = (value) ? "1" : "0";
        serverConnection.sendInstruction(base_url + "/api/device/event/update?device_key=" + key + "&event_key=" + event_key + "&imei=" + imei + "&wadd=" + mac_address + "&" + action + "=" + v);
    }

    private void smsSend(final String sms_key, String number, String message) {
        Intent sentI = new Intent(SENT);
        sentI.putExtra("sms_key", sms_key);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                sentI, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent deliveredI = new Intent(DELIVERED);
        deliveredI.putExtra("sms_key", sms_key);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                deliveredI, PendingIntent.FLAG_UPDATE_CURRENT);

        SmsManager mSmsManager = SmsManager.getDefault();
        if (message.length() > 160) {
            ArrayList parts = mSmsManager.divideMessage(message);
            mSmsManager.sendMultipartTextMessage(number, null, parts, null, null);
        } else {
            Log.d(this.getClass().getName(), "Sending SMS to " + number);
            mSmsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);
        }
    }

    Handler EnableTracking = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            deviceTrackOn();

            super.handleMessage(msg);
        }
    };

    // INTERPROCESSES STUB
    private final ITrkService.Stub mBinder = new ITrkService.Stub() {

        public boolean getTrackStatus() throws RemoteException {
            return localTrack;
        }

        public void swapTrackStatus()
                throws RemoteException {
            if (!localTrack) {
                ServerEventCreate("track");
            } else
                deviceTrackOff();
        }

        public boolean getAlarmStatus() throws RemoteException {
            return localAlarm;
        }

        public void swapAlarmStatus()
                throws RemoteException {
            if (!localAlarm) {
                ServerEventCreate("alarm");
                //deviceAlarmOn();
            } else
                deviceAlarmOff();
        }

        public boolean getLockStatus() throws RemoteException {
            return localLock;
        }

        public void swapLockStatus()
                throws RemoteException {
            if (!localLock)
                ServerEventCreate("lock");
            else {
                deviceUnlock();
            }

        }

        public String getDeviceId() throws RemoteException {
            if (imei == null)
                return "";
            return imei;
        }

        public String getSimSerial() throws RemoteException {
            return sim;
        }

        public String getMessage() throws RemoteException {
            return message;
        }

        public String getOwner() throws RemoteException {
            return owner;
        }

        @Override
        public boolean getRegistered() throws RemoteException {
            return registeredOnServer;
        }

        @Override
        public boolean isNewDevice() throws RemoteException {
            return new_device;
        }

        @Override
        public boolean isConnected() throws RemoteException {
            if(mConnectivityManager.getActiveNetworkInfo() == null)
                return false;
            if(!mConnectivityManager.getActiveNetworkInfo().isConnected())
                return false;
            return true;
        }

        @Override
        public void clearActivityMessenger() throws RemoteException {
            TrkService.this.activityMessenger = null;
        }

        public boolean isDeviceAdministrator() {
            return mDevicePolicyManager.isAdminActive(new ComponentName(TrkService.this,
                    DeviceAdminEnabled.class));
        }

        public boolean login(String pin) {
            if (pin.equals(TrkService.this.requestPin))
                return true;
            return false;
        }

        @Override
        public void setEmail(String email) throws RemoteException {
            TrkService.this.email = email;
            TrkService.this.ServerDeviceCreate();
        }

        @Override
        public String getDeviceKey() throws RemoteException {
            if (TrkService.this.key == null)
                return "";
            return TrkService.this.key;
        }

        @Override
        public void sendMessage(Bundle bundle) throws RemoteException {
            SharedPreferences.Editor editor = settings.edit();
            Log.d(this.getClass().getName(), "MOBI.TRK: " + bundle.getString("action"));
            Log.d(this.getClass().getName(), bundle.getString("data"));

            if (bundle.getString("action").equals("device_not_found")) {
                notifyMessenger("device_not_found");
                TrkService.this.new_device = true;
                TrkService.this.key = "";
                TrkService.this.name = "";
                TrkService.this.message = "";
                TrkService.this.requestPin = "";
                editor.putString("key", TrkService.this.key);
                editor.putString("name", TrkService.this.name);
                editor.putString("owner", TrkService.this.owner);
                editor.putString("pin", TrkService.this.requestPin);
                editor.putString("message", TrkService.this.message);
            } else if (bundle.getString("action").equals("event_created")) {
                try {
                    JSONObject data = new JSONObject(bundle.getString("data"));
                    if (data.getString("type").equals("alarm")) {
                        TrkService.this.alarm_event = data.getString("event_key");
                        TrkService.this.deviceAlarmOn();
                    } else if (data.getString("type").equals("backup")) {
                        ; // Not implemented
                    } else if (data.getString("type").equals("lock")) {
                        TrkService.this.lock_event = data.getString("event_key");
                        TrkService.this.deviceLock();
                    } else if (data.getString("type").equals("track")) {
                        TrkService.this.track_event = data.getString("event_key");
                        EnableTracking.sendMessage(new Message());
                        notifyMessenger("track_on");
                    } else if (data.getString("type").equals("wipe")) {
                        TrkService.this.wipe_event = data.getString("event_key");
                        TrkService.this.deviceWipe();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (bundle.getString("action").equals("event_updated")) {
                try {
                    JSONObject data = new JSONObject(bundle.getString("data"));
                    if (data.getInt("disable_date") > 0 && data.getInt("stop_date") < 0) {
                        if (data.getString("type").equals("alarm")) {

                            TrkService.this.deviceAlarmOff();
                        } else if (data.getString("type").equals("backup")) {
                            //TrkService.this.bckup_event = data.getString("backup_key");
                            ; // Not implemented
                        } else if (data.getString("type").equals("lock")) {

                            TrkService.this.deviceUnlock();
                        } else if (data.getString("type").equals("track")) {

                            TrkService.this.deviceTrackOff();
                        } else if (data.getString("type").equals("wipe")) {
                            // On event_created only
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (bundle.getString("action").equals("sms_send")) {
                TrkService.this.smsSend(bundle.getString("key"), bundle.getString("number"), bundle.getString("message"));
            } else if (bundle.getString("action").equals("device_update")) {
                try {
                    JSONObject data = new JSONObject(bundle.getString("data"));
                    TrkService.this.key = data.getString("device_key");
                    TrkService.this.name = data.getString("name");
                    TrkService.this.message = data.getString("message");
                    TrkService.this.requestPin = data.getString("pin");
                    TrkService.this.smsLog = data.getBoolean("smslog");
                    TrkService.this.new_device = false;

                    try {
                        Context c = TrkService.this.getBaseContext();
                        if (c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode < data.getInt("last_app_version") && data.getInt("last_app_version") != settings.getInt("last_version_notified", 0)) {
                            Log.d(this.getClass().getName(), data.getInt("last_app_version") + " !=" + settings.getInt("last_version_notified", 0));
                            editor.putInt("last_version_notified", data.getInt("last_app_version"));
                            editor.commit();
                            Notification notification = new Notification(R.drawable.logo, getString(R.string.app_name), System.currentTimeMillis());
                            notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
                            notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                            notification.ledARGB = Color.CYAN;
                            notification.ledOnMS = 1000;
                            notification.ledOffMS = 1200;

                            Intent notificationIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=mobi.trk.app"));

                            PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                            notification.setLatestEventInfo(c, getString(R.string.app_name), getString(R.string.update_available), contentIntent);
                            mNotificationManager.notify(1, notification);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (data.getBoolean("alarm")) {
                        TrkService.this.alarm_event = data
                                .getString("alarmevent");
                        TrkService.this.deviceAlarmOn();
                    }
                    if (!data.getBoolean("alarm") && !data.getString("alarmevent").equals("")) {
                        TrkService.this.alarm_event = data
                                .getString("alarmevent");
                        TrkService.this.deviceAlarmOff();
                    }
                    if (data.getBoolean("lock")) {
                        TrkService.this.lock_event = data
                                .getString("lockevent");
                        TrkService.this.deviceLock();
                    }
                    if (!data.getBoolean("lock") && !data.getString("lockevent").equals("")) {
                        TrkService.this.lock_event = data
                                .getString("lockevent");
                        TrkService.this.deviceUnlock();
                    }
                    if (data.getBoolean("track")) {
                        TrkService.this.track_event = data
                                .getString("trackevent");
                        EnableTracking.sendMessage(new Message());
                    }
                    if (!data.getBoolean("track") && !data.getString("trackevent").equals("")) {
                        TrkService.this.track_event = data
                                .getString("trackevent");
                        TrkService.this.deviceTrackOff();
                    }
                    if (data.getBoolean("wipe")) {
                        TrkService.this.wipe_event = data.getString("wipeevent");
                        TrkService.this.deviceWipe();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                editor.putString("key", TrkService.this.key);
                editor.putString("name", TrkService.this.name);
                editor.putString("owner", TrkService.this.owner);
                editor.putString("pin", TrkService.this.requestPin);
                editor.putString("message", TrkService.this.message);
                if (registeredOnServer == false) {
                    registeredOnServer = true;
                    //notifyMessenger("service_started");
                }
                waitingForServer = false;
                editor.commit();
                notifyMessenger("device_update");
            } else if (bundle.getString("action").equals("invalid_key")) {
                TrkService.this.key = "";
                editor.putString("key", TrkService.this.key);
                TrkService.this.stopSelf();
            } else {
                Log.i(this.getClass().getSimpleName(),
                        "Unknown action received: "
                                + bundle.getString("action"));
            }
            editor.putString("pin", TrkService.this.requestPin);
            editor.commit();
        }

    };

    // GPS RELATED METHODS
    @Override
    public void onLocationChanged(final Location location) {
        ServerLocationCreate(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // DEVICE ACTIONS
    protected boolean deviceLock() {
        if (!mDevicePolicyManager.isAdminActive(new ComponentName(TrkService.this, DeviceAdminEnabled.class))) {
            localLock = false;
            return false;
        }

        notifyMessenger("lock_on");
        // Hard Lock the device to avoid lock screen bypassing
        localLock = mDevicePolicyManager.resetPassword(requestPin, 0);
        // Force the screen to lock by turning it off
        mDevicePolicyManager.lockNow();
        // Set receivers for ACTION_SCREEN_ON to call our lock screen
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(lockScreenReceiver, filter);
        ServerEventUpdate(lock_event, "activelock", true);
        localLock = true;
        return true;
    }

    protected boolean deviceUnlock() {
        if (!mDevicePolicyManager.isAdminActive(new ComponentName(TrkService.this, DeviceAdminEnabled.class))) {
            localLock = true;
            return false;
        }
        notifyMessenger("lock_off");
        localLock = !mDevicePolicyManager.resetPassword("", 0);
        try {
            unregisterReceiver(lockScreenReceiver);
            localLock = false;
        } catch (Exception ignored) {
            localLock = true;
        }
        ServerEventUpdate(lock_event, "activelock", false);
        mNotificationManager.cancelAll();
        return true;
    }

    protected boolean deviceAlarmOn() {

        soundpool = new SoundPool(5, AudioManager.STREAM_ALARM, 100);
        soundpool
                .setOnLoadCompleteListener(new OnLoadCompleteListener() {
                    public void onLoadComplete(SoundPool soundPool,
                                               int sampleId, int status) {
                        if ((soundStreamId = soundPool.play(sampleId,
                                0.5f, 0.5f, 1, -1, 1.0f)) != 0) {
                            ServerEventUpdate(alarm_event, "activealarm", true);
                            localAlarm = true;
                            notifyMessenger("alarm_on");
                        } else {
                            ServerEventUpdate(alarm_event, "alarm", false);
                            localAlarm = false;
                            notifyMessenger("alarm_off");
                        }
                    }
                });
        // Carrega o audio para o soundpool
        soundId = soundpool.load(getBaseContext(), R.raw.alarm, 1);

        return true;
    }

    protected boolean deviceAlarmOff() {
        if (soundpool != null) {
            soundpool.stop(soundStreamId);
            soundpool.release();
            soundpool = null;
        }
        notifyMessenger("alarm_off");
        localAlarm = false;
        ServerEventUpdate(alarm_event, "activealarm", false);
        mNotificationManager.cancelAll();
        return true;
    }

    protected boolean deviceTrackOn() {

        if (localTrack)
            return true;
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = mLocationManager.getBestProvider(criteria, true);
        Log.d(this.getClass().getName(), "Location provider:" + provider.toString());
        if (provider == null) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 120000, 8,
                    TrkService.this);
        } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                    provider, 120000, 8,
                    TrkService.this);

        } else {
            mLocationManager.requestLocationUpdates(
                    provider, 600000, 15,
                    TrkService.this);
        }
        Location location = mLocationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = mLocationManager
                    .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location == null)
            location = mLocationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null)
            ServerLocationCreate(location);
        ServerEventUpdate(track_event, "activetrack", true);
        localTrack = true;
        notifyMessenger("track_on");

        Looper.loop();
        return true;
    }

    protected boolean deviceTrackOff() {
        mLocationManager.removeUpdates(TrkService.this);
        localTrack = false;
        notifyMessenger("track_off");
        ServerEventUpdate(track_event, "activetrack", false);
        return true;
    }

    protected boolean deviceWipe() {

        ServerEventUpdate(wipe_event, "wiped", true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mDevicePolicyManager.wipeData(0);
            }
        }).start();

        return true;
    }

    protected void notifyMessenger(String str) {
        CharSequence notifmsg = null;
        if (str.equals("alarm_on"))
            notifmsg = getString(R.string.alarm_on_txt);
        if (str.equals("lock_on"))
            notifmsg = getString(R.string.lock_on_txt);
        if (str.equals("first_time"))
            notifmsg = getString(R.string.configure_web);
        if (notifmsg != null) {
            Notification notification = new Notification(R.drawable.logo, getString(R.string.app_name), System.currentTimeMillis());
            notification.defaults = Notification.DEFAULT_LIGHTS;
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            Intent notificationIntent = new Intent(this, mobi.trk.app.activities.Login.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setLatestEventInfo(this.getApplicationContext(), getString(R.string.app_name), notifmsg, contentIntent);
            mNotificationManager.notify(1, notification);
        }
        if (activityMessenger != null) {
            Message m = new Message();
            Bundle b = new Bundle();
            b.putString("msg", str);
            m.setData(b);
            try {
                activityMessenger.send(m);
            } catch (RemoteException e) {
                ;
            }
        }
    }
}

