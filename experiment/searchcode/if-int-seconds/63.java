public class MusicPlayerUtils {

private static final int SECONDS_PER_MINUTE = 60;
private static final int MINUTES_PER_HOUR = 60;

public static String secondsToFormattedString(int numberOfSeconds) {
int seconds = numberOfSeconds % SECONDS_PER_MINUTE;

