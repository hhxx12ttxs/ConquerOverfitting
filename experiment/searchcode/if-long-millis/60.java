// deny
}

public static Date toDate(long millis) {
return Date.newBuilder()
public static long fromDate(Date date) {
long millis = 0L;
if (date != null) {
millis = date.getMillis();
}
return millis;
}


}

