public static LocalDateTime parse(String text) {
if (StringUtils.isEmpty(text)) {
return null;
}
LocalDateTime time;
String mathString;
if (text.startsWith(&quot;now&quot;)) {
time = LocalDateTime.now();

