public int getPercentages(String time) {

String[] wholeTime = time.split(&quot;:&quot;);

int totalSeconds = (Integer.parseInt(wholeTime[0]) * 60 * 60) + (Integer.parseInt(wholeTime[1]) * 60) + Integer.parseInt(wholeTime[2]);
int initialTotalSeconds = totalSeconds;

if(totalSeconds % 2 == 0) {
totalSeconds /= 2;
}
if(totalSeconds % 2 == 0) {

