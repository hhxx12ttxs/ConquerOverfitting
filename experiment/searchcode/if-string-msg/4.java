public static String getMessage(String nameException, String msg) {
String finalMsg = &quot;[&quot; + nameException + &quot; - &quot;;

if (msg != null &amp;&amp; !msg.isEmpty()) {
String[] array = null;
if (msg.contains(&quot;:&quot;)) {
array = msg.split(&quot;:&quot;);

