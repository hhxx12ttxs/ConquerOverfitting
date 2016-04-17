package com.number26.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Rory_McCormack on 14/02/16.
 *
 * Data model representing a BarzahlenLocation returned from the Barzahlen Api.
 */
public class BarzahlenLocation {

    @SerializedName("id")
    private int id;
    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;
    @SerializedName("title")
    private String title;
    @SerializedName("street_no")
    private String address;
    @SerializedName("logo_url")
    private String logoUrl;
    @SerializedName("opening_hours")
    private List<OpeningHours> openingHours;

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    public String getMoFrHours() {
        OpeningHours hours = openingHours.get(0);
        String hoursString = null;
        if (hours != null) {
            hoursString = hours.getDays() + " " + hours.getTime();
        }

        return hoursString;
    }

    public String getSaHours() {
        String hoursString = null;
        if (openingHours.size() > 1) {
            OpeningHours hours = openingHours.get(1);
            hoursString = hours.getDays() + " " + hours.getTime();
        }
        return hoursString;
    }

    @Override
    public String toString() {
        return "BarzahlenLocation{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", title='" + title + '\'' +
                ", address='" + address + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", openingHours=" + openingHours +
                '}';
    }
}

