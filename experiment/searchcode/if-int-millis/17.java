public final static long HOUR_MILLIS = HOUR_SECONDS * HOUR_MINUTES;
/**
* 一天包含的小时数
*/
public final static int DAY_HOURS = 24;
public static Duration createDays(int days) {
return new Duration(Duration.DAY_MILLIS * days);

