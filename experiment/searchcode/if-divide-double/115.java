package com.ebay.nearby.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ebay.nearby.database.entity.Location;

public class Geo {
	final static String urlStr = "http://maps.google.com/maps/geo?q=";
	final static String param = "&output=json&key=";

	public static Double[] getLatLongFromAddress(String address)
			throws IOException {
		address = URLEncoder.encode(address, "UTF-8");
		BufferedReader in = null;
		InputStream input = null;
		Double[] result = new Double[2];
		if (address == null || address.isEmpty()) {
			return null;
		}
		try {
			URL url = new URL(urlStr + address + param);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			input = connection.getInputStream();
			in = new BufferedReader(new InputStreamReader(
					input,"utf-8"));
			String line = null;
			String response = "";
			while ((line = in.readLine()) != null) {
				response += line + "\n";
			}
			JSONObject responseJson = JSONObject.fromObject(response);
			JSONArray placeMarkArray = JSONArray.fromObject(responseJson
					.get("Placemark"));
			// get the first mark if have two more same marks
			JSONObject placeMark = placeMarkArray.getJSONObject(0);
			JSONObject point = (JSONObject) placeMark.get("Point");
			JSONArray coordinates = (JSONArray) point.get("coordinates");
			result[0] = (Double) coordinates.get(1); // latitude
			result[1] = (Double) coordinates.get(0); // longitude
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (input != null) {
					input.close();
				}
				
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
//		result[0] = 31.2122530;
//		result[1] = 121.6117730;
		return result;
	}

//	public static double calDistance(String[] lalong1, String lalong2) {
//
//		return 0;
//	}
//
//	public static Double[] findLatLongByDistance(Double[] LatLong,
//			Double distance) {
//		return null;
//	}

	/*
	 * convert lat, lag to Bounds Object
	 * 
	 * @param r radius (m) 1000表示1公里，111表示同经度时，纬度相差一度，距离就相差111公里
	 * @param lat latitude 纬度
	 * @param lag longitude 经度
	 */
	public static Bound conversion(Double lat, Double lag, Integer r) {
		String l = String.valueOf(1000 * 111);
		String latx = new BigDecimal(String.valueOf(r)).divide(
				new BigDecimal(l), 4, BigDecimal.ROUND_HALF_EVEN).toString();
		String lagx = new BigDecimal(latx).divide(
				new BigDecimal(String.valueOf(Math.cos(lat))), 4,
				BigDecimal.ROUND_HALF_EVEN).toString();
		Double latN = lat + Math.abs(Double.valueOf(latx));
		Double latS = lat - Math.abs(Double.valueOf(latx));
		Double lagE = lag + Math.abs(Double.valueOf(lagx));
		Double lagW = lag - Math.abs(Double.valueOf(lagx));
		Bound bound = new Bound();
		bound.setLagE(lagE);
		bound.setLagW(lagW);
		bound.setLatN(latN);
		bound.setLatS(latS);
		return bound;
	}
	
	/*
	 * check the location in bounds is in the circle of the radius r
	 */
	public static Boolean check(Location location, Double lat, Double lag,
			Integer r) {
		double R = 6371;// earth radius
		double distance = 0.0;
		double dLat = Double.valueOf(new BigDecimal(String.valueOf((location
				.getLatitude() - lat)))
				.multiply(new BigDecimal(String.valueOf(Math.PI)))
				.divide(new BigDecimal(String.valueOf(180)), 4,
						BigDecimal.ROUND_HALF_EVEN).toString());
		double dLon = Double.valueOf(new BigDecimal(String.valueOf((location
				.getLongitude() - lag)))
				.multiply(new BigDecimal(String.valueOf(Math.PI)))
				.divide(new BigDecimal(String.valueOf(180)), 4,
						BigDecimal.ROUND_HALF_EVEN).toString());
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(location.getLatitude() * Math.PI / 180)
				* Math.cos(lat * Math.PI / 180) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		distance = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))) * R * 1000;
		System.out.println(distance);
		if (distance > Double.valueOf(String.valueOf(r))) {
			return false;
		}
		return true;
	}
}

