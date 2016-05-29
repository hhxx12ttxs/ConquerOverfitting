public String format(String dateAsString) {
if (dateAsString == null) {
return null;
}
int strLen = dateAsString.length();
} else if (dateAsString.matches(&quot;.*\\.[0-3]\\d$&quot;)) {
return dateAsString.substring(0, strLen - 2) + &quot;20&quot; + dateAsString.substring(strLen - 2, strLen);

