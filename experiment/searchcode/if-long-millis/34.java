static String humanize(long millis) {

StringBuffer stringBuffer = new StringBuffer();

if (millis > MILLIS_IN_HOUR) {
millis -= hours * MILLIS_IN_HOUR;

}

if (millis > MILLIS_IN_MINUTE) {

// Get minutes
long minutes = millis / MILLIS_IN_MINUTE;

