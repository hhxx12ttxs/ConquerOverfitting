public static String format(long seconds) {
if (seconds < 60) {
return pluralSeconds(seconds);
}
int minutes = (int) (seconds / 60.0);
return pluralMinutes(minutes) + &quot; and &quot; + pluralSeconds(seconds % 60);

