public static String formatDuration(long sec) {
long min = sec/60;
sec = sec % 60;
long hou = min/60;
min = min % 60;
String ret = &quot;&quot;;
if (hou > 0) {
ret = ret + hou + &quot;小时&quot;;
}
if (min > 0 || ret.length() > 0)
ret = ret + min + &quot;分&quot;;

