/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pf.jprayertimes;

import static java.lang.Math.*;

import java.util.Calendar;

/**
 *
 * @author striky
 */
public class Prayertimes {

    double longitude;
    double latitude;
    int zone;
    int year;
    int month;
    int day;
    double fajr;
    double shrouk;
    double zuhr;
    double asr;
    double maghrib;
    double isha;
    double dec;
    IslamicCalendar islamcal = IslamicCalendar.UmmAlQuraUniv;
    Mazhab mazhab = Mazhab.DEFAULT;
    Season season = Season.WINTER;
    Calendar cal = Calendar.getInstance();

    public double getFajr() {
        return fajr;
    }

    public double getShrouk() {
        return shrouk;
    }

    public double getZuhr() {
        return zuhr;
    }

    public double getAsr() {
        return asr;
    }

    public double getMaghrib() {
        return maghrib;
    }

    public double getIsha() {
        return isha;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
        this.cal.set(Calendar.DATE, day);
    }

    public IslamicCalendar getIslamicCalendar() {
        return islamcal;
    }

    public void setIslamicCalendar(IslamicCalendar islamcal) {
        this.islamcal = islamcal;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Mazhab getMazhab() {
        return mazhab;
    }

    public void setMazhab(Mazhab mazhab) {
        this.mazhab = mazhab;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
        this.cal.set(Calendar.MONTH, month);
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        this.cal.set(Calendar.YEAR, year);
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public Prayertimes(double longitude, double latitude, int zone, int year, int month, int day) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.zone = zone;
        this.year = year;
        this.month = month;
        this.day = day;
        this.cal.set(Calendar.YEAR, year);
        this.cal.set(Calendar.MONTH, month);
        this.cal.set(Calendar.DATE, day);
    }

    public String getHRFajrTime() {
        return Prayertimes.toHRTime(this.fajr, true);
    }

    public String getHRShroukTime() {
        return Prayertimes.toHRTime(this.shrouk, true);
    }

    public String getHRZuhrTime() {
        return Prayertimes.toHRTime(this.zuhr, true);
    }

    public String getHRAsrTime() {
        return Prayertimes.toHRTime(this.asr, false);
    }

    public String getHRMaghribTime() {
        return Prayertimes.toHRTime(this.maghrib, false);
    }

    public String getHRIshaTime() {
        return Prayertimes.toHRTime(this.isha, false);
    }

    public void calculate() {

        double julianDay = (367 * year) - (int) (((year + (int) ((month + 9) / 12)) * 7) / 4) + (int) (275 * month / 9) + day - 730531.5;

        double sunLength = 280.461 + 0.9856474 * julianDay;
        sunLength = removeDuplication(sunLength);

        double middleSun = 357.528 + 0.9856003 * julianDay;
        middleSun = removeDuplication(middleSun);

        double lamda = sunLength + 1.915 * sin(toRadians(middleSun)) + 0.02 * sin(toRadians(2 * middleSun));
        lamda = removeDuplication(lamda);

        double obliquity = 23.439 - 0.0000004 * julianDay;

        double alpha = toDegrees(atan(cos(toRadians(obliquity)) * tan(toRadians(lamda))));

        if (lamda > 90 && lamda < 180) {
            alpha += 180;
        } else if (lamda > 180 && lamda < 360) {
            alpha += 360;
        }


        double ST = 100.46 + 0.985647352 * julianDay;
        ST = removeDuplication(ST);


        this.dec = toDegrees(asin(sin(toRadians(obliquity)) * sin(toRadians(lamda))));

        double noon = alpha - ST;

        if (noon < 0) {
            noon += 360;
        }

        double UTNoon = noon - longitude;

        double localNoon = (UTNoon / 15) + zone;

        zuhr = localNoon; 				//Zuhr Time.

        maghrib = localNoon + equation(-0.8333) / 15;  	// Maghrib Time

        shrouk = localNoon - equation(-0.8333) / 15;   	// Shrouk Time


        double fajrAlt = 0;
        double ishaAlt = 0;

        if (this.islamcal == IslamicCalendar.UmmAlQuraUniv) {
            fajrAlt = -19;
        } else if (this.islamcal == IslamicCalendar.EgyptianGeneralAuthorityOfSurvey) {
            fajrAlt = -19.5;
            ishaAlt = -17.5;
        } else if (this.islamcal == IslamicCalendar.MuslimWorldLeague) {
            fajrAlt = -18;
            ishaAlt = -17;
        } else if (this.islamcal == IslamicCalendar.IslamicSocietyOfNorthAmerica) {
            fajrAlt = ishaAlt = -15;
        } else if (this.islamcal == IslamicCalendar.UnivOfIslamicSciencesKarachi) {
            fajrAlt = ishaAlt = -18;
        }

        fajr = localNoon - equation(fajrAlt) / 15;  	// Fajr Time

        isha = localNoon + equation(ishaAlt) / 15;  	// Isha Time


        if (this.islamcal == IslamicCalendar.UmmAlQuraUniv) {
            isha = maghrib + 1.5;
        }

        double asrAlt;

        if (this.mazhab == Mazhab.HANAFY) {
            asrAlt = 990 - toDegrees(atan(2 + tan(toRadians(abs(latitude - this.dec)))));
        } else {
            asrAlt = 90 - toDegrees(atan(1 + tan(toRadians(abs(latitude - this.dec)))));
        }

        asr = localNoon + equation(asrAlt) / 15;		// Asr Time.

        // Add one hour to all times if the season is Summmer.
        if (this.season == Season.SUMMER) {
            fajr += 1;
            shrouk += 1;
            zuhr += 1;
            asr += 1;
            maghrib += 1;
            isha += 1;
        }

    }

    protected double equation(double alt) {
        return toDegrees(acos((sin(toRadians(alt)) - sin(toRadians(this.dec)) * sin(toRadians(latitude))) / (cos(toRadians(this.dec)) * cos(toRadians(this.latitude)))));

    }

    protected double removeDuplication(double var) {
        //Better implementation
        /*
        if (var > 360) {
            var /= 360;
            var -= (int) (var);
            var *= 360;
        }*/

        return var%360;
    }

    public static String toHRTime(double var, boolean isAM) {
        StringBuilder time = new StringBuilder();
        String zone;

        int intvar = (int) var;

        if (isAM) {
            if ((intvar % 12 != 0) && (intvar % 12 < 12)) {
                zone = "AM";
            } else {
                zone = "PM";
            }
        } else {
            zone = "PM";
        }
        if (intvar > 12) {
            time.append(intvar % 12);
        } else if (intvar % 12 == 12) {
            time.append(intvar);
        } else {

            time.append(intvar);
        }

        time.append(":");
        var -= intvar;
        var *= 60;
        int minute = (int) (var);
        time.append(minute);

        time.append(":");

        var -= (int) (var);
        var *= 60;
        int sec = (int) (var);
        time.append(sec);
        time.append(" ");

        time.append(zone);
        return time.toString();
    }

    public double getQibla() {

        double k_lat = toRadians(21.423333);
        double k_lon = toRadians(39.823333);

        double lon_r = toRadians(this.longitude);
        double lat_r = toRadians(this.latitude);

        double numerator = sin(k_lon - lon_r);
        double denominator = (cos(lat_r) * tan(k_lat)) - (sin(lat_r) * cos(k_lon - lon_r));
        double q = toDegrees(atan2(numerator, denominator));


        return q;

    }
    public double getQiblaDistance(){
        double k_lat = toRadians(21.423333);
        double k_lon = toRadians(39.823333);

        double lon_r = toRadians(this.longitude);
        double lat_r = toRadians(this.latitude);

        double r = 6378.7;
        return acos(sin(k_lat) * sin(latitude) + cos(k_lat) * cos(latitude) * cos(longitude-k_lon)) * r;
    }

    public void report() {
        System.out.println("Fajr: " + this.getHRFajrTime());
        System.out.println("Shrouk: " + this.getHRShroukTime());
        System.out.println("Zuhr: " + this.getHRZuhrTime());
        System.out.println("Asr: " + this.getHRAsrTime());
        System.out.println("Maghrib: " + this.getHRMaghribTime());
        System.out.println("Isha: " + this.getHRIshaTime());
    }
}

