public String whatTime(int seconds){
int hours = 0, mins = 0;

if(seconds > 0){
if(seconds >= 3600){ hours = getHours(seconds); seconds = seconds % 3600; }
if (seconds >= 60){ mins = getMins(seconds); seconds = seconds % 60; }

