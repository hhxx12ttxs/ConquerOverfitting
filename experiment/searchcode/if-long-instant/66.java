public static Calendar MakeInstantFromMillis(long millis) {
Calendar instant = Dates.Now(); // just to get our hands on an instant
* @return milliseconds since 1/1/1970.
*/

public static long GetMillis(Calendar instant) {
return instant.getTimeInMillis();

