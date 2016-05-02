package com.eagleminer.client.map;

import java.util.Arrays;
import java.util.Comparator;

import com.eagleminer.shared.objects.Location;

public class TenementContureSorter implements Comparator<Location> {
	private Location inLocation;
	
	public TenementContureSorter() {
	}

	private void findInLocation(Location[] locations) {
		Location first = locations[0];
		
		//
		// Find the longest diagonal in the area.
		//
		Location farLoc = locations[0];
		double far = 0;
		
		for (int k = 0; k < locations.length; k++) {
			Location loc = locations[k];
			
			double dis = first.distance(loc);
			if (far >= dis) continue;
			
			far = dis;
			farLoc = loc;
		}
		
		double lat = (first.lat + farLoc.lat) / 2;
		double lng = (first.lng + farLoc.lng) / 2;
		
		inLocation = new Location(lat, lng);
	}

	@Override
	public int compare(Location loc1, Location loc2) {
		double angle1 = getAngle(loc1);
		double angle2 = getAngle(loc2);
		
		if (angle1 > angle2) return  1;
		if (angle1 < angle2) return -1;
		
		return 0;
	}

	private double getAngle(Location loc) {
		double dLat = loc.lat - inLocation.lat;
		double dLng = loc.lng - inLocation.lng;
		
		double gipo = Math.sqrt(dLat * dLat + dLng * dLng);
		
		double angle = Math.asin(dLat / gipo);
		
		angle = Math.abs(angle);
		
		if ((dLng < 0) && (dLat >= 0))
			angle = Math.PI - angle;
		else
		if ((dLng < 0) && (dLat < 0))
			angle = Math.PI + angle;
		else
		if ((dLng > 0) && (dLat < 0))
			angle = 2*Math.PI - angle;
		
		return angle;
	}
	
	public void sort(Location[] locs) {
		if (locs.length <=3) return;
		
		findInLocation(locs);
		
		printLocations("Before sorting", locs);
		Arrays.sort(locs, this);
		printLocations("After  sorting", locs);
	}

	private void printLocations(String title, Location[] locs) {
		System.out.println(title);
		
		double[] aa = new double[locs.length];
		for(int k = 0; k < aa.length; k++) {
			aa[k] = getAngle(locs[k]);
			double degry = aa[k] * (180 / Math.PI);
			
			String s = "" + k + ". (" + locs[k].lat + "," + locs[k].lng + ") R:" + aa[k] + " D:" + degry;
			System.out.println(s);
		}
		System.out.println();
	}
}

