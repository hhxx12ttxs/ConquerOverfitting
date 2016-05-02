package com.aluen.tracerecorder.util;
import com.google.android.gms.maps.model.LatLng;

public class Transform {
	public final static double pi = 3.14159265358979324;
	public final static double a = 6378245.0;
	public final static double ee = 0.00669342162296594323;

	/**
	 * transform World Geodetic System to Mars Geodetic System
	 * @param wgLat double in WGS-84 format
	 * @param wgLon double in WGS-84 format
	 * @param mgLat double in GC-J02 format
	 * @param mgLon double in GC-J02 format
	 */
	public static void transform(double wgLat, double wgLon, double mgLat,
			double mgLon) {
		if (outOfChina(wgLat, wgLon)) {
			mgLat = wgLat;
			mgLon = wgLon;
			return;
		}
		double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		mgLat = wgLat + dLat;
		mgLon = wgLon + dLon;
	}

	/**Transform the LatLng form World Geodetic System to  Mars Geodetic System
	 * @param wgLat double
	 * @param wgLon double
	 * @return Transformed LatLng in  Mars Geodetic System format.
	 */
	public static LatLng transformLatLng(double wgLat, double wgLon) {
		LatLng latlng ;
		if (outOfChina(wgLat, wgLon)) {
			latlng = new LatLng(wgLat, wgLon);	
			return latlng;
		}
		double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = wgLat + dLat;
		double mgLon = wgLon + dLon;
		latlng = new LatLng(mgLat, mgLon);	
		return latlng;
	}
	
	/**Judge if in China.
	 * @param lat double
	 * @param lon double
	 * @return
	 */
	static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	/**
	 * Transform Latitude.
	 * @param x double
	 * @param y double
	 * @return Transformed Latitude.
	 */
	static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	/**
	 * Transform Longitude.
	 * @param x double
	 * @param y double
	 * @return Transformed Longitude.
	 */
	static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}
}

