public static Timestamp add(Timestamp time, long hours)
{
long extendMillis  = Parser.hoursToMillis(hours);
long oldMillis     = time.getTime();
long currentMillis = System.currentTimeMillis();
if(currentMillis > oldMillis)
oldMillis = currentMillis;

