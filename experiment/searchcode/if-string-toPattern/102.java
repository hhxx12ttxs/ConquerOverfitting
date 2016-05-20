package luminous.unitconverter.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import luminous.unitconverter.objects.MyAreaUnitConverter.MyUnit;
import luminous.unitconverter.objects.MyForceUnitConverter;
import luminous.unitconverter.objects.MyLengthUnitConverter;
import luminous.unitconverter.objects.MyMassUnitConverter;
import luminous.unitconverter.objects.MyPressureUnitConverter;
import luminous.unitconverter.objects.MySpeedUnitConverter;
import luminous.unitconverter.objects.MyStorageUnitConverter;
import luminous.unitconverter.objects.MyTempUnitConverter;
import luminous.unitconverter.objects.MyTimeUnitConverter;
import luminous.unitconverter.objects.MyVolumeUnitConverter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Utils {

	public static void setPref(Context c, String pref, String val) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
		e.putString(pref, val);
		e.commit();

	}

	public static String getPref(Context c, String pref, String val) {
		return PreferenceManager.getDefaultSharedPreferences(c).getString(pref,
				val);
	}

	public static void setPref(Context c, String pref, boolean val) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
		e.putBoolean(pref, val);
		e.commit();

	}

	public static boolean getPref(Context c, String pref, boolean val) {
		return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(
				pref, val);
	}

	public static void delPref(Context c, String pref) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
		e.remove(pref);
		e.commit();
	}

	public static void setPref(Context c, String pref, int val) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
		e.putInt(pref, val);
		e.commit();

	}

	public static int getPref(Context c, String pref, int val) {
		return PreferenceManager.getDefaultSharedPreferences(c).getInt(pref,
				val);
	}

	public static void setPref(Context c, String pref, long val) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(c).edit();
		e.putLong(pref, val);
		e.commit();
	}

	public static long getPref(Context c, String pref, long val) {
		return PreferenceManager.getDefaultSharedPreferences(c).getLong(pref,
				val);
	}

	public static Typeface getNormal(Context c) {
		return Typeface.createFromAsset(c.getAssets(), "MyriadPro-Regular.otf");
	}

	public static Typeface getBold(Context c) {
		return Typeface
				.createFromAsset(c.getAssets(), "MyriadPro-Semibold.otf");
	}

	public static boolean isInternetConnected(Context mContext) {
		boolean outcome = false;

		try {
			if (mContext != null) {
				ConnectivityManager cm = (ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

				for (NetworkInfo tempNetworkInfo : networkInfos) {

					if (tempNetworkInfo.isConnected()) {
						outcome = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outcome;
	}

	public static int getDeviceWidth(Context context) {
		try {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			return metrics.widthPixels;
		} catch (Exception e) {
			Utils.sendExceptionReport(e);
		}

		return 480;
	}

	public static int getDeviceWidth(Context context, int percentage) {
		int width = getDeviceWidth(context);
		float actualWidth = (width * percentage) / 100;

		return (int) actualWidth;
	}

	public static int getDeviceHeight(Context context) {
		try {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			return metrics.heightPixels;
		} catch (Exception e) {
			Utils.sendExceptionReport(e);
		}

		return 800;
	}

	public static void sendExceptionReport(Exception e) {
		e.printStackTrace();

		try {
			// Writer result = new StringWriter();
			// PrintWriter printWriter = new PrintWriter(result);
			// e.printStackTrace(printWriter);
			// String stacktrace = result.toString();
			// new CustomExceptionHandler(c, URLs.URL_STACKTRACE)
			// .sendToServer(stacktrace);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public static String getAndroidId(Context c) {
		String aid;
		try {
			aid = "";
			aid = Settings.Secure.getString(c.getContentResolver(),
					"android_id");

			if (aid == null) {
				aid = "No DeviceId";
			} else if (aid.length() <= 0) {
				aid = "No DeviceId";
			}
		} catch (Exception e) {
			e.printStackTrace();
			aid = "No DeviceId";
		}

		// Debug.e("", "aid : " + aid);

		return aid;

	}

	public static String getAppVersion(Context c) {
		try {
			return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "Undefined";

	}

	public static void startActivity(Activity activity, Intent i) {
		activity.startActivity(i);
	}

	public static void startActivityForResult(Activity activity, Intent i,
			int requestCode) {
		activity.startActivityForResult(i, requestCode);
	}

	public static String parseCalendarFormat(Calendar c, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern,
				Locale.getDefault());
		return sdf.format(c.getTime());
	}

	public static String parseTime(long time, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern,
				Locale.getDefault());
		return sdf.format(new Date(time));
	}

	public static Date parseTime(String time, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern,
				Locale.getDefault());
		try {
			return sdf.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return new Date();
	}

	public static String parseTime(String time, String fromPattern,
			String toPattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(fromPattern,
				Locale.getDefault());
		try {
			Date d = sdf.parse(time);
			sdf = new SimpleDateFormat(toPattern, Locale.getDefault());
			return sdf.format(d);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	// Area
	public static ArrayList<String> getBlackListUnitArea(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_AREA, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderArea(Context mContext,
			ArrayList<MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_AREA, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderArea(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_AREA, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitArea(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_AREA, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitArea(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_AREA, "");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Length
	public static ArrayList<String> getBlackListUnitLength(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_LENGTH, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderLength(Context mContext,
			ArrayList<MyLengthUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_LENGTH, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderLength(Context context) {
		String orderStr = Utils
				.getPref(context, Constant.UNIT_ORDER_LENGTH, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitLength(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_LENGTH, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitLength(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_LENGTH,
				"");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Mass
	public static ArrayList<String> getBlackListUnitMass(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_MASS, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderMass(Context mContext,
			ArrayList<MyMassUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_MASS, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderMass(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_MASS, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitMass(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_MASS, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitMass(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_MASS, "");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Temperature
	public static ArrayList<String> getBlackListUnitTemp(Context context) {
		String fav = Utils.getPref(context,
				Constant.FAVOURITE_UNIT_TEMPERATURE, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderTemp(Context mContext,
			ArrayList<MyTempUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_TEMPERATURE, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderTemp(Context context) {
		String orderStr = Utils.getPref(context,
				Constant.UNIT_ORDER_TEMPERATURE, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitTemp(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_TEMPERATURE, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitTemp(Context context) {
		String orderStr = Utils.getPref(context,
				Constant.CUSTOM_UNIT_TEMPERATURE, "");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Volume
	public static ArrayList<String> getBlackListUnitVol(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_VOLUME, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderVol(Context mContext,
			ArrayList<MyVolumeUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_VOLUME, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderVol(Context context) {
		String orderStr = Utils
				.getPref(context, Constant.UNIT_ORDER_VOLUME, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitVol(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_VOLUME, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitVol(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_VOLUME,
				"");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Pressure
	public static ArrayList<String> getBlackListUnitPressure(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_PRESSURE,
				"");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderPressure(Context mContext,
			ArrayList<MyPressureUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_PRESSURE, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderPressure(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_PRESSURE,
				"");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitPressure(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_PRESSURE, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitPressure(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_PRESSURE,
				"");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Storage
	public static ArrayList<String> getBlackListUnitStorage(Context context) {
		String fav = Utils
				.getPref(context, Constant.FAVOURITE_UNIT_STORAGE, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderStorage(Context mContext,
			ArrayList<MyStorageUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_STORAGE, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderStorage(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_STORAGE,
				"");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitStorage(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_STORAGE, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitStorage(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_STORAGE,
				"");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Force
	public static ArrayList<String> getBlackListUnitForce(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_FORCE, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderForce(Context mContext,
			ArrayList<MyForceUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_FORCE, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderForce(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_FORCE, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitForce(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_FORCE, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitForce(Context context) {
		String orderStr = Utils
				.getPref(context, Constant.CUSTOM_UNIT_FORCE, "");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Time
	public static ArrayList<String> getBlackListUnitTime(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_TIME, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderTime(Context mContext,
			ArrayList<MyTimeUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_TIME, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderTime(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_TIME, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitTime(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_TIME, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitTime(Context context) {
		String orderStr = Utils.getPref(context, Constant.CUSTOM_UNIT_TIME, "");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}

	// Speed
	public static ArrayList<String> getBlackListUnitSpeed(Context context) {
		String fav = Utils.getPref(context, Constant.FAVOURITE_UNIT_SPEED, "");

		ArrayList<String> blackList = new ArrayList<String>();
		if (fav.length() > 0) {
			ArrayList<String> tmp = new Gson().fromJson(fav,
					new TypeToken<ArrayList<String>>() {
					}.getType());
			blackList.addAll(tmp);
		}

		return blackList;
	}

	public static void saveUnitOrderSpeed(Context mContext,
			ArrayList<MySpeedUnitConverter.MyUnit> data) {
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		for (int i = 0; i < data.size(); i++) {
			unitOrderList.put(data.get(i).name, i);
		}

		String unitStr = new Gson().toJson(unitOrderList,
				new TypeToken<HashMap<String, Integer>>() {
				}.getType());
		Utils.setPref(mContext, Constant.UNIT_ORDER_SPEED, unitStr);
	}

	public static HashMap<String, Integer> getUnitOrderSpeed(Context context) {
		String orderStr = Utils.getPref(context, Constant.UNIT_ORDER_SPEED, "");
		HashMap<String, Integer> unitOrderList = new HashMap<String, Integer>();

		if (orderStr.length() > 0) {
			HashMap<String, Integer> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Integer>>() {
					}.getType());
			unitOrderList.putAll(tmp);
		}

		return unitOrderList;
	}

	public static void saveCustomUnitSpeed(Context mContext,
			HashMap<String, Double> customUnit) {

		String unitStr = new Gson().toJson(customUnit,
				new TypeToken<HashMap<String, Double>>() {
				}.getType());
		Utils.setPref(mContext, Constant.CUSTOM_UNIT_SPEED, unitStr);
	}

	public static HashMap<String, Double> getCustomUnitSpeed(Context context) {
		String orderStr = Utils
				.getPref(context, Constant.CUSTOM_UNIT_SPEED, "");
		HashMap<String, Double> customUnitList = new HashMap<String, Double>();

		if (orderStr.length() > 0) {
			HashMap<String, Double> tmp = new Gson().fromJson(orderStr,
					new TypeToken<HashMap<String, Double>>() {
					}.getType());
			customUnitList.putAll(tmp);
		}

		return customUnitList;
	}
}

