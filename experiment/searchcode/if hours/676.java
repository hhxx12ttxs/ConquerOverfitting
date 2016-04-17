package andela.checkpoint.stilld.utilities;


/**
 * A simple representation of a time duration.
 * For example 1 minute; 2 hours; 2 hours, 45 minutes; ...
 */
public class Duration {
    public final static String NO_DURATION = "0";

    public final static String NO_DURATION_READABLE = "No duration";

    /**
     * The 'hours' part of this duration.
     */
    private int hours;

    /**
     * The 'minutes' part of this duration.
     */
    private int minutes;

    /**
     * Creates a new instance of the {@link Duration} class.
     */
    public Duration() {
    }

    /**
     * Creates a new instance of the {@link Duration} class.
     * @param hours the 'hours' part of the created duration.
     */
    public Duration(int hours) {
        this();

        this.hours = hours;
    }

    /**
     * Creates a new instance of the {@link Duration} class.
     * @param hours the 'hours' part of the created duration.
     * @param minutes the 'minutes' part of the created duration.
     */
    public Duration(int hours, int minutes) {
        this(hours);

        this.minutes = minutes;
    }

    /**
     * Converts this duration to milliseconds.
     * @return a {@code long} corresponding to this duration in milliseconds.
     */
    public long getMillis() {
        return (hours * 3600 + minutes * 60) * 1000;
    }

    /**
     * Reads a duration from a string formatted as 'hours':'minutes'.
     * If the string does not represent a valid duration, returns a
     * duration choosed arbitrarily (currently 5 minutes).
     * @param s the {@link String} to parse.
     * @return a {@link Duration}.
     */
    public static Duration parse(String s) {
        String[] parts = s.split(":");

        if (parts.length < 2) {
            return new Duration(0, 5);
        }

        try {
            return new Duration(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return new Duration(0, 5);
        }
    }

    /**
     * Converts this duration to a human readable string.
     * For example 4 hours or 2 minutes 10 seconds.
     * @return a human readable {@link String}.
     */
    public String toHumanReadableString() {
        if (hours <= 0 && minutes <= 0) {
            return NO_DURATION_READABLE;
        }

        if (hours <= 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        }

        if (minutes <= 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        }

        return hours + " hour" + (hours > 1 ? "s" : "")
                + ", " + minutes + " minute" + (minutes > 1 ? "s" : "");
    }

    @Override
    public String toString() {
        return (hours <= 0 && minutes <= 0) ? NO_DURATION : hours + ":" + minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}

