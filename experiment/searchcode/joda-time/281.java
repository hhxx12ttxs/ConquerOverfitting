package lrg.dude.duplication;

/**
 * Created by IntelliJ IDEA.
 * User: Richard
 * Date: 01.05.2004
 * Time: 16:19:56
 * To change this template use File | Settings | File Templates.
 */
public class TimeMeasurer {
    public static String convertTimeToString(long miliseconds) {
        if(miliseconds < 1000)
            return new String("" + miliseconds + "ms");
        final int totalSeconds = (int) (miliseconds / 1000);
        int hours = totalSeconds / 3600;
        int rezidualSeconds = totalSeconds % 3600;
        int minutes = rezidualSeconds / 60;
        int seconds = rezidualSeconds % 60;
        String time = "";
        if (hours > 0) {
            if (hours < 10)
                time = time + "0";
            time = time + hours + "h";
        }
        if (minutes > 0) {
            if (minutes < 10)
                time = time + "0";
            time = time + minutes + "m";
        }
        if (seconds < 10)
            time = time + "0";
        time = time + seconds + "s";
        return time;
    }
}

