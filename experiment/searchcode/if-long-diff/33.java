public static String compareDate(long milliseconds1, long milliseconds2) {
StringBuilder stb = new StringBuilder();

long diff = milliseconds2 - milliseconds1;
long diffDays = diff / (24 * 60 * 60 * 1000);
diff = diff - diffHours * (60 * 60 * 1000);
}

long diffMinutes = diff / (60 * 1000);
if(diffMinutes > 0) {

