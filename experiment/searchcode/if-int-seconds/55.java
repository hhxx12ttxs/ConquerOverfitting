public static String getTimePassedString(int seconds) {
if (seconds >= 60 * 60 * 24 * 30)
return seconds / (60 * 60 * 24 * 30) + &quot;개월 전&quot;;
else if (seconds >= 60 * 60 * 24)
return seconds / (60 * 60 * 24) + &quot;일 전&quot;;

