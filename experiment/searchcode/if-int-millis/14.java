* @param days 天数
*/
public static Duration createDays(int days) {
return new Duration(Duration.DAY_MILLIS * days);
public static Duration createWeeks(int weeks) {
return new Duration(Duration.WEEK_MILLIS * weeks);

