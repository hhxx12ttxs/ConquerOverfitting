public abstract class AbstractInstant implements ReadableInstant {

public abstract long getMillis();


/**
* Compares this object with the specified object for ascending
public boolean isEqual(ReadableInstant instant) {
long instantMillis = DateTimeUtils.getInstantMillis(instant);
return isEqual(instantMillis);

