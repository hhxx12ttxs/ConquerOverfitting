public static void main(final String[] args) {
int answer = 0;
main: for (int i = 11; i < 1000000; i++) {
String tmp = i + &quot;&quot;;
if (tmp.contains(&quot;0&quot;) || tmp.contains(&quot;4&quot;) || tmp.contains(&quot;6&quot;) || tmp.contains(&quot;8&quot;) || tmp.startsWith(&quot;1&quot;) || tmp.endsWith(&quot;1&quot;) || tmp.substring(1, tmp.length() - 1).contains(&quot;2&quot;) || tmp.substring(1, tmp.length() - 1).contains(&quot;5&quot;)) {

