package com.weanticipate.client.android.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public final class LocationUtil {

	public static double gpsToMiles(double lat_a, double lng_a, double lat_b, double lng_b) {
		double pk = (180 / 3.14159);

		double a1 = lat_a / pk;
		double a2 = lng_a / pk;
		double b1 = lat_b / pk;
		double b2 = lng_b / pk;

		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);

		return (6366000 * tt) / 1609.344;
	}

	public static Location getLocation(Context context) {
		LocationManager locManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		Location myLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (myLocation == null) {
			myLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			return myLocation;
		}
		return myLocation;
	}
}

