public static long getNextMidnightInMillis(long time){
long ret = 0;
if(time > 0){
long x = time % MILLIS_PER_DAY;
ret = time - x;

