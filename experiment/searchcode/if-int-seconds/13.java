public static String justParsingTheTime(int seconds) {
int minutes = seconds / 60;
seconds     = seconds % 60;

if (seconds < 10) {
return minutes + &quot;:0&quot; + seconds;
} else {

