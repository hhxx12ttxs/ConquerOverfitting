public final class UpdateTimeStamp {

private final long millis;

private UpdateTimeStamp(long millis) {
this.millis = millis;
}

public static UpdateTimeStamp from(long millis) {
return new UpdateTimeStamp(millis);

