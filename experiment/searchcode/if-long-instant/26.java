this(fixedInstant, ZoneOffset.UTC);
}

public SettableClock(long epochMillis) {
this(Instant.ofEpochMilli(epochMillis));
public void setCurrentTimeMillis(long epochMillis) {
_instant = Instant.ofEpochMilli(epochMillis);

