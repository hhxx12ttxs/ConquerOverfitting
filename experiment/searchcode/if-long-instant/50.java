public boolean isBefore(long instant) {
return (getMillis() < instant);
}
public boolean isBefore(ReadableInstant instant) {
long instantMillis = DateTimeUtils.getInstantMillis(instant);

