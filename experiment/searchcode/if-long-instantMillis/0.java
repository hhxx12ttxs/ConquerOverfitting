public boolean isBefore(ReadableInstant instant) {
long instantMillis = DateTimeUtils.getInstantMillis(instant);
return isBefore(instantMillis);
}

public boolean isEqual(long instant) {

