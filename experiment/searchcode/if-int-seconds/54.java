public static Random ran = new Random();

public static String toTime(int seconds) {
int hours = 0;
int minutes = 0;

if (seconds / 3600 > 0) {
hours = seconds / 3600;
seconds -= hours * 3600;

