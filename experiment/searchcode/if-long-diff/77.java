public abstract class DateUtils {

public static String convertDate(Long commentTimestamp){
Long timeDiff = (new Date().getTime() - commentTimestamp) / 1000;

if(timeDiff <= 3600){
return timeDiff / 60 + &quot; minutes ago&quot;;

