boolean showSeconds, boolean showMillis) {
if (millis <= 0) {
return &quot;0ms&quot;;
}

long millisInSecond = 1000;
long millisInHour = millisInMinute * 60;
long millisInDay = millisInHour * 24;

int days = (int) Math.floor(millis / millisInDay);

