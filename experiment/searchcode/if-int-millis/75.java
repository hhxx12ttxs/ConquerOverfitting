public class Formatter {

public static String getFormatDuration(double millis) {
int second = (int) (millis / 1000) % 60;
int second = (int) (_millis / 1000) % 60;
int minutes = (int) (_millis / (1000 * 60));

if (visibleMillis) return String.format(&quot;%02d:%02d:%03d&quot;, minutes, second, millis);

