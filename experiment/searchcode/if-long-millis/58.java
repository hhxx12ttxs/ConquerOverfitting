private final long millis;

private RefreshTimestamp(long millis) {
this.millis = millis;
}

public static RefreshTimestamp from(long millis) {
return new RefreshTimestamp(millis);

