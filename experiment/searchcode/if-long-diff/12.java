public static long now() {
return System.currentTimeMillis();
}

public static String timeSince(long time) {
long diff = (TimeUtil.now() - time) / 1000;
public static long sleepTime(long time) {
long diff = (TimeUtil.now() - time) / 1000;
if (diff / 60 <= 0) {

