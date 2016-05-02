package biz.bokhorst.xprivacy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InterfaceAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends Activity implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

	private int mThemeId;
	private Spinner spRestriction = null;
	private AppListAdapter mAppAdapter = null;
	private boolean mUsed = false;
	private boolean mPro = false;

	private BroadcastReceiver mPackageChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ActivityMain.this.recreate();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Salt should be the same when exporting/importing
		String salt = PrivacyManager.getSetting(null, this, PrivacyManager.cSettingSalt, null, false);
		if (salt == null) {
			salt = Build.SERIAL;
			if (salt == null)
				salt = "";
			PrivacyManager.setSetting(null, this, PrivacyManager.cSettingSalt, salt);
		}

		// Set theme
		String themeName = PrivacyManager.getSetting(null, this, PrivacyManager.cSettingTheme, "", false);
		mThemeId = (themeName.equals("Dark") ? R.style.CustomTheme : R.style.CustomTheme_Light);
		setTheme(mThemeId);

		// Set layout
		setContentView(R.layout.mainlist);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Get localized restriction name
		List<String> listRestriction = PrivacyManager.getRestrictions(true);
		List<String> listLocalizedRestriction = new ArrayList<String>();
		for (String restrictionName : listRestriction)
			listLocalizedRestriction.add(PrivacyManager.getLocalizedName(this, restrictionName));
		listLocalizedRestriction.add(0, getString(R.string.menu_all));

		// Build spinner adapter
		SpinnerAdapter spAdapter = new SpinnerAdapter(this, android.R.layout.simple_spinner_item);
		spAdapter.addAll(listLocalizedRestriction);

		// Handle info
		ImageView imgInfo = (ImageView) findViewById(R.id.imgInfo);
		imgInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int position = spRestriction.getSelectedItemPosition();
				if (position != AdapterView.INVALID_POSITION) {
					String title = (position == 0 ? "XPrivacy" : PrivacyManager.getRestrictions(true).get(position - 1));
					String url = String.format("http://wiki.faircode.eu/index.php?title=%s", title);
					Intent infoIntent = new Intent(Intent.ACTION_VIEW);
					infoIntent.setData(Uri.parse(url));
					startActivity(infoIntent);
				}
			}
		});

		// Setup spinner
		spRestriction = (Spinner) findViewById(R.id.spRestriction);
		spRestriction.setAdapter(spAdapter);
		spRestriction.setOnItemSelectedListener(this);

		// Handle help
		ImageView ivHelp = (ImageView) findViewById(R.id.ivHelp);
		ivHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = new Dialog(ActivityMain.this);
				dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
				dialog.setTitle(getString(R.string.help_application));
				dialog.setContentView(R.layout.help);
				dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, getThemed(R.attr.icon_launcher));
				dialog.setCancelable(true);
				dialog.show();
			}
		});

		ImageView imgEdit = (ImageView) findViewById(R.id.imgEdit);
		imgEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast toast = Toast.makeText(ActivityMain.this, getString(R.string.msg_edit), Toast.LENGTH_LONG);
				toast.show();
			}
		});

		// Setup used filter
		final ImageView imgUsed = (ImageView) findViewById(R.id.imgUsed);
		if (savedInstanceState != null && savedInstanceState.containsKey("Used")) {
			mUsed = savedInstanceState.getBoolean("Used");
			imgUsed.setImageDrawable(getResources().getDrawable(
					getThemed(mUsed ? R.attr.icon_used : R.attr.icon_used_grayed)));
		}
		imgUsed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mUsed = !mUsed;
				imgUsed.setImageDrawable(getResources().getDrawable(
						getThemed(mUsed ? R.attr.icon_used : R.attr.icon_used_grayed)));
				applyFilter();
			}
		});

		// Setup name filter
		final EditText etFilter = (EditText) findViewById(R.id.etFilter);
		etFilter.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String text = etFilter.getText().toString();
				ImageView imgClear = (ImageView) findViewById(R.id.imgClear);
				imgClear.setImageDrawable(getResources().getDrawable(
						getThemed(text.equals("") ? R.attr.icon_clear_grayed : R.attr.icon_clear)));
				applyFilter();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		ImageView imgClear = (ImageView) findViewById(R.id.imgClear);
		imgClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				etFilter.setText("");
			}
		});

		// Setup restriction filter
		CheckBox cbFilter = (CheckBox) findViewById(R.id.cbFilter);
		cbFilter.setOnCheckedChangeListener(this);

		// Start task to get app list
		AppListTask appListTask = new AppListTask();
		appListTask.execute();

		// Check environment
		checkRequirements();

		// Licensing
		checkLicense();

		// Listen for package add/remove
		IntentFilter iff = new IntentFilter();
		iff.addAction(Intent.ACTION_PACKAGE_ADDED);
		iff.addAction(Intent.ACTION_PACKAGE_REMOVED);
		iff.addDataScheme("package");
		registerReceiver(mPackageChangeReceiver, iff);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("Used", mUsed);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAppAdapter != null)
			mAppAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPackageChangeReceiver != null)
			unregisterReceiver(mPackageChangeReceiver);
	}

	private static final int LICENSED = 0x0100;
	private static final int NOT_LICENSED = 0x0231;
	private static final int RETRY = 0x0123;

	private static final int ERROR_CONTACTING_SERVER = 0x101;
	private static final int ERROR_INVALID_PACKAGE_NAME = 0x102;
	private static final int ERROR_NON_MATCHING_UID = 0x103;

	private void checkLicense() {
		if (Util.hasProLicense(this) == null) {
			if (Util.isProInstalled(this))
				try {
					int uid = getPackageManager().getApplicationInfo("biz.bokhorst.xprivacy.pro", 0).uid;
					PrivacyManager.deleteRestrictions(this, uid);
					Util.log(null, Log.INFO, "Licensing: check");
					startActivityForResult(new Intent("biz.bokhorst.xprivacy.pro.CHECK"), 0);
				} catch (Throwable ex) {
					Util.bug(null, ex);
				}
		} else {
			Toast toast = Toast.makeText(this, getString(R.string.menu_pro), Toast.LENGTH_LONG);
			toast.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			int code = data.getIntExtra("Code", -1);
			int reason = data.getIntExtra("Reason", -1);

			String sReason;
			if (reason == LICENSED)
				sReason = "LICENSED";
			else if (reason == NOT_LICENSED)
				sReason = "NOT_LICENSED";
			else if (reason == RETRY)
				sReason = "RETRY";
			else if (reason == ERROR_CONTACTING_SERVER)
				sReason = "ERROR_CONTACTING_SERVER";
			else if (reason == ERROR_INVALID_PACKAGE_NAME)
				sReason = "ERROR_INVALID_PACKAGE_NAME";
			else if (reason == ERROR_NON_MATCHING_UID)
				sReason = "ERROR_NON_MATCHING_UID";
			else
				sReason = Integer.toString(reason);

			Util.log(null, Log.INFO, "Licensing: code=" + code + " reason=" + sReason);

			if (code > 0) {
				mPro = true;
				invalidateOptionsMenu();
				Toast toast = Toast.makeText(this, getString(R.string.menu_pro), Toast.LENGTH_LONG);
				toast.show();
			} else if (reason == RETRY) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						checkLicense();
					}
				}, 60 * 1000);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean pro = (mPro || Util.hasProLicense(this) != null);
		boolean mounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

		menu.findItem(R.id.menu_export).setEnabled(pro && mounted);
		menu.findItem(R.id.menu_import).setEnabled(pro && mounted);
		menu.findItem(R.id.menu_pro).setVisible(!pro);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.menu_all:
				optionAll();
				return true;
			case R.id.menu_settings:
				optionSettings();
				return true;
			case R.id.menu_template:
				optionTemplate();
				return true;
			case R.id.menu_update:
				optionCheckUpdate();
				return true;
			case R.id.menu_report:
				optionReportIssue();
				return true;
			case R.id.menu_export:
				optionExport();
				return true;
			case R.id.menu_import:
				optionImport();
				return true;
			case R.id.menu_theme:
				optionSwitchTheme();
				return true;
			case R.id.menu_pro:
				optionPro();
				return true;
			case R.id.menu_about:
				optionAbout();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (Throwable ex) {
			Util.bug(null, ex);
			return true;
		}
	}

	// Spinner

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		selectRestriction(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		selectRestriction(0);
	}

	private void selectRestriction(int pos) {
		if (mAppAdapter != null) {
			String restrictionName = (pos == 0 ? null : PrivacyManager.getRestrictions(true).get(pos - 1));
			mAppAdapter.setRestrictionName(restrictionName);
			applyFilter();
		}
	}

	private class SpinnerAdapter extends ArrayAdapter<String> {

		public SpinnerAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = super.getView(position, convertView, parent);
			row.setBackgroundColor(getBackgroundColor(position));
			return row;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View row = super.getDropDownView(position, convertView, parent);
			row.setBackgroundColor(getBackgroundColor(position));
			return row;
		}

		private int getBackgroundColor(int position) {
			String restrictionName = (position == 0 ? null : PrivacyManager.getRestrictions(true).get(position - 1));
			if (PrivacyManager.isDangerousRestriction(restrictionName))
				return getResources().getColor(getThemed(R.attr.color_dangerous));
			else {
				TypedArray array = getTheme().obtainStyledAttributes(new int[] { android.R.attr.colorBackground, });
				return array.getColor(0, 0);
			}
		}
	}

	// Filtering

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		CheckBox cbFilter = (CheckBox) findViewById(R.id.cbFilter);
		if (buttonView == cbFilter)
			applyFilter();
	}

	private void applyFilter() {
		if (mAppAdapter != null) {
			EditText etFilter = (EditText) findViewById(R.id.etFilter);
			CheckBox cbFilter = (CheckBox) findViewById(R.id.cbFilter);
			String filter = String.format("%b\n%s\n%b", mUsed, etFilter.getText().toString(), cbFilter.isChecked());
			mAppAdapter.getFilter().filter(filter);
		}
	}

	private void checkRequirements() {
		// Check Android version
		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.ICE_CREAM_SANDWICH
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN
				&& Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN_MR1) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(getString(R.string.app_name));
			alertDialogBuilder.setMessage(getString(R.string.app_wrongandroid));
			alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent xposedIntent = new Intent(Intent.ACTION_VIEW);
					xposedIntent.setData(Uri.parse("https://github.com/M66B/XPrivacy#installation"));
					startActivity(xposedIntent);
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		// Check Xposed version
		int xVersion = Util.getXposedVersion();
		if (xVersion < PrivacyManager.cXposedMinVersion) {
			String msg = String.format(getString(R.string.app_notxposed), PrivacyManager.cXposedMinVersion);
			Util.log(null, Log.WARN, msg);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(getString(R.string.app_name));
			alertDialogBuilder.setMessage(msg);
			alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent xposedIntent = new Intent(Intent.ACTION_VIEW);
					xposedIntent.setData(Uri.parse("http://forum.xda-developers.com/showthread.php?t=1574401"));
					startActivity(xposedIntent);
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		// Check if XPrivacy is enabled
		if (!Util.isXposedEnabled()) {
			String msg = getString(R.string.app_notenabled);
			Util.log(null, Log.WARN, msg);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(getString(R.string.app_name));
			alertDialogBuilder.setMessage(msg);
			alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
			alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent xInstallerIntent = getPackageManager().getLaunchIntentForPackage(
							"de.robv.android.xposed.installer");
					if (xInstallerIntent != null) {
						xInstallerIntent.putExtra("opentab", 1);
						startActivity(xInstallerIntent);
					}
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		// Check activity manager
		if (!checkField(getSystemService(Context.ACTIVITY_SERVICE), "mContext", Context.class))
			reportClass(getSystemService(Context.ACTIVITY_SERVICE).getClass());

		// Check activity thread
		try {
			Class<?> clazz = Class.forName("android.app.ActivityThread");
			try {
				clazz.getDeclaredMethod("unscheduleGcIdler");
			} catch (NoSuchMethodException ex) {
				reportClass(clazz);
			}
		} catch (Throwable ex) {
			sendSupportInfo(ex.toString());
		}

		// Check activity thread receiver data
		try {
			Class<?> clazz = Class.forName("android.app.ActivityThread$ReceiverData");
			if (!checkField(clazz, "intent"))
				reportClass(clazz);
		} catch (Throwable ex) {
			try {
				reportClass(Class.forName("android.app.ActivityThread"));
			} catch (Throwable exex) {
				sendSupportInfo(exex.toString());
			}
		}

		// Check content resolver
		if (!checkField(getContentResolver(), "mContext", Context.class))
			reportClass(getContentResolver().getClass());

		// Check interface address
		if (!checkField(InterfaceAddress.class, "address") || !checkField(InterfaceAddress.class, "broadcastAddress")
				|| PrivacyManager.getDefacedProp("InetAddress") == null)
			reportClass(InterfaceAddress.class);

		// Check package manager
		if (!checkField(getPackageManager(), "mContext", Context.class))
			reportClass(getPackageManager().getClass());

		// Check package manager service
		try {
			Class<?> clazz = Class.forName("com.android.server.pm.PackageManagerService");
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					clazz.getDeclaredMethod("getPackageUid", String.class, int.class);
				else
					clazz.getDeclaredMethod("getPackageUid", String.class);
			} catch (NoSuchMethodException ex) {
				reportClass(clazz);
			}
		} catch (Throwable ex) {
			sendSupportInfo(ex.toString());
		}

		// Check runtime
		try {
			Runtime.class.getDeclaredMethod("load", String.class, ClassLoader.class);
			Runtime.class.getDeclaredMethod("loadLibrary", String.class, ClassLoader.class);
		} catch (NoSuchMethodException ex) {
			reportClass(Runtime.class);
		}

		// Check telephony manager
		if (!checkField(getSystemService(Context.TELEPHONY_SERVICE), "sContext", Context.class)
				&& !checkField(getSystemService(Context.TELEPHONY_SERVICE), "mContext", Context.class))
			reportClass(getSystemService(Context.TELEPHONY_SERVICE).getClass());

		// Check wifi info
		if (!checkField(WifiInfo.class, "mSupplicantState") || !checkField(WifiInfo.class, "mBSSID")
				|| !checkField(WifiInfo.class, "mIpAddress") || !checkField(WifiInfo.class, "mMacAddress")
				|| !(checkField(WifiInfo.class, "mSSID") || checkField(WifiInfo.class, "mWifiSsid")))
			reportClass(WifiInfo.class);

		// Check mWifiSsid.octets
		if (checkField(WifiInfo.class, "mWifiSsid"))
			try {
				Class<?> clazz = Class.forName("android.net.wifi.WifiSsid");
				if (!checkField(clazz, "octets"))
					reportClass(clazz);
			} catch (Throwable ex) {
				sendSupportInfo(ex.toString());
			}
	}

	private boolean checkField(Object obj, String fieldName, Class<?> expectedClass) {
		try {
			// Find field
			Field field = null;
			Class<?> superClass = (obj == null ? null : obj.getClass());
			while (superClass != null)
				try {
					field = superClass.getDeclaredField(fieldName);
					field.setAccessible(true);
					break;
				} catch (Throwable ex) {
					superClass = superClass.getSuperclass();
				}

			// Check field
			if (field != null) {
				Object value = field.get(obj);
				if (value != null && expectedClass.isAssignableFrom(value.getClass()))
					return true;
			}
		} catch (Throwable ex) {
		}
		return false;
	}

	private boolean checkField(Class<?> clazz, String fieldName) {
		try {
			clazz.getDeclaredField(fieldName);
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}

	private void optionAll() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.app_name));
		alertDialogBuilder.setMessage(getString(R.string.msg_sure));
		alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
		alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mAppAdapter != null) {
					// Check if some restricted
					boolean someRestricted = false;
					for (int pos = 0; pos < mAppAdapter.getCount(); pos++) {
						ApplicationInfoEx xAppInfo = mAppAdapter.getItem(pos);
						if (mAppAdapter.getRestrictionName() == null) {
							for (boolean restricted : PrivacyManager.getRestricted(getApplicationContext(),
									xAppInfo.getUid(), false))
								if (restricted) {
									someRestricted = true;
									break;
								}
						} else if (PrivacyManager.getRestricted(null, ActivityMain.this, xAppInfo.getUid(),
								mAppAdapter.getRestrictionName(), null, false, false))
							someRestricted = true;
						if (someRestricted)
							break;
					}

					// Invert selection
					for (int pos = 0; pos < mAppAdapter.getCount(); pos++) {
						ApplicationInfoEx xAppInfo = mAppAdapter.getItem(pos);
						if (mAppAdapter.getRestrictionName() == null) {
							for (String restrictionName : PrivacyManager.getRestrictions(false))
								PrivacyManager.setRestricted(null, ActivityMain.this, xAppInfo.getUid(),
										restrictionName, null, !someRestricted);
						} else
							PrivacyManager.setRestricted(null, ActivityMain.this, xAppInfo.getUid(),
									mAppAdapter.getRestrictionName(), null, !someRestricted);
					}

					// Refresh
					mAppAdapter.notifyDataSetChanged();
				}
			}
		});
		alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void optionSettings() {
		// Build dialog
		final Dialog dlgSettings = new Dialog(this);
		dlgSettings.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dlgSettings.setTitle(getString(R.string.app_name));
		dlgSettings.setContentView(R.layout.settings);
		dlgSettings.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, getThemed(R.attr.icon_launcher));

		// Reference controls
		final EditText etLat = (EditText) dlgSettings.findViewById(R.id.etLat);
		final EditText etLon = (EditText) dlgSettings.findViewById(R.id.etLon);
		final EditText etSearch = (EditText) dlgSettings.findViewById(R.id.etSearch);
		Button btnSearch = (Button) dlgSettings.findViewById(R.id.btnSearch);
		final EditText etMac = (EditText) dlgSettings.findViewById(R.id.etMac);
		final EditText etImei = (EditText) dlgSettings.findViewById(R.id.etImei);
		final EditText etPhone = (EditText) dlgSettings.findViewById(R.id.etPhone);
		final EditText etId = (EditText) dlgSettings.findViewById(R.id.etId);
		final EditText etGsfId = (EditText) dlgSettings.findViewById(R.id.etGsfId);
		final EditText etMcc = (EditText) dlgSettings.findViewById(R.id.etMcc);
		final EditText etMnc = (EditText) dlgSettings.findViewById(R.id.etMnc);
		final EditText etCountry = (EditText) dlgSettings.findViewById(R.id.etCountry);
		final EditText etIccId = (EditText) dlgSettings.findViewById(R.id.etIccId);
		final EditText etSubscriber = (EditText) dlgSettings.findViewById(R.id.etSubscriber);
		final CheckBox cbFPermission = (CheckBox) dlgSettings.findViewById(R.id.cbFPermission);
		final CheckBox cbFSystem = (CheckBox) dlgSettings.findViewById(R.id.cbFSystem);
		Button btnOk = (Button) dlgSettings.findViewById(R.id.btnOk);

		// Set current values
		final boolean fPermission = PrivacyManager.getSettingBool(null, ActivityMain.this,
				PrivacyManager.cSettingFPermission, true, false);
		final boolean fSystem = PrivacyManager.getSettingBool(null, ActivityMain.this, PrivacyManager.cSettingFSystem,
				true, false);

		etLat.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingLatitude, "", false));
		etLon.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingLongitude, "", false));
		etMac.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingMac, "", false));
		etImei.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingImei, "", false));
		etPhone.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingPhone, "", false));
		etId.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingId, "", false));
		etGsfId.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingGsfId, "", false));
		etMcc.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingMcc, "", false));
		etMnc.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingMnc, "", false));
		etCountry
				.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingCountry, "", false));
		etIccId.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingIccId, "", false));
		etSubscriber.setText(PrivacyManager.getSetting(null, ActivityMain.this, PrivacyManager.cSettingSubscriber, "",
				false));
		cbFPermission.setChecked(fPermission);
		cbFSystem.setChecked(fSystem);

		// Handle search
		etSearch.setEnabled(Geocoder.isPresent());
		btnSearch.setEnabled(Geocoder.isPresent());
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					etLat.setText("");
					etLon.setText("");
					String search = etSearch.getText().toString();
					final List<Address> listAddress = new Geocoder(ActivityMain.this).getFromLocationName(search, 1);
					if (listAddress.size() > 0) {
						Address address = listAddress.get(0);

						// Get coordinates
						if (address.hasLatitude())
							etLat.setText(Double.toString(address.getLatitude()));
						if (address.hasLongitude())
							etLon.setText(Double.toString(address.getLongitude()));

						// Get address
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
							if (i != 0)
								sb.append(", ");
							sb.append(address.getAddressLine(i));
						}
						etSearch.setText(sb.toString());
					}
				} catch (Throwable ex) {
					Util.bug(null, ex);
				}
			}
		});

		// Wait for OK
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Set location
				try {
					float lat = Float.parseFloat(etLat.getText().toString().replace(',', '.'));
					float lon = Float.parseFloat(etLon.getText().toString().replace(',', '.'));
					if (lat < -90 || lat > 90 || lon < -180 || lon > 180)
						throw new InvalidParameterException();

					PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingLatitude,
							Float.toString(lat));
					PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingLongitude,
							Float.toString(lon));

				} catch (Throwable ex) {
					PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingLatitude, "");
					PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingLongitude, "");
				}

				// Other settings
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingMac, etMac.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingImei, etImei.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingPhone, etPhone.getText()
						.toString());
				PrivacyManager
						.setSetting(null, ActivityMain.this, PrivacyManager.cSettingId, etId.getText().toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingGsfId, etGsfId.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingMcc, etMcc.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingMnc, etMnc.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingCountry, etCountry.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingIccId, etIccId.getText()
						.toString());
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingSubscriber, etSubscriber
						.getText().toString());

				// Set filter by permission
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingFPermission,
						Boolean.toString(cbFPermission.isChecked()));

				// Set filter by system
				PrivacyManager.setSetting(null, ActivityMain.this, PrivacyManager.cSettingFSystem,
						Boolean.toString(cbFSystem.isChecked()));

				// Refresh if needed
				if (fPermission != cbFPermission.isChecked() || fSystem != cbFSystem.isChecked()) {
					AppListTask appListTask = new AppListTask();
					appListTask.execute();
				}

				// Done
				dlgSettings.dismiss();
			}
		});

		dlgSettings.setCancelable(true);
		dlgSettings.show();
	}

	private void optionTemplate() {
		// Get restriction categories
		final List<String> listRestriction = PrivacyManager.getRestrictions(false);
		CharSequence[] options = new CharSequence[listRestriction.size()];
		boolean[] selection = new boolean[listRestriction.size()];
		for (int i = 0; i < listRestriction.size(); i++) {
			options[i] = PrivacyManager.getLocalizedName(this, listRestriction.get(i));
			selection[i] = PrivacyManager.getSettingBool(null, this,
					String.format("Template.%s", listRestriction.get(i)), true, false);
		}

		// Build dialog
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.menu_template));
		alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
		alertDialogBuilder.setMultiChoiceItems(options, selection, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
				PrivacyManager.setSetting(null, ActivityMain.this,
						String.format("Template.%s", listRestriction.get(whichButton)), Boolean.toString(isChecked));
			}
		});
		alertDialogBuilder.setPositiveButton(getString(R.string.msg_done), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
			}
		});

		// Show dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void optionCheckUpdate() {
		new UpdateTask().execute("http://goo.im/json2&path=/devs/M66B/xprivacy");
	}

	private void optionReportIssue() {
		// Report issue
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/M66B/XPrivacy/issues"));
		startActivity(browserIntent);
	}

	private void optionPro() {
		// Redirect to pro page
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.faircode.eu/xprivacy/"));
		startActivity(browserIntent);
	}

	private void optionExport() {
		ExportTask exportTask = new ExportTask();
		exportTask.execute(getExportFile());
	}

	private void optionImport() {
		ImportTask importTask = new ImportTask();
		importTask.execute(getExportFile());
	}

	private void optionSwitchTheme() {
		String themeName = PrivacyManager.getSetting(null, this, PrivacyManager.cSettingTheme, "", false);
		themeName = (themeName.equals("Dark") ? "Light" : "Dark");
		PrivacyManager.setSetting(null, this, PrivacyManager.cSettingTheme, themeName);
		this.recreate();
	}

	private void optionAbout() {
		// About
		Dialog dlgAbout = new Dialog(this);
		dlgAbout.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dlgAbout.setTitle(getString(R.string.app_name));
		dlgAbout.setContentView(R.layout.about);
		dlgAbout.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, getThemed(R.attr.icon_launcher));

		// Show version
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView tvVersion = (TextView) dlgAbout.findViewById(R.id.tvVersion);
			tvVersion.setText(String.format(getString(R.string.app_version), pInfo.versionName, pInfo.versionCode));
		} catch (Throwable ex) {
			Util.bug(null, ex);
		}

		// Show Xposed version
		int xVersion = Util.getXposedVersion();
		TextView tvXVersion = (TextView) dlgAbout.findViewById(R.id.tvXVersion);
		tvXVersion.setText(String.format(getString(R.string.app_xversion), xVersion));

		// Show license
		String licensed = Util.hasProLicense(this);
		TextView tvLicensed = (TextView) dlgAbout.findViewById(R.id.tvLicensed);
		if (licensed == null)
			tvLicensed.setVisibility(View.GONE);
		else
			tvLicensed.setText(String.format(getString(R.string.msg_licensed), licensed));

		dlgAbout.setCancelable(true);
		dlgAbout.show();
	}

	private File getExportFile() {
		File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ ".xprivacy");
		folder.mkdir();
		String fileName = folder + File.separator + "XPrivacy.xml";
		return new File(fileName);
	}

	private String fetchJson(String... uri) {
		try {
			// Request downloads
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(uri[0]));
			StatusLine statusLine = response.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				// Succeeded
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				return out.toString("ISO-8859-1");
			} else {
				// Failed
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (Throwable ex) {
			Util.bug(null, ex);
			return ex.toString();
		}
	}

	private void processJson(String json) {
		try {
			// Parse result
			String version = null;
			String url = null;
			if (json != null)
				if (json.startsWith("{")) {
					long newest = 0;
					String prefix = "XPrivacy_";
					JSONObject jRoot = new JSONObject(json);
					JSONArray jArray = jRoot.getJSONArray("list");
					for (int i = 0; jArray != null && i < jArray.length(); i++) {
						// File
						JSONObject jEntry = jArray.getJSONObject(i);
						String filename = jEntry.getString("filename");
						if (filename.startsWith(prefix)) {
							// Check if newer
							long modified = jEntry.getLong("modified");
							if (modified > newest) {
								newest = modified;
								version = filename.substring(prefix.length()).replace(".apk", "");
								url = "http://goo.im" + jEntry.getString("path");
							}
						}
					}
				} else {
					Toast toast = Toast.makeText(ActivityMain.this, json, Toast.LENGTH_LONG);
					toast.show();
				}

			if (url == null || version == null) {
				// Assume no update
				String msg = getString(R.string.msg_noupdate);
				Toast toast = Toast.makeText(ActivityMain.this, msg, Toast.LENGTH_LONG);
				toast.show();
			} else {
				// Compare versions
				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				Version ourVersion = new Version(pInfo.versionName);
				Version latestVersion = new Version(version);
				if (ourVersion.compareTo(latestVersion) < 0) {
					// Update available
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				} else {
					// No update available
					String msg = getString(R.string.msg_noupdate);
					Toast toast = Toast.makeText(ActivityMain.this, msg, Toast.LENGTH_LONG);
					toast.show();
				}
			}
		} catch (Throwable ex) {
			Toast toast = Toast.makeText(ActivityMain.this, ex.toString(), Toast.LENGTH_LONG);
			toast.show();
			Util.bug(null, ex);
		}
	}

	private void reportClass(final Class<?> clazz) {
		String msg = String.format("Incompatible %s", clazz.getName());
		Util.log(null, Log.WARN, msg);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.app_name));
		alertDialogBuilder.setMessage(msg);
		alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
		alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendClassInfo(clazz);
			}
		});
		alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void sendClassInfo(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append(clazz.getName());
		sb.append("\r\n");
		sb.append("\r\n");
		for (Constructor<?> constructor : clazz.getConstructors()) {
			sb.append(constructor.toString());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		for (Method method : clazz.getDeclaredMethods()) {
			sb.append(method.toString());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		for (Field field : clazz.getDeclaredFields()) {
			sb.append(field.toString());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		sendSupportInfo(sb.toString());
	}

	private void sendSupportInfo(String text) {
		String xversion = null;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			xversion = pInfo.versionName;
		} catch (Throwable ex) {
		}

		StringBuilder sb = new StringBuilder(text);
		sb.insert(0, String.format("Android SDK %d\r\n", Build.VERSION.SDK_INT));
		sb.insert(0, String.format("XPrivacy %s\r\n", xversion));
		sb.append("\r\n");

		Intent sendEmail = new Intent(Intent.ACTION_SEND);
		sendEmail.setType("message/rfc822");
		sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "marcel+xprivacy@faircode.eu" });
		sendEmail.putExtra(Intent.EXTRA_SUBJECT, "XPrivacy support info");
		sendEmail.putExtra(Intent.EXTRA_TEXT, sb.toString());
		try {
			startActivity(sendEmail);
		} catch (Throwable ex) {
			Util.bug(null, ex);
		}
	}

	private class UpdateTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			return fetchJson(uri);
		}

		@Override
		protected void onPostExecute(String json) {
			super.onPostExecute(json);
			if (json != null)
				processJson(json);
		}
	}

	private class ExportTask extends AsyncTask<File, String, String> {
		private File mFile;
		private final static int NOTIFY_ID = 1;

		@Override
		protected String doInBackground(File... params) {
			mFile = params[0];
			try {
				// Serialize
				FileOutputStream fos = new FileOutputStream(mFile);
				try {
					XmlSerializer serializer = Xml.newSerializer();
					serializer.setOutput(fos, "UTF-8");
					serializer.startDocument(null, Boolean.valueOf(true));
					serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
					serializer.startTag(null, "XPrivacy");

					// Process settings
					publishProgress(getString(R.string.menu_settings));

					Map<String, String> mapSetting = PrivacyManager.getSettings(ActivityMain.this);
					for (String setting : mapSetting.keySet())
						if (setting.startsWith("Account.") || setting.startsWith("Contact.")) {
							// TODO: translate uid to package name
						} else {
							// Serialize setting
							String value = mapSetting.get(setting);
							serializer.startTag(null, "Setting");
							serializer.attribute(null, "Name", setting);
							serializer.attribute(null, "Value", value);
							serializer.endTag(null, "Setting");
						}

					// Process restrictions
					List<PrivacyManager.RestrictionDesc> listRestriction = PrivacyManager
							.getRestricted(ActivityMain.this);
					Map<String, List<PrivacyManager.RestrictionDesc>> mapRestriction = new HashMap<String, List<PrivacyManager.RestrictionDesc>>();
					for (PrivacyManager.RestrictionDesc restriction : listRestriction) {
						String[] packages = getPackageManager().getPackagesForUid(restriction.uid);
						if (packages == null)
							Util.log(null, Log.WARN, "No packages for uid=" + restriction.uid);
						else
							for (String packageName : packages) {
								if (!mapRestriction.containsKey(packageName))
									mapRestriction.put(packageName, new ArrayList<PrivacyManager.RestrictionDesc>());
								mapRestriction.get(packageName).add(restriction);
							}
					}

					// Process result
					for (String packageName : mapRestriction.keySet()) {
						publishProgress(packageName);
						for (PrivacyManager.RestrictionDesc restrictionDesc : mapRestriction.get(packageName)) {
							serializer.startTag(null, "Package");
							serializer.attribute(null, "Name", packageName);
							serializer.attribute(null, "Restriction", restrictionDesc.restrictionName);
							if (restrictionDesc.methodName != null)
								serializer.attribute(null, "Method", restrictionDesc.methodName);
							serializer.attribute(null, "Restricted", Boolean.toString(restrictionDesc.restricted));
							serializer.endTag(null, "Package");
						}
					}

					// End serialization
					serializer.endTag(null, "XPrivacy");
					serializer.endDocument();
					serializer.flush();
				} finally {
					fos.close();
				}

				// Display message
				return getString(R.string.msg_done);
			} catch (Throwable ex) {
				Util.bug(null, ex);
				return ex.toString();
			}
		}

		@Override
		protected void onPreExecute() {
			notify(getExportFile().getAbsolutePath(), true);
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			notify(values[0], true);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			notify(result, false);
			super.onPostExecute(result);
		}

		private void notify(String text, boolean ongoing) {
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.this);
			notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
			notificationBuilder.setContentTitle(getString(R.string.menu_export));
			notificationBuilder.setContentText(text);
			notificationBuilder.setWhen(System.currentTimeMillis());
			if (ongoing)
				notificationBuilder.setOngoing(true);
			else {
				// Build result intent
				Intent resultIntent = new Intent(ActivityMain.this, ActivityMain.class);
				resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				// Build pending intent
				PendingIntent pendingIntent = PendingIntent.getActivity(ActivityMain.this, NOTIFY_ID, resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				notificationBuilder.setAutoCancel(true);
				notificationBuilder.setContentIntent(pendingIntent);
			}
			Notification notification = notificationBuilder.build();

			NotificationManager notificationManager = (NotificationManager) ActivityMain.this
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFY_ID, notification);
		}
	}

	private class ImportTask extends AsyncTask<File, String, String> {

		private File mFile;
		private final static int NOTIFY_ID = 2;

		@Override
		protected String doInBackground(File... params) {
			mFile = params[0];
			try {
				// Read XML
				FileInputStream fis = new FileInputStream(mFile);
				InputStreamReader isr = new InputStreamReader(fis);
				char[] inputBuffer = new char[fis.available()];
				isr.read(inputBuffer);
				String xml = new String(inputBuffer);
				isr.close();
				fis.close();

				// Prepare XML document
				InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(is);
				dom.getDocumentElement().normalize();

				// Process settings
				publishProgress(getString(R.string.menu_settings));
				PrivacyManager.deleteSettings(ActivityMain.this);
				NodeList sItems = dom.getElementsByTagName("Setting");
				for (int i = 0; i < sItems.getLength(); i++) {
					// Process package restriction
					Node entry = sItems.item(i);
					NamedNodeMap attrs = entry.getAttributes();
					String setting = attrs.getNamedItem("Name").getNodeValue();
					String value = attrs.getNamedItem("Value").getNodeValue();
					PrivacyManager.setSetting(null, ActivityMain.this, setting, value);
				}

				// Process restrictions
				Map<String, Map<String, List<String>>> mapPackage = new HashMap<String, Map<String, List<String>>>();
				NodeList rItems = dom.getElementsByTagName("Package");
				for (int i = 0; i < rItems.getLength(); i++) {
					// Process package restriction
					Node entry = rItems.item(i);
					NamedNodeMap attrs = entry.getAttributes();
					String packageName = attrs.getNamedItem("Name").getNodeValue();
					String restrictionName = attrs.getNamedItem("Restriction").getNodeValue();
					String methodName = (attrs.getNamedItem("Method") == null ? null : attrs.getNamedItem("Method")
							.getNodeValue());

					// Map package restriction
					if (!mapPackage.containsKey(packageName))
						mapPackage.put(packageName, new HashMap<String, List<String>>());
					if (!mapPackage.get(packageName).containsKey(restrictionName))
						mapPackage.get(packageName).put(restrictionName, new ArrayList<String>());
					if (methodName != null)
						mapPackage.get(packageName).get(restrictionName).add(methodName);
				}

				// Process result
				for (String packageName : mapPackage.keySet()) {
					try {
						publishProgress(packageName);

						// Get uid
						int uid = getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;

						// Reset existing restrictions
						PrivacyManager.deleteRestrictions(ActivityMain.this, uid);

						// Set imported restrictions
						for (String restrictionName : mapPackage.get(packageName).keySet()) {
							PrivacyManager.setRestricted(null, ActivityMain.this, uid, restrictionName, null, true);
							for (String methodName : mapPackage.get(packageName).get(restrictionName))
								PrivacyManager.setRestricted(null, ActivityMain.this, uid, restrictionName, methodName,
										false);
						}
					} catch (NameNotFoundException ex) {
						Util.log(null, Log.WARN, "Not found package=" + packageName);
					}
				}

				// Display message
				return getString(R.string.msg_done);
			} catch (Throwable ex) {
				Util.bug(null, ex);
				return ex.toString();
			}
		}

		@Override
		protected void onPreExecute() {
			notify(getExportFile().getAbsolutePath(), true);
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			notify(values[0], true);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			notify(result, false);
			ActivityMain.this.recreate();
			super.onPostExecute(result);
		}

		private void notify(String text, boolean ongoing) {
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.this);
			notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
			notificationBuilder.setContentTitle(getString(R.string.menu_import));
			notificationBuilder.setContentText(text);
			notificationBuilder.setWhen(System.currentTimeMillis());
			if (ongoing)
				notificationBuilder.setOngoing(true);
			else {
				// Build result intent
				Intent resultIntent = new Intent(ActivityMain.this, ActivityMain.class);
				resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				// Build pending intent
				PendingIntent pendingIntent = PendingIntent.getActivity(ActivityMain.this, NOTIFY_ID, resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				notificationBuilder.setAutoCancel(true);
				notificationBuilder.setContentIntent(pendingIntent);
			}
			Notification notification = notificationBuilder.build();

			NotificationManager notificationManager = (NotificationManager) ActivityMain.this
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFY_ID, notification);
		}
	}

	private class AppListTask extends AsyncTask<String, Integer, List<ApplicationInfoEx>> {

		private String mRestrictionName;
		private ProgressDialog mProgressDialog;

		@Override
		protected List<ApplicationInfoEx> doInBackground(String... params) {
			mRestrictionName = null;
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);
			return ApplicationInfoEx.getXApplicationList(ActivityMain.this, mProgressDialog);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// Reset spinner
			spRestriction.setEnabled(false);

			// Reset filters
			final ImageView imgUsed = (ImageView) findViewById(R.id.imgUsed);
			imgUsed.setEnabled(false);

			EditText etFilter = (EditText) findViewById(R.id.etFilter);
			etFilter.setEnabled(false);

			CheckBox cbFilter = (CheckBox) findViewById(R.id.cbFilter);
			cbFilter.setEnabled(false);

			// Show progress dialog
			ListView lvApp = (ListView) findViewById(R.id.lvApp);
			mProgressDialog = new ProgressDialog(lvApp.getContext());
			mProgressDialog.setMessage(getString(R.string.msg_loading));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(List<ApplicationInfoEx> listApp) {
			super.onPostExecute(listApp);

			// Display app list
			mAppAdapter = new AppListAdapter(ActivityMain.this, R.layout.mainentry, listApp, mRestrictionName);
			ListView lvApp = (ListView) findViewById(R.id.lvApp);
			lvApp.setAdapter(mAppAdapter);

			// Dismiss progress dialog
			try {
				mProgressDialog.dismiss();
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}

			// Enable filters
			final ImageView imgUsed = (ImageView) findViewById(R.id.imgUsed);
			imgUsed.setEnabled(true);

			EditText etFilter = (EditText) findViewById(R.id.etFilter);
			etFilter.setEnabled(true);

			CheckBox cbFilter = (CheckBox) findViewById(R.id.cbFilter);
			cbFilter.setEnabled(true);

			// Enable spinner
			Spinner spRestriction = (Spinner) findViewById(R.id.spRestriction);
			spRestriction.setEnabled(true);

			// Restore state
			ActivityMain.this.selectRestriction(spRestriction.getSelectedItemPosition());
		}
	}

	@SuppressLint("DefaultLocale")
	private class AppListAdapter extends ArrayAdapter<ApplicationInfoEx> implements SectionIndexer {

		private Context mContext;
		private List<ApplicationInfoEx> mListAppAll;
		private List<ApplicationInfoEx> mListAppSelected;
		private String mRestrictionName;
		private Map<String, Integer> mAlphaIndexer;
		private String[] mSections;

		public AppListAdapter(Context context, int resource, List<ApplicationInfoEx> objects,
				String initialRestrictionName) {
			super(context, resource, objects);
			mContext = context;
			mListAppAll = new ArrayList<ApplicationInfoEx>();
			mListAppAll.addAll(objects);
			mRestrictionName = initialRestrictionName;
			selectApps();
			reindexSections();
		}

		public void setRestrictionName(String restrictionName) {
			mRestrictionName = restrictionName;
			selectApps();
			notifyDataSetChanged();
		}

		public String getRestrictionName() {
			return mRestrictionName;
		}

		private void selectApps() {
			mListAppSelected = new ArrayList<ApplicationInfoEx>();
			if (PrivacyManager.getSettingBool(null, ActivityMain.this, PrivacyManager.cSettingFPermission, true, false)) {
				for (ApplicationInfoEx appInfo : mListAppAll)
					if (mRestrictionName == null)
						mListAppSelected.add(appInfo);
					else if (PrivacyManager.hasPermission(mContext, appInfo.getPackageName(), mRestrictionName))
						mListAppSelected.add(appInfo);
			} else
				mListAppSelected.addAll(mListAppAll);
		}

		@Override
		public Filter getFilter() {
			return new AppFilter();
		}

		private class AppFilter extends Filter {
			public AppFilter() {
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();

				// Get arguments
				String[] components = ((String) constraint).split("\\n");
				boolean fUsed = Boolean.parseBoolean(components[0]);
				String fName = components[1];
				boolean fRestricted = Boolean.parseBoolean(components[2]);

				// Match applications
				List<ApplicationInfoEx> lstApp = new ArrayList<ApplicationInfoEx>();
				for (ApplicationInfoEx xAppInfo : AppListAdapter.this.mListAppSelected) {
					// Get used
					boolean used = false;
					if (fUsed)
						used = (PrivacyManager.getUsed(getApplicationContext(), xAppInfo.getUid(), mRestrictionName,
								null) != 0);

					// Get contains
					boolean contains = false;
					if (!fName.equals(""))
						contains = (xAppInfo.toString().toLowerCase().contains(((String) fName).toLowerCase()));

					// Get some restricted
					boolean someRestricted = false;
					if (fRestricted)
						if (mRestrictionName == null) {
							for (boolean restricted : PrivacyManager.getRestricted(getApplicationContext(),
									xAppInfo.getUid(), false))
								if (restricted) {
									someRestricted = true;
									break;
								}
						} else
							someRestricted = PrivacyManager.getRestricted(null, getApplicationContext(),
									xAppInfo.getUid(), mRestrictionName, null, false, false);

					// Match application
					if ((fUsed ? used : true) && (fName.equals("") ? true : contains)
							&& (fRestricted ? someRestricted : true))
						lstApp.add(xAppInfo);
				}

				synchronized (this) {
					results.values = lstApp;
					results.count = lstApp.size();
				}

				return results;
			}

			@Override
			@SuppressWarnings("unchecked")
			protected void publishResults(CharSequence constraint, FilterResults results) {
				clear();
				if (results.values == null)
					notifyDataSetInvalidated();
				else {
					addAll((ArrayList<ApplicationInfoEx>) results.values);
					notifyDataSetChanged();
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.mainentry, parent, false);
			ImageView imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
			ImageView imgUsed = (ImageView) row.findViewById(R.id.imgUsed);
			ImageView imgGranted = (ImageView) row.findViewById(R.id.imgGranted);
			ImageView imgInternet = (ImageView) row.findViewById(R.id.imgInternet);
			final CheckedTextView ctvApp = (CheckedTextView) row.findViewById(R.id.ctvName);

			// Get entry
			final ApplicationInfoEx xAppInfo = getItem(position);

			// Set background color
			if (xAppInfo.getIsSystem())
				row.setBackgroundColor(getResources().getColor(getThemed(R.attr.color_dangerous)));

			// Set icon
			imgIcon.setImageDrawable(xAppInfo.getDrawable());

			// Handle details click
			imgIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intentSettings = new Intent(view.getContext(), ActivityApp.class);
					intentSettings.putExtra(ActivityApp.cPackageName, xAppInfo.getPackageName());
					intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					view.getContext().startActivity(intentSettings);
				}
			});

			// Set title
			ctvApp.setText(xAppInfo.toString());

			// Check if used
			boolean used = (PrivacyManager.getUsed(row.getContext(), xAppInfo.getUid(), mRestrictionName, null) != 0);
			ctvApp.setTypeface(null, used ? Typeface.BOLD_ITALIC : Typeface.NORMAL);
			imgUsed.setVisibility(used ? View.VISIBLE : View.INVISIBLE);

			// Check if permission
			if (mRestrictionName != null)
				if (!PrivacyManager.hasPermission(row.getContext(), xAppInfo.getPackageName(), mRestrictionName))
					imgGranted.setVisibility(View.INVISIBLE);

			// Check if internet access
			imgInternet.setVisibility(xAppInfo.hasInternet() ? View.VISIBLE : View.INVISIBLE);

			// Get restrictions
			final List<String> listRestriction;
			if (mRestrictionName == null)
				listRestriction = PrivacyManager.getRestrictions(false);
			else {
				listRestriction = new ArrayList<String>();
				listRestriction.add(mRestrictionName);
			}

			// Get all/some restricted
			boolean allRestricted = true;
			boolean someRestricted = false;
			if (mRestrictionName == null)
				for (boolean restricted : PrivacyManager.getRestricted(row.getContext(), xAppInfo.getUid(), false)) {
					allRestricted = allRestricted && restricted;
					someRestricted = someRestricted || restricted;
				}
			else {
				boolean restricted = PrivacyManager.getRestricted(null, row.getContext(), xAppInfo.getUid(),
						mRestrictionName, null, false, false);
				allRestricted = restricted;
				someRestricted = restricted;
			}

			// Display restriction
			ctvApp.setChecked(allRestricted);
			ctvApp.setEnabled(mRestrictionName == null && someRestricted ? allRestricted : true);

			// Listen for restriction changes
			ctvApp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// Get all/some restricted
					boolean allRestricted = true;
					if (mRestrictionName == null)
						for (boolean restricted : PrivacyManager.getRestricted(view.getContext(), xAppInfo.getUid(),
								false))
							allRestricted = allRestricted && restricted;
					else {
						boolean restricted = PrivacyManager.getRestricted(null, view.getContext(), xAppInfo.getUid(),
								mRestrictionName, null, false, false);
						allRestricted = restricted;
					}

					// Process click
					allRestricted = !allRestricted;
					for (String restrictionName : listRestriction)
						PrivacyManager.setRestricted(null, view.getContext(), xAppInfo.getUid(), restrictionName, null,
								allRestricted);
					ctvApp.setChecked(allRestricted);
				}
			});

			// Refresh
			row.refreshDrawableState();
			return row;
		}

		@Override
		public int getPositionForSection(int section) {
			if (section >= mSections.length)
				return super.getCount() - 1;

			return mAlphaIndexer.get(mSections[section]);
		}

		@Override
		public int getSectionForPosition(int position) {
			// Iterate over the sections to find the closest index
			// that is not greater than the position
			int closestIndex = 0;
			int latestDelta = Integer.MAX_VALUE;

			for (int i = 0; i < mSections.length; i++) {
				int current = mAlphaIndexer.get(mSections[i]);
				if (current == position) {
					// If position matches an index, return it immediately
					return i;
				} else if (current < position) {
					// Check if this is closer than the last index we inspected
					int delta = position - current;
					if (delta < latestDelta) {
						closestIndex = i;
						latestDelta = delta;
					}
				}
			}

			return closestIndex;
		}

		@Override
		public Object[] getSections() {
			return mSections;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			reindexSections();
		}

		private void reindexSections() {
			mAlphaIndexer = new HashMap<String, Integer>();
			for (int i = getCount() - 1; i >= 0; i--) {
				ApplicationInfoEx app = getItem(i);
				String appName = app.toString();
				String firstChar;
				if (appName == null || appName.length() < 1) {
					firstChar = "@";
				} else {
					firstChar = appName.substring(0, 1).toUpperCase();
					if (firstChar.charAt(0) > 'Z' || firstChar.charAt(0) < 'A')
						firstChar = "@";
				}

				mAlphaIndexer.put(firstChar, i);
			}

			// create a list from the set to sort
			List<String> sectionList = new ArrayList<String>(mAlphaIndexer.keySet());
			Collections.sort(sectionList);

			mSections = new String[sectionList.size()];
			sectionList.toArray(mSections);
		}
	}

	public int getThemed(int attr) {
		TypedValue typedvalueattr = new TypedValue();
		getTheme().resolveAttribute(attr, typedvalueattr, true);
		return typedvalueattr.resourceId;
	}
}

