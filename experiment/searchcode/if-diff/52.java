long targetTime = c.getTimeInMillis() / 1000;
long diff = curTime - targetTime;

if (targetTime > curTime) {
return &quot;방금&quot;;
} else {
if (diff < 3) {
return &quot;방금&quot;;

