package org.sk.maskedpicker;

import java.sql.Time;

/**
 * Class to perform centesimal time calculations. Centesimal times have the
 * hours divided into 100 minutes and the minutes divided into 100 seconds.
 * 
 * @author Mario C??novas
 */
public class CTime {

    /** Hours. */
    private int hours = 0;

    /** Minutes (sexagesimal). */
    private int minutes = 0;

    /** Seconds (sexagesimal). */
    private int seconds = 0;

    /**
     * Default constructor.
     */
    public CTime() {
        super();
    }

    /**
     * Constructor assigning hours, minutes and seconds.
     * 
     * @param h
     *            The number of hours.
     * @param m
     *            The number of minutes.
     * @param s
     *            The number of seconds.
     */
    public CTime(final int h, final int m, final int s) {
        super();
        hours = h;
        minutes = m;
        seconds = s;
    }

    /**
     * Constructor assigning a <code>java.sql.Time</code>.
     * 
     * @param time
     *            A java.sql.Time
     */
    public CTime(final Time time) {
        super();
        Calendar c = new Calendar(time);
        hours = c.getHour();
        minutes = c.getMinute();
        seconds = c.getSecond();
    }

    /**
     * Constructor passing a number of milliseconds.
     * 
     * @param millis
     */
    public CTime(final long millis) {
        super();
        long rest = millis;
        hours = (int) rest / (1000 * 60 * 60);
        rest = rest - (hours * (1000 * 60 * 60));
        minutes = (int) rest / (1000 * 60);
        rest = rest - (minutes * (1000 * 60));
        seconds = (int) rest / (1000);
    }

    /**
     * Construct a CTime passing two timestamps.
     * 
     * @param start
     * @param end
     */
    public CTime(final Timestamp start, final Timestamp end) {
        this(end.getTime() - start.getTime());
    }

    /**
     * Add some <code>java.sql.Time</code> to the current time.
     * 
     * @param time
     *            The java.sql.Time to add.
     * @param amount
     *            The number of times to add.
     */
    public void add(final Time time, final int amount) {
        add(new CTime(time), amount);
    }

    /**
     * Add a <code>CTime</code> to the current time.
     * 
     * @param time
     *            The CTime to add.
     * @param amount
     *            The number of times to add it.
     */
    public void add(final CTime time, final int amount) {
        setCentesimalHours(getCentesimalHoursNoRound()
            + (time.getCentesimalHoursNoRound() * amount));
    }

    /**
     * Add centesimal hours to this CTime.
     * 
     * @param centesimalHours
     *            The number of centesimal hours.
     */
    public void add(final double centesimalHours) {
        setCentesimalHours(getCentesimalHoursNoRound() + centesimalHours);
    }

    /**
     * Return the number of centesimal hours (4 decimals).
     * 
     * @return The number of centesimal hours (4 decimals).
     */
    public double getCentesimalHours() {
        return NumberUtils.round(getCentesimalHoursNoRound(), 4);
    }

    /**
     * Return the number of centesimal hours with nDecimals decimals.
     * 
     * @param nDecimals
     *            The number of decimals.
     * @return The number of centesimal hours with nDecimals decimals.
     */
    public double getCentesimalHours(final int nDecimals) {
        return NumberUtils.round(getCentesimalHoursNoRound(), nDecimals);
    }

    /**
     * Return the number of centesimal hours without rounding in order to
     * perform calculations (add, multiply).
     * 
     * @return The number of centesimal hours.
     */
    public double getCentesimalHoursNoRound() {
        double h = hours;
        double m = minutes;
        double s = seconds;
        return h + (m / 60) + (s / 6000);
    }

    /**
     * Return the number of centesimal minutes (2 decimals).
     * 
     * @return The number of centesimal minutes (2 decimals).
     */
    public double getCentesimalMinutes() {
        return NumberUtils.round(getCentesimalMinutesNoRound(), 2);
    }

    /**
     * Return the number of centesimal minutes without rounding.
     * 
     * @return The number of centesimal minutes without rounding.
     */
    public double getCentesimalMinutesNoRound() {
        double h = hours;
        double m = minutes;
        double s = seconds;
        return (h * 100) + (m * 100 / 60) + (s * 100 / 6000);
    }

    /**
     * Return the number of hours.
     * 
     * @return The number of hours.
     */
    public int getHours() {
        return hours;
    }

    /**
     * return the number of minutes.
     * 
     * @return The number of minutes.
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Return the number of seconds.
     * 
     * @return The number of seconds.
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Return a <code>java.sql.Time</code>.
     * 
     * @return A java.sql.Time
     */
    public Time getTime() {
        Calendar c = new Calendar();
        c.setHour(0);
        c.setMinute(0);
        c.setSecond(0);
        c.add(this, 1);
        return new Time(c.getTimeInMillis());
    }

    /**
     * Set the number of centesimal hours.
     * 
     * @param v
     *            The number of centesimal hours.
     */
    public void setCentesimalHours(final double v) {
        setCentesimalMinutes(v * 100);
    }

    /**
     * Set the number of centesimal minutes.
     * 
     * @param v
     *            The number of centesimal minutes.
     */
    public void setCentesimalMinutes(double v) {
        // Move arg to centesimal seconds
        v = v * 100;
        // One hour is 10000 centesimal seconds
        double h = Math.floor(v / 10000);
        // Substract hours
        v -= (h * 10000);
        // Minutes
        double m = Math.floor(v * 60 / 10000);
        // substract minutes
        v -= (m * 10000 / 60);
        // Seconds
        double s = NumberUtils.round(v * 3600 / 10000, 0);
        // Substract seconds
        v -= (s * 10000 / 3600);
        // Assing h/m/s
        hours = (int) h;
        minutes = (int) m;
        seconds = (int) s;

        // Rest of seconds
        if (seconds == 60) {
            seconds = 0;
            minutes++;
        }
        if (minutes == 60) {
            minutes = 0;
            hours++;
        }

    }

    /**
     * Set the number of hours.
     * 
     * @param hours
     *            The number of hours.
     */
    public void setHours(final int hours) {
        this.hours = hours;
    }

    /**
     * Set the number of minutes.
     * 
     * @param minutes
     *            The number of minutes.
     */
    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }

    /**
     * Set the number of seconds.
     * 
     * @param seconds
     *            The number of seconds.
     */
    public void setSeconds(final int seconds) {
        this.seconds = seconds;
    }

    /**
     * Returns a string representation of this time.
     * 
     * @return A string representation of this time.
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Returns a string representation of this time.
     * 
     * @return A string representation of this time.
     * @param showSeconds
     *            A boolean indicating if seconds should be shown.
     */
    public String toString(final boolean showSeconds) {
        String sHour = Integer.toString(getHours());
        String sMinute = Integer.toString(getMinutes());
        String sSecond = Integer.toString(getSeconds());
        sMinute = StringUtils.leftPad(sMinute, 2, '0');
        sSecond = StringUtils.leftPad(sSecond, 2, '0');
        String string = sHour + ":" + sMinute;
        if (showSeconds) {
            string += ":" + sSecond;
        }
        return string;
    }
}

