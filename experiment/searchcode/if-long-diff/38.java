public static String getTimeDifferential(long timestamp) {
long now = Calendar.getInstance().getTimeInMillis();
long diff = (now - timestamp) / 1000;
if (diff < 0) {
return &quot;time leak&quot;;
} else if (diff < 60) {

