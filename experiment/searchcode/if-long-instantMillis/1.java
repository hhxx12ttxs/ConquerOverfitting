public void setMillis(ReadableInstant instant) {
long instantMillis = DateTimeUtils.getInstantMillis(instant);
setMillis(instantMillis);  // set via this class not super
public void setDate(final ReadableInstant instant) {
long instantMillis = DateTimeUtils.getInstantMillis(instant);

