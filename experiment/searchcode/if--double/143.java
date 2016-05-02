package com.atlassian.jconnect.jira.customfields;

import static com.atlassian.jconnect.jira.customfields.GeoCalculator.normalizeLat;
import static com.atlassian.jconnect.jira.customfields.GeoCalculator.normalizeLng;

/**
 * Simple location bean.
 *
 */
public class Location {

    public final double lat;
    public final double lng;

    public Location(String latLong) {
        String[] parts = latLong.split(",");
        lat = Double.parseDouble(parts[0]);
        lng = Double.parseDouble(parts[1]);
    }

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }


    @Override
    public String toString() {
        return String.format("%f,%f", lat, lng);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Location normalize() {
        return new Location(normalizeLat(lat), normalizeLng(lng));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Location location = (Location) o;

        if (Double.compare(location.lat, lat) != 0) { return false; }
        if (Double.compare(location.lng, lng) != 0) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lng != +0.0d ? Double.doubleToLongBits(lng) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
