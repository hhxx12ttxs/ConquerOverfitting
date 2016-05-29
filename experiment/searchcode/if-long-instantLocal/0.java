public IllegalInstantException(long instantLocal, String zoneId) {
super(createMessage(instantLocal, zoneId));
}

private static String createMessage(long instantLocal, String zoneId) {
String localDateTime = DateTimeFormat.forPattern(&quot;yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&quot;).print(new Instant(instantLocal));

